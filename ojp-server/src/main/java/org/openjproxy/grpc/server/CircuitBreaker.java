package org.openjproxy.grpc.server;

import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Implements a basic circuit breaker that counts failures and return the latest error if a threshold is exceeded.
 */
public class CircuitBreaker {
    private static class FailureRecord {
        private volatile SQLException lastError;
        private final AtomicInteger failureCount = new AtomicInteger(0);
        private final AtomicLong openUntil = new AtomicLong(0);
        private final int failureThreashold;

        public FailureRecord(int failureThreashold) {
            this.failureThreashold = failureThreashold;
        }

        public void recordFailure(SQLException error, long openMs) {
            lastError = error;
            int failures = failureCount.incrementAndGet();
            if (failures >= failureThreashold) {
                openUntil.updateAndGet(prev -> Math.max(prev, System.currentTimeMillis() + openMs));
            }
        }

        public boolean isOpen() {
            long until = openUntil.get();
            if (failureCount.get() < failureThreashold) return false;
            if (System.currentTimeMillis() > until) {
                return false;
            }
            return true;
        }

        public boolean tryReset() {
            long until = openUntil.get();
            if (System.currentTimeMillis() > until && failureCount.get() >= failureThreashold) {
                return true;
            }
            return false;
        }

        public void reset() {
            failureCount.set(0);
            openUntil.set(0);
            lastError = null;
        }

        public SQLException getLastError() {
            return lastError;
        }

        public int getFailureCount() {
            return failureCount.get();
        }
    }

    private final ConcurrentHashMap<String, FailureRecord> state = new ConcurrentHashMap<>();
    private final long openMs;
    private final int failureThreashold;
    private final CircuitBreakerMetrics metrics;

    public CircuitBreaker(long openMs, int failureThreashold, CircuitBreakerMetrics metrics) {
        this.openMs = openMs;
        this.failureThreashold = failureThreashold;
        this.metrics = metrics;
    }

    /**
     * Call when a statement is received.
     * @param sql The SQL statement (normalized string).
     * @throws java.sql.SQLException if blocked (open).
     */
    public void preCheck(String sql) throws SQLException {
        FailureRecord rec = state.get(sql);
        if (rec == null) {
            metrics.updateState(sql, CircuitBreakerMetrics.State.CLOSED);
            return;
        }

        if (rec.isOpen()) {
            metrics.updateState(sql, CircuitBreakerMetrics.State.OPEN);
            throw rec.getLastError();
        }

        if (rec.getFailureCount() >= this.failureThreashold ) {
            metrics.updateState(sql, CircuitBreakerMetrics.State.HALF_OPEN);
        } else {
            metrics.updateState(sql, CircuitBreakerMetrics.State.CLOSED);
        }

        rec.tryReset();
    }

    /**
     * Call when a statement succeeds.
     * @param sql The SQL statement.
     */
    public void onSuccess(String sql) {
        FailureRecord rec = state.get(sql);
        if (rec != null) {
            rec.reset();
            state.remove(sql, rec);
        }

        metrics.updateState(sql, CircuitBreakerMetrics.State.CLOSED);
    }

    /**
     * Call when a statement fails. If called when already opened will not record the failure,
     * that is intended so it can always be called from catch blocks without checks as per if
     * a catch block might be handling an exception thrown by the circuit breaker itself.
     * @param sql The SQL statement.
     * @param error The exception.
     */
    public void onFailure(String sql, SQLException error) {
        FailureRecord rec = state.computeIfAbsent(sql, s -> new FailureRecord(this.failureThreashold));
        if (!rec.isOpen()) {
            rec.recordFailure(error, openMs);
        }

        if (rec.isOpen()) {
            metrics.updateState(sql, CircuitBreakerMetrics.State.OPEN);
        } else {
            metrics.updateState(sql, CircuitBreakerMetrics.State.CLOSED);
        }
    }
}