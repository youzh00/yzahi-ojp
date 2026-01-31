package org.openjproxy.grpc.server;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.*;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class CircuitBreakerMetrics {

    private final Meter meter = GlobalOpenTelemetry.getMeter("ojp.server.circuit.breaker");
    private final ObservableLongGauge observableGauge;
    private final LongCounter transitionsCounter;
    private final LongCounter tripsCounter;

    private final ConcurrentHashMap<String, State> states = new ConcurrentHashMap<>();

    private static final AttributeKey<String> QUERY_HASH = AttributeKey.stringKey("query_hash");
    private static final AttributeKey<String> FROM_STATE = AttributeKey.stringKey("from_state");
    private static final AttributeKey<String> TO_STATE = AttributeKey.stringKey("to_state");





    public CircuitBreakerMetrics(){
        this.observableGauge = this.meter
                .gaugeBuilder("ojp.circuit_breaker.state")
                .setDescription("Current circuit breaker state (0=CLOSED, 1=OPEN, 2=HALF_OPEN)")
                .ofLongs()
                .buildWithCallback(this::observeState);

        this.transitionsCounter = this.meter.counterBuilder("ojp.circuit_breaker.trips.total")
                .setDescription("Counts circuit breaker state transitions")
                .build();

        this.tripsCounter = this.meter.counterBuilder("ojp.circuit_breaker.transitions.total")
                .setDescription("Number of times the circuit breaker opened due to failures")
                .build();
    }

    private void observeState(ObservableLongMeasurement observer){
        for(Map.Entry<String, State> entry : states.entrySet()){
            observer.record(entry.getValue().value, Attributes.of(
                    QUERY_HASH, entry.getKey())
            );
        }
    }


    public void updateState(String queryHash, State newState){
        states.compute(queryHash, (key, previousState) -> {
            // First observation → establish baseline, no transition counted
            if (previousState == null) {
                return newState;
            }

            // No real transition → do nothing
            if (previousState == newState) {
                return previousState;
            }

            // Real transition detected → increment counter
            transitionsCounter.add(
                    1,
                    Attributes.of(
                            FROM_STATE, previousState.name(),
                            TO_STATE, newState.name(),
                            QUERY_HASH, queryHash
                    )
            );

            // Update stored state
            return newState;
        });
    }

    enum State {
        CLOSED(0),
        OPEN(1),
        HALF_OPEN(2);

        final long value;
        State(int i) {
            this.value = i;
        }
    }
}