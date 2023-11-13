package org.jembi.jempi.shared.kafka.globalContext.globalKTableWrapper;

@FunctionalInterface
public interface TableUpdaterProcessor<G, C, R> { //TODO: Exception
    R apply(G globalValue, C currentValue);
}