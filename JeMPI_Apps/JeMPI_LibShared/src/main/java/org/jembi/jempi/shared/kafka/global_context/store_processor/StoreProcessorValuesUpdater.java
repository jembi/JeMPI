package org.jembi.jempi.shared.kafka.global_context.store_processor;

import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;

public class StoreProcessorValuesUpdater<T> implements Processor<String, T, Void, Void> {

    private ProcessorContext<Void, Void> context;
    private KeyValueStore<String, T> globalStore;
    private final StoreUpdaterProcessor<T, T, T> valuesUpdater;
    private final String globalStoreName;

    public StoreProcessorValuesUpdater(final StoreUpdaterProcessor<T, T, T> valuesUpdater, final String globalStoreName){
        this.valuesUpdater = valuesUpdater;
        this.globalStoreName = globalStoreName;

    }
    @Override
    public void init(ProcessorContext<Void, Void> context) {
        this.context = context;
        this.globalStore = context.getStateStore(globalStoreName);
    }

    @Override
    public void process(final Record<String, T> recordToProcess) {
        T updatedValue = this.valuesUpdater.apply(this.globalStore.get(recordToProcess.key()), recordToProcess.value());
        this.globalStore.put(recordToProcess.key(), updatedValue);
        this.context.commit();
    }

}