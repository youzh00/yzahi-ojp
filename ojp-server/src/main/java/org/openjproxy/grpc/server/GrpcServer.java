package org.openjproxy.grpc.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.health.v1.HealthCheckResponse;
import io.grpc.netty.NettyServerBuilder;
import io.opentelemetry.instrumentation.grpc.v1_6.GrpcTelemetry;
import org.openjproxy.constants.CommonConstants;
import org.openjproxy.grpc.server.utils.DriverLoader;
import org.openjproxy.grpc.server.utils.DriverUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GrpcServer {
    private static final Logger logger = LoggerFactory.getLogger(GrpcServer.class);

    public static void main(String[] args) throws IOException, InterruptedException {
        // Initialize health status manager
        OjpHealthManager.initialize();

        // Load configuration
        ServerConfiguration config = new ServerConfiguration();
        
        // Load external JDBC drivers from configured directory
        logger.info("Loading external JDBC drivers...");
        boolean driversLoaded = DriverLoader.loadDriversFromPath(config.getDriversPath());
        if (!driversLoaded) {
            logger.warn("Failed to load external libraries from path: {}", config.getDriversPath());
            logger.warn("Server will continue, but proprietary drivers may not be available");
        }
        
        // Register JDBC drivers (both built-in and externally loaded)
        DriverUtils.registerDrivers(config.getDriversPath());
        
        // Validate IP whitelist for server
        if (!IpWhitelistValidator.validateWhitelistRules(config.getAllowedIps())) {
            logger.error("Invalid IP whitelist configuration for server. Exiting.");
            System.exit(1);
        }

        // Initialize telemetry based on configuration
        OjpServerTelemetry ojpServerTelemetry = new OjpServerTelemetry();
        GrpcTelemetry grpcTelemetry;
        
        if (config.isOpenTelemetryEnabled()) {
            grpcTelemetry = ojpServerTelemetry.createGrpcTelemetry(
                config.getPrometheusPort(), 
                config.getPrometheusAllowedIps()
            );

            OjpHealthManager.setServiceStatus(OjpHealthManager.Services.OPENTELEMETRY_SERVICE,
                    HealthCheckResponse.ServingStatus.SERVING);
        } else {
            grpcTelemetry = ojpServerTelemetry.createNoOpGrpcTelemetry();
        }

        // Build server with configuration
        SessionManagerImpl sessionManager = new SessionManagerImpl();
        
        NettyServerBuilder serverBuilder = NettyServerBuilder
                .forPort(config.getServerPort())
                .executor(Executors.newFixedThreadPool(config.getThreadPoolSize()))
                .maxInboundMessageSize(config.getMaxRequestSize())
                .keepAliveTime(config.getConnectionIdleTimeout(), TimeUnit.MILLISECONDS)
                .addService(new StatementServiceImpl(
                        sessionManager,
                        new CircuitBreaker(config.getCircuitBreakerTimeout(), config.getCircuitBreakerThreshold()),
                        config
                ))
                .addService(OjpHealthManager.getHealthStatusManager().getHealthService())
                .intercept(new IpWhitelistingInterceptor(config.getAllowedIps()))
                .intercept(grpcTelemetry.newServerInterceptor());
        
        // Configure TLS if enabled
        if (config.isTlsEnabled()) {
            try {
                configureTls(serverBuilder, config);
                logger.info("TLS/mTLS enabled for gRPC server");
            } catch (Exception e) {
                logger.error("Failed to configure TLS for gRPC server: {}", e.getMessage(), e);
                System.exit(1);
            }
        } else {
            logger.info("TLS not enabled - using plaintext communication");
        }

        Server server = serverBuilder.build();

        logger.info("Starting OJP gRPC Server on port {}", config.getServerPort());
        logger.info("Server configuration applied successfully");
        
        // Initialize session cleanup task if enabled
        ScheduledExecutorService sessionCleanupExecutor = null;
        if (config.isSessionCleanupEnabled()) {
            logger.info("Initializing session cleanup task: timeout={}min, interval={}min", 
                    config.getSessionTimeoutMinutes(), config.getSessionCleanupIntervalMinutes());
            
            sessionCleanupExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread thread = new Thread(r, "ojp-session-cleanup");
                thread.setDaemon(true);
                return thread;
            });
            
            long timeoutMillis = config.getSessionTimeoutMinutes() * 60 * 1000;
            long intervalMillis = config.getSessionCleanupIntervalMinutes() * 60 * 1000;
            
            SessionCleanupTask cleanupTask = new SessionCleanupTask(sessionManager, timeoutMillis);
            sessionCleanupExecutor.scheduleAtFixedRate(
                    cleanupTask, 
                    intervalMillis, // Initial delay
                    intervalMillis, // Period
                    TimeUnit.MILLISECONDS
            );
            
            logger.info("Session cleanup task started successfully");
        } else {
            logger.info("Session cleanup is disabled");
        }
        
        server.start();
        OjpHealthManager.setServiceStatus(OjpHealthManager.Services.OJP_SERVER,
                HealthCheckResponse.ServingStatus.SERVING);
        
        // Add shutdown hook
        ScheduledExecutorService finalSessionCleanupExecutor = sessionCleanupExecutor;
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down OJP gRPC Server...");
            
            // Shutdown session cleanup task first
            if (finalSessionCleanupExecutor != null) {
                logger.info("Shutting down session cleanup executor...");
                finalSessionCleanupExecutor.shutdown();
                try {
                    if (!finalSessionCleanupExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                        logger.warn("Session cleanup executor did not terminate gracefully");
                        finalSessionCleanupExecutor.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    logger.warn("Interrupted while waiting for session cleanup shutdown");
                    finalSessionCleanupExecutor.shutdownNow();
                    Thread.currentThread().interrupt();
                }
            }
            
            server.shutdown();

            try {
                if (!server.awaitTermination(30, TimeUnit.SECONDS)) {
                    logger.warn("Server did not terminate gracefully, forcing shutdown");
                    server.shutdownNow();
                }
            } catch (InterruptedException e) {
                logger.warn("Interrupted while waiting for server shutdown");
                server.shutdownNow();
                Thread.currentThread().interrupt();
            }
            
            logger.info("OJP gRPC Server shutdown complete");
        }));

        logger.info("OJP gRPC Server started successfully and awaiting termination");
        server.awaitTermination();
    }
    
    /**
     * Configures TLS/mTLS for the gRPC server.
     * 
     * @param serverBuilder The NettyServerBuilder to configure
     * @param config Server configuration containing TLS settings
     * @throws Exception if TLS configuration fails
     */
    private static void configureTls(NettyServerBuilder serverBuilder, ServerConfiguration config) throws Exception {
        io.grpc.netty.GrpcSslContexts.SslContextBuilder sslContextBuilder = 
            io.grpc.netty.GrpcSslContexts.forServer(
                loadKeyManager(config)
            );
        
        // Configure client authentication (mTLS) if required
        if (config.isTlsClientAuthRequired()) {
            logger.info("mTLS (mutual TLS) enabled - client certificates required");
            sslContextBuilder.trustManager(loadTrustManager(config));
            sslContextBuilder.clientAuth(io.netty.handler.ssl.ClientAuth.REQUIRE);
        } else {
            logger.info("Server TLS enabled - client certificates not required");
            // Optionally configure truststore for client certificate verification
            if (config.getTlsTruststorePath() != null && !config.getTlsTruststorePath().trim().isEmpty()) {
                logger.info("Truststore configured for optional client certificate verification");
                sslContextBuilder.trustManager(loadTrustManager(config));
                sslContextBuilder.clientAuth(io.netty.handler.ssl.ClientAuth.OPTIONAL);
            }
        }
        
        io.netty.handler.ssl.SslContext sslContext = sslContextBuilder.build();
        serverBuilder.sslContext(sslContext);
    }
    
    /**
     * Loads the key manager for server certificate.
     * 
     * @param config Server configuration
     * @return KeyManagerFactory for server certificate
     * @throws Exception if keystore cannot be loaded
     */
    private static javax.net.ssl.KeyManagerFactory loadKeyManager(ServerConfiguration config) throws Exception {
        if (config.getTlsKeystorePath() == null || config.getTlsKeystorePath().trim().isEmpty()) {
            throw new IllegalStateException("TLS is enabled but keystore path is not configured. " +
                    "Set ojp.server.tls.keystore.path property.");
        }
        
        java.security.KeyStore keyStore = java.security.KeyStore.getInstance(config.getTlsKeystoreType());
        try (java.io.FileInputStream fis = new java.io.FileInputStream(new java.io.File(config.getTlsKeystorePath()))) {
            keyStore.load(fis, config.getTlsKeystorePassword() != null ? 
                config.getTlsKeystorePassword().toCharArray() : null);
        }
        
        javax.net.ssl.KeyManagerFactory keyManagerFactory = 
            javax.net.ssl.KeyManagerFactory.getInstance(javax.net.ssl.KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, config.getTlsKeystorePassword() != null ? 
            config.getTlsKeystorePassword().toCharArray() : null);
        
        return keyManagerFactory;
    }
    
    /**
     * Loads the trust manager for client certificate verification.
     * 
     * @param config Server configuration
     * @return TrustManagerFactory for client certificate verification
     * @throws Exception if truststore cannot be loaded
     */
    private static javax.net.ssl.TrustManagerFactory loadTrustManager(ServerConfiguration config) throws Exception {
        if (config.getTlsTruststorePath() == null || config.getTlsTruststorePath().trim().isEmpty()) {
            logger.info("No truststore path configured, using JVM default truststore");
            // Return default trust manager factory
            javax.net.ssl.TrustManagerFactory trustManagerFactory = 
                javax.net.ssl.TrustManagerFactory.getInstance(javax.net.ssl.TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init((java.security.KeyStore) null);
            return trustManagerFactory;
        }
        
        java.security.KeyStore trustStore = java.security.KeyStore.getInstance(config.getTlsTruststoreType());
        try (java.io.FileInputStream fis = new java.io.FileInputStream(new java.io.File(config.getTlsTruststorePath()))) {
            trustStore.load(fis, config.getTlsTruststorePassword() != null ? 
                config.getTlsTruststorePassword().toCharArray() : null);
        }
        
        javax.net.ssl.TrustManagerFactory trustManagerFactory = 
            javax.net.ssl.TrustManagerFactory.getInstance(javax.net.ssl.TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(trustStore);
        
        return trustManagerFactory;
    }
}
