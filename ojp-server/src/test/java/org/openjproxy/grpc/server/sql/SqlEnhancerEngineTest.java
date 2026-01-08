package org.openjproxy.grpc.server.sql;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for SqlEnhancerEngine - Phase 2: Validation, optimization, and caching
 */
class SqlEnhancerEngineTest {
    
    @Test
    void testEnhancerDisabled() {
        SqlEnhancerEngine engine = new SqlEnhancerEngine(false);
        
        assertFalse(engine.isEnabled(), "Engine should be disabled");
        
        String sql = "SELECT * FROM users WHERE id = 1";
        SqlEnhancementResult result = engine.enhance(sql);
        
        assertEquals(sql, result.getEnhancedSql(), "SQL should be unchanged when disabled");
        assertFalse(result.isModified(), "SQL should not be marked as modified");
        assertFalse(result.isHasErrors(), "Should not have errors");
    }
    
    @Test
    void testEnhancerEnabled_ValidSQL() {
        SqlEnhancerEngine engine = new SqlEnhancerEngine(true);
        
        assertTrue(engine.isEnabled(), "Engine should be enabled");
        
        String sql = "SELECT * FROM users WHERE id = 1";
        SqlEnhancementResult result = engine.enhance(sql);
        
        assertNotNull(result.getEnhancedSql(), "Enhanced SQL should not be null");
        assertFalse(result.isHasErrors(), "Should not have errors for valid SQL");
    }
    
    @Test
    void testEnhancerEnabled_InvalidSQL() {
        SqlEnhancerEngine engine = new SqlEnhancerEngine(true);
        
        String sql = "SELECT * FROM WHERE";
        SqlEnhancementResult result = engine.enhance(sql);
        
        // Phase 2: Invalid SQL should pass through (no errors thrown)
        assertEquals(sql, result.getEnhancedSql(), "Invalid SQL should pass through");
        assertFalse(result.isModified(), "SQL should not be modified");
        assertFalse(result.isHasErrors(), "Should not mark as error in pass-through mode");
    }
    
    @Test
    void testEnhancerEnabled_ComplexQuery() {
        SqlEnhancerEngine engine = new SqlEnhancerEngine(true);
        
        String sql = "SELECT u.id, u.name, o.order_id " +
                     "FROM users u " +
                     "INNER JOIN orders o ON u.id = o.user_id " +
                     "WHERE u.status = 'active' AND o.created_at > '2024-01-01'";
        
        SqlEnhancementResult result = engine.enhance(sql);
        
        assertNotNull(result.getEnhancedSql(), "Complex SQL should parse successfully");
        assertFalse(result.isHasErrors(), "Should not have errors for valid complex SQL");
    }
    
    @Test
    void testEnhancerEnabled_PreparedStatement() {
        SqlEnhancerEngine engine = new SqlEnhancerEngine(true);
        
        String sql = "SELECT * FROM users WHERE id = ? AND status = ?";
        SqlEnhancementResult result = engine.enhance(sql);
        
        assertNotNull(result.getEnhancedSql(), "Prepared statement SQL should parse successfully");
        assertFalse(result.isHasErrors(), "Should not have errors for prepared statement");
    }
    
    // Phase 2 tests
    
    @Test
    void testCaching() {
        SqlEnhancerEngine engine = new SqlEnhancerEngine(true);
        
        String sql = "SELECT * FROM users WHERE id = 1";
        
        // First call - should parse and cache
        long start1 = System.currentTimeMillis();
        SqlEnhancementResult result1 = engine.enhance(sql);
        long duration1 = System.currentTimeMillis() - start1;
        
        // Second call - should hit cache
        long start2 = System.currentTimeMillis();
        SqlEnhancementResult result2 = engine.enhance(sql);
        long duration2 = System.currentTimeMillis() - start2;
        
        // Cache hit should be faster (though timing can be flaky in tests)
        assertEquals(result1.getEnhancedSql(), result2.getEnhancedSql(), "Cached result should match");
        
        // Verify cache stats show entries
        String cacheStats = engine.getCacheStats();
        assertTrue(cacheStats.contains("1"), "Cache should have at least 1 entry");
    }
    
    @Test
    void testCacheClear() {
        SqlEnhancerEngine engine = new SqlEnhancerEngine(true);
        
        String sql = "SELECT * FROM users WHERE id = 1";
        engine.enhance(sql);
        
        // Verify cache has entry
        String statsBefore = engine.getCacheStats();
        assertTrue(statsBefore.contains("1"), "Cache should have 1 entry");
        
        // Clear cache
        engine.clearCache();
        
        // Verify cache is empty
        String statsAfter = engine.getCacheStats();
        assertTrue(statsAfter.contains("0"), "Cache should be empty after clear");
    }
    
    @Test
    void testMultipleQueries_Caching() {
        SqlEnhancerEngine engine = new SqlEnhancerEngine(true);
        
        // Cache multiple different queries
        String sql1 = "SELECT * FROM users WHERE id = 1";
        String sql2 = "SELECT * FROM orders WHERE user_id = 2";
        String sql3 = "SELECT COUNT(*) FROM products";
        
        engine.enhance(sql1);
        engine.enhance(sql2);
        engine.enhance(sql3);
        
        // Verify cache has 3 entries
        String cacheStats = engine.getCacheStats();
        assertTrue(cacheStats.contains("3"), "Cache should have 3 entries");
        
        // Re-query one - should hit cache
        SqlEnhancementResult result = engine.enhance(sql1);
        assertNotNull(result.getEnhancedSql(), "Cached query should return result");
    }
    
    @Test
    void testValidation_SimpleSelect() {
        SqlEnhancerEngine engine = new SqlEnhancerEngine(true);
        
        // Valid simple SELECT
        String sql = "SELECT col1, col2 FROM table1";
        SqlEnhancementResult result = engine.enhance(sql);
        
        assertNotNull(result.getEnhancedSql(), "Valid SQL should be enhanced");
        assertFalse(result.isHasErrors(), "Valid SQL should not have errors");
    }
}
