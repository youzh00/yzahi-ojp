package openjproxy.jdbc;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import openjproxy.jdbc.testutil.TestDBUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests that verify transaction isolation level is properly reset when connections
 * are returned to the pool. This prevents connection state pollution between different
 * client sessions.
 * 
 * The test simulates multiple clients that:
 * 1. Change transaction isolation levels aggressively
 * 2. Close their connections (returning them to the pool)
 * 3. Verify that subsequent connections have the correct default isolation level
 */
@Slf4j
public class TransactionIsolationResetTest {

    private static boolean isH2TestEnabled;

    private Connection connection1;
    private Connection connection2;
    private Connection connection3;

    @BeforeAll
    public static void setupClass() {
        isH2TestEnabled = Boolean.parseBoolean(System.getProperty("enableH2Tests", "false"));
    }

    @AfterEach
    public void tearDown() throws SQLException {
        TestDBUtils.closeQuietly(connection1);
        TestDBUtils.closeQuietly(connection2);
        TestDBUtils.closeQuietly(connection3);
    }

    /**
     * Test that transaction isolation is reset to the default level when a connection
     * is returned to the pool and reused by another client.
     * 
     * Scenario:
     * 1. Client 1 gets a connection, changes isolation to SERIALIZABLE, closes it
     * 2. Client 2 gets a connection (should be the same physical connection from pool)
     * 3. Verify Client 2's connection has the default isolation level (READ_COMMITTED)
     * 4. Client 2 changes to READ_UNCOMMITTED, closes it
     * 5. Client 3 gets a connection (again from pool)
     * 6. Verify Client 3's connection has the default isolation level (READ_COMMITTED)
     */
    @ParameterizedTest
    @CsvFileSource(resources = "/h2_connection.csv")
    public void testTransactionIsolationResetBetweenSessions(String driverClass, String url, String user, String password) throws SQLException {
        Assumptions.assumeTrue(isH2TestEnabled, "Skipping H2 tests - not enabled");
        
        // Client 1: Change isolation and close
        log.info("Client 1: Getting connection and changing isolation to SERIALIZABLE");
        connection1 = DriverManager.getConnection(url, user, password);
        int defaultIsolation = connection1.getTransactionIsolation();
        log.info("Client 1: Default isolation level is: {}", defaultIsolation);
        
        connection1.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
        assertEquals(Connection.TRANSACTION_SERIALIZABLE, connection1.getTransactionIsolation());
        log.info("Client 1: Changed isolation to SERIALIZABLE, closing connection");
        connection1.close();
        
        // Small delay to ensure connection is processed and returned to pool
        // HikariCP processes connection returns asynchronously
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Client 2: Get connection from pool - should have default isolation
        log.info("Client 2: Getting connection from pool");
        connection2 = DriverManager.getConnection(url, user, password);
        int client2Isolation = connection2.getTransactionIsolation();
        log.info("Client 2: Transaction isolation level is: {}", client2Isolation);
        
        // Verify Client 2 gets the default isolation level, not SERIALIZABLE
        assertEquals(defaultIsolation, client2Isolation, 
                "Connection from pool should have default isolation level, not the level set by previous client");
        
        // Client 2: Change to different isolation and close
        log.info("Client 2: Changing isolation to READ_UNCOMMITTED, closing connection");
        connection2.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
        assertEquals(Connection.TRANSACTION_READ_UNCOMMITTED, connection2.getTransactionIsolation());
        connection2.close();
        
        // Small delay to ensure connection is processed and returned to pool
        // HikariCP processes connection returns asynchronously
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Client 3: Get connection from pool - should have default isolation
        log.info("Client 3: Getting connection from pool");
        connection3 = DriverManager.getConnection(url, user, password);
        int client3Isolation = connection3.getTransactionIsolation();
        log.info("Client 3: Transaction isolation level is: {}", client3Isolation);
        
        // Verify Client 3 gets the default isolation level, not READ_UNCOMMITTED
        assertEquals(defaultIsolation, client3Isolation, 
                "Connection from pool should have default isolation level, not the level set by previous client");
    }

    /**
     * Test aggressive transaction isolation changes within a single session.
     * This verifies that isolation changes work correctly during the lifetime of a session.
     */
    @ParameterizedTest
    @CsvFileSource(resources = "/h2_connection.csv")
    public void testAggressiveIsolationChangesWithinSession(String driverClass, String url, String user, String password) throws SQLException {
        Assumptions.assumeTrue(isH2TestEnabled, "Skipping H2 tests - not enabled");
        
        connection1 = DriverManager.getConnection(url, user, password);
        int defaultIsolation = connection1.getTransactionIsolation();
        
        // Change isolation multiple times
        connection1.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
        assertEquals(Connection.TRANSACTION_SERIALIZABLE, connection1.getTransactionIsolation());
        
        connection1.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
        assertEquals(Connection.TRANSACTION_READ_UNCOMMITTED, connection1.getTransactionIsolation());
        
        connection1.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
        assertEquals(Connection.TRANSACTION_REPEATABLE_READ, connection1.getTransactionIsolation());
        
        connection1.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        assertEquals(Connection.TRANSACTION_READ_COMMITTED, connection1.getTransactionIsolation());
        
        // Close and reopen - should get default isolation
        connection1.close();
        
        // Small delay to ensure connection is processed and returned to pool
        // HikariCP processes connection returns asynchronously
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        connection2 = DriverManager.getConnection(url, user, password);
        assertEquals(defaultIsolation, connection2.getTransactionIsolation(), 
                "After closing connection with multiple isolation changes, new connection should have default isolation");
    }

    /**
     * Test that multiple concurrent clients don't interfere with each other's
     * transaction isolation settings.
     */
    @ParameterizedTest
    @CsvFileSource(resources = "/h2_connection.csv")
    public void testConcurrentIsolationChanges(String driverClass, String url, String user, String password) throws SQLException {
        Assumptions.assumeTrue(isH2TestEnabled, "Skipping H2 tests - not enabled");
        
        // Open 3 connections concurrently
        connection1 = DriverManager.getConnection(url, user, password);
        connection2 = DriverManager.getConnection(url, user, password);
        connection3 = DriverManager.getConnection(url, user, password);
        
        // Set different isolation levels
        connection1.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
        connection2.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
        connection3.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
        
        // Verify each connection maintains its own isolation level
        assertEquals(Connection.TRANSACTION_SERIALIZABLE, connection1.getTransactionIsolation());
        assertEquals(Connection.TRANSACTION_READ_UNCOMMITTED, connection2.getTransactionIsolation());
        assertEquals(Connection.TRANSACTION_REPEATABLE_READ, connection3.getTransactionIsolation());
        
        log.info("Successfully verified concurrent connections maintain independent isolation levels");
    }
}
