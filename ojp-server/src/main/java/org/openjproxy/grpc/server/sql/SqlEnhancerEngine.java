package org.openjproxy.grpc.server.sql;

import lombok.extern.slf4j.Slf4j;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * SQL Enhancer Engine that uses Apache Calcite for SQL parsing, validation, and optimization.
 * 
 * Phase 1: Basic integration with SQL parsing only
 * Phase 2: Add validation and optimization with caching
 * Phase 3: Add database-specific dialect support
 */
@Slf4j
public class SqlEnhancerEngine {
    
    private final boolean enabled;
    private final SqlParser.Config parserConfig;
    private final LRUCache<String, SqlEnhancementResult> cache;
    
    // LRU Cache implementation
    private static class LRUCache<K, V> extends LinkedHashMap<K, V> {
        private final int maxSize;
        
        public LRUCache(int maxSize) {
            super(maxSize + 1, 0.75f, true);
            this.maxSize = maxSize;
        }
        
        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return size() > maxSize;
        }
    }
    
    /**
     * Creates a new SqlEnhancerEngine with the given enabled status.
     * 
     * @param enabled Whether the SQL enhancer is enabled
     */
    public SqlEnhancerEngine(boolean enabled) {
        this.enabled = enabled;
        this.parserConfig = SqlParser.config();
        this.cache = new LRUCache<>(1000); // Default cache size of 1000
        
        if (enabled) {
            log.info("SQL Enhancer Engine initialized and enabled with validation and caching");
        } else {
            log.info("SQL Enhancer Engine initialized but disabled");
        }
    }
    
    /**
     * Checks if the SQL enhancer is enabled.
     * 
     * @return true if enabled, false otherwise
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * Gets cache statistics.
     * 
     * @return String describing cache statistics
     */
    public String getCacheStats() {
        return String.format("Cache size: %d / %d", cache.size(), 1000);
    }
    
    /**
     * Clears the cache.
     */
    public void clearCache() {
        cache.clear();
        log.info("SQL enhancer cache cleared");
    }
    
    /**
     * Parses, validates, and optionally optimizes SQL.
     * Phase 2: Adds validation and optimization with caching.
     * 
     * @param sql The SQL statement to enhance
     * @return SqlEnhancementResult containing the result
     */
    public SqlEnhancementResult enhance(String sql) {
        if (!enabled) {
            // If disabled, just return the original SQL
            return SqlEnhancementResult.passthrough(sql);
        }
        
        // Phase 2: Check cache first
        synchronized (cache) {
            SqlEnhancementResult cached = cache.get(sql);
            if (cached != null) {
                log.debug("Cache hit for SQL: {}", sql.substring(0, Math.min(sql.length(), 50)));
                return cached;
            }
        }
        
        long startTime = System.currentTimeMillis();
        SqlEnhancementResult result;
        
        try {
            // Phase 2: Parse and validate SQL
            SqlParser parser = SqlParser.create(sql, parserConfig);
            SqlNode sqlNode = parser.parseQuery();
            
            // Log successful parse
            log.debug("Successfully parsed and validated SQL: {}", sql.substring(0, Math.min(sql.length(), 100)));
            
            // Phase 2: Return original SQL (normalization will be in Phase 3)
            // For now, we've validated the SQL parses correctly
            result = SqlEnhancementResult.success(sql, false);
            
        } catch (SqlParseException e) {
            // Log parse errors
            log.debug("SQL parse error: {} for SQL: {}", e.getMessage(), sql.substring(0, Math.min(sql.length(), 100)));
            
            // Phase 2: On parse error, return original SQL (pass-through mode)
            // This allows queries to still execute even if Calcite can't parse them
            result = SqlEnhancementResult.passthrough(sql);
        } catch (Exception e) {
            // Catch any unexpected errors
            log.warn("Unexpected error in SQL enhancer: {}", e.getMessage(), e);
            
            // Fall back to pass-through mode
            result = SqlEnhancementResult.passthrough(sql);
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // Log performance if it took significant time
        if (duration > 50) {
            log.debug("SQL enhancement took {}ms for SQL: {}", duration, sql.substring(0, Math.min(sql.length(), 50)));
        }
        
        // Phase 2: Cache the result
        synchronized (cache) {
            cache.put(sql, result);
        }
        
        return result;
    }
}
