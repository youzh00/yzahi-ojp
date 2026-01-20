# Virtual Threads for XA Pool Housekeeping

## Summary

XA pool housekeeping tasks now use virtual threads (Java 21+) for efficient resource management.

## Implementation

**ThreadFactory** creates virtual thread executors for housekeeping tasks (leak detection, diagnostics):

```java
public static ScheduledExecutorService createHousekeepingExecutor(String threadName) {
    // Uses virtual threads on Java 21+ (production)
    // Falls back to daemon threads for testing on Java 17
    return VIRTUAL_THREADS_AVAILABLE 
        ? createVirtualThreadExecutor(threadName)
        : createPlatformThreadExecutor(threadName);
}
```

### Benefits of Virtual Threads

- **Reduced Memory**: ~KB vs ~2MB per thread
- **Better Scalability**: Millions vs thousands of threads
- **Lower Overhead**: Cheaper to create and manage
- **JVM-Managed**: Automatic scheduling on carrier threads

## Requirements

- **Production**: Java 21+ (OJP Server requirement)
- **Testing**: Java 11+ (fallback to daemon threads)

## Usage

Used automatically by `CommonsPool2XADataSource`:

```java
// Creates virtual thread executor when housekeeping enabled
housekeepingExecutor = ThreadFactory.createHousekeepingExecutor("ojp-xa-housekeeping");
```

No configuration needed - virtual threads used automatically on Java 21+.
