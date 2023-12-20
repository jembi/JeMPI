package org.jembi.jempi.shared.kafka.global_context.store_processor;

@FunctionalInterface
public interface StoreUpdaterProcessor<G, C, R> {
    R apply(G globalValue, C currentValue);
}
