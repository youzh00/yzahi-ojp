package org.openjproxy.grpc.client;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.openjproxy.jdbc.xa.OjpXADataSource;

import javax.sql.XAConnection;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/**
 * Integration test for XA pool rebalancing behavior.
 * Tests a single OJP server's ability to rebalance its XA pool based on cluster health metadata.
 * 
 * The test simulates multinode cluster topology changes by modifying the connection URL:
 * 1. Connect to OJP server at localhost:1059 with a multinode URL (localhost:1059,localhost:99999)
 *    This simulates a 2-server cluster where the second server (port 99999) doesn't actually exist
 * 2. Verify the XA pool divides capacity (~10 connections per "server")
 * 3. Reconnect with a single-node URL (localhost:1059)
 *    This simulates the second server going down
 * 4. Verify the XA pool expands to full capacity (~20 connections)
 * 
 * This approach is much cheaper than spinning up multiple actual OJP servers.
 */
@Slf4j
public class XAPoolRebalancingIntegrationTest {

    // XID components for testing
    private static final byte[] GLOBAL_TXN_ID_BASE = "global_txn_".getBytes();
    private static final byte[] BRANCH_QUALIFIER = "branch_001".getBytes();
    private static int xidCounter = 0;
    
    protected static boolean isTestDisabled;

    @BeforeAll
    public static void checkTestConfiguration() {
        // Check both system property and environment variable
        boolean sysPropEnabled = Boolean.parseBoolean(System.getProperty("poolRebalancingTestsEnabled", "false"));
        boolean envVarEnabled = Boolean.parseBoolean(System.getenv("POOL_REBALANCING_TESTS_ENABLED"));
        isTestDisabled = !(sysPropEnabled || envVarEnabled);
    }

    @SneakyThrows
    @ParameterizedTest
    @CsvFileSource(resources = "/multinode_connection.csv")
    public void testXAPoolRebalancesOnSimulatedClusterChange(String driverClass, String url, String user, String password) throws Exception {
        assumeFalse(isTestDisabled, "Pool rebalancing tests are disabled");
        
        log.info("=== Starting XA Pool Rebalancing Test ===");
        log.info("Base URL: {}", url);
        
        // Create test table using non-XA connection
        Class.forName(driverClass);
        try (Connection conn = java.sql.DriverManager.getConnection(url, user, password)) {
            Statement stmt = conn.createStatement();
            stmt.execute("DROP TABLE IF EXISTS test_rebalance");
            stmt.execute("CREATE TABLE test_rebalance (id SERIAL PRIMARY KEY, value VARCHAR(100))");
            stmt.close();
            log.info("Test table created successfully");
        }
        
        // Phase 1: Insert with 2-server cluster URL (localhost:1059,localhost:99999)
        // The second server doesn't exist but the URL simulates a 2-server cluster
        log.info("Phase 1: Inserting data with 2-server cluster configuration");
        String twoServerUrl = "jdbc:ojp:postgresql://localhost:1059,localhost:99999/defaultdb";
        log.info("URL: {}", twoServerUrl);
        
        OjpXADataSource xaDataSource = new OjpXADataSource();
        xaDataSource.setUrl(twoServerUrl);
        xaDataSource.setUser(user);
        xaDataSource.setPassword(password);
        
        int initialConnections = performXAInsert(xaDataSource, "phase1_value", url, user, password);
        
        log.info("Initial connection count: {}", initialConnections);
        
        // Verify initial pool size is divided (should be around 8-12 connections)
        // With 2 servers in URL, each should get ~50% of the pool capacity
        assertTrue(initialConnections >= 8 && initialConnections <= 13,
                "Initial connections should be 8-13 (divided pool), but got: " + initialConnections);
        
        // Wait for pool to stabilize
        Thread.sleep(3000);
        
        // Phase 2: Insert with 1-server cluster URL (simulating server failure)
        log.info("Phase 2: Inserting data with 1-server cluster configuration (server failure simulation)");
        String singleServerUrl = "jdbc:ojp:postgresql://localhost:1059/defaultdb";
        log.info("URL: {}", singleServerUrl);
        
        OjpXADataSource singleServerDataSource = new OjpXADataSource();
        singleServerDataSource.setUrl(singleServerUrl);
        singleServerDataSource.setUser(user);
        singleServerDataSource.setPassword(password);
        
        int expandedConnections = performXAInsert(singleServerDataSource, "phase2_value", url, user, password);
        
        log.info("Expanded connection count: {}", expandedConnections);
        
        // Verify pool expanded to handle full capacity (should be around 18-22 connections)
        // With 1 server in URL, it should use 100% of the pool capacity
        assertTrue(expandedConnections >= 16 && expandedConnections <= 23,
                "Expanded connections should be 16-23 (full pool), but got: " + expandedConnections);
        
        // Verify the expansion happened (should grow by at least 50%)
        double growthRatio = (double) expandedConnections / initialConnections;
        assertTrue(growthRatio >= 1.4,
                String.format("Growth ratio should be >= 1.4x, but got: %.2fx (%d -> %d)",
                        growthRatio, initialConnections, expandedConnections));
        
        log.info("=== XA Pool Rebalancing Test PASSED ===");
        log.info("Pool successfully expanded from {} to {} connections ({}x growth)",
                initialConnections, expandedConnections, String.format("%.2f", growthRatio));
    }

    /**
     * Performs an XA insert transaction and returns the current PostgreSQL connection count
     */
    private int performXAInsert(OjpXADataSource dataSource, String value, String dbUrl, String dbUser, String dbPassword) throws Exception {
        XAConnection xaConn = null;
        Connection conn = null;
        
        try {
            // Get XA connection
            xaConn = dataSource.getXAConnection();
            XAResource xaResource = xaConn.getXAResource();
            conn = xaConn.getConnection();
            
            // Create unique XID
            Xid xid = createXid();
            
            // Start XA transaction
            log.info("Starting XA transaction");
            xaResource.start(xid, XAResource.TMNOFLAGS);
            
            // Execute insert
            PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO test_rebalance (value) VALUES (?)");
            pstmt.setString(1, value);
            int inserted = pstmt.executeUpdate();
            pstmt.close();
            
            log.info("Inserted {} row(s)", inserted);
            
            // End XA transaction
            xaResource.end(xid, XAResource.TMSUCCESS);
            
            // Prepare
            int prepared = xaResource.prepare(xid);
            log.info("XA prepare result: {}", prepared);
            
            // Commit
            xaResource.commit(xid, false);
            log.info("XA transaction committed successfully");
            
            // Wait for pool to adjust and stabilize
            return waitForConnectionCount(8, 60, dbUrl, dbUser, dbPassword);
            
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception e) {
                    log.warn("Error closing connection: {}", e.getMessage());
                }
            }
            if (xaConn != null) {
                try {
                    xaConn.close();
                } catch (Exception e) {
                    log.warn("Error closing XA connection: {}", e.getMessage());
                }
            }
        }
    }
    
    /**
     * Creates a unique XID for each transaction
     */
    private Xid createXid() {
        byte[] globalId = (new String(GLOBAL_TXN_ID_BASE) + (++xidCounter)).getBytes();
        return new TestXid(globalId, BRANCH_QUALIFIER, xidCounter);
    }


    /**
     * Waits for the connection count to reach at least the minimum expected count.
     * Polls every 5 seconds for up to maxWaitSeconds.
     */
    private int waitForConnectionCount(int minExpected, int maxWaitSeconds, String dbUrl, String dbUser, String dbPassword) throws Exception {
        int elapsed = 0;
        int currentCount = 0;
        
        while (elapsed < maxWaitSeconds) {
            currentCount = getPostgresConnectionCount(dbUrl, dbUser, dbPassword);
            log.info("Current PostgreSQL connections: {} (waiting for >= {})", currentCount, minExpected);
            
            if (currentCount >= minExpected) {
                log.info("Connection count reached: {}", currentCount);
                return currentCount;
            }
            
            Thread.sleep(5000);
            elapsed += 5;
        }
        
        log.warn("Timeout waiting for connection count. Expected >= {}, got {}", minExpected, currentCount);
        return currentCount;
    }

    /**
     * Queries PostgreSQL to get the current number of active connections to the database.
     * Counts idle connections from the OJP server backend pool.
     */
    private int getPostgresConnectionCount(String dbUrl, String dbUser, String dbPassword) throws Exception {
        try (Connection conn = java.sql.DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(
                    "SELECT count(*) FROM pg_stat_activity " +
                    "WHERE datname = current_database() " +
                    "AND state = 'idle' " +
                    "AND application_name != 'psql'");
            
            if (rs.next()) {
                int count = rs.getInt(1);
                rs.close();
                stmt.close();
                return count;
            }
            
            rs.close();
            stmt.close();
            return 0;
        }
    }

    /**
     * Simple Xid implementation for testing
     */
    private static class TestXid implements Xid {
        private final byte[] globalTxnId;
        private final byte[] branchQualifier;
        private final int formatId;

        public TestXid(byte[] globalTxnId, byte[] branchQualifier, int formatId) {
            this.globalTxnId = globalTxnId;
            this.branchQualifier = branchQualifier;
            this.formatId = formatId;
        }

        @Override
        public int getFormatId() {
            return formatId;
        }

        @Override
        public byte[] getGlobalTransactionId() {
            return globalTxnId;
        }

        @Override
        public byte[] getBranchQualifier() {
            return branchQualifier;
        }
    }
}
