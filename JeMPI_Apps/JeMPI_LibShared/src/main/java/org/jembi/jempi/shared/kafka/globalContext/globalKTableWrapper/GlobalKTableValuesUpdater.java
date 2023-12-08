package org.jembi.jempi.shared.kafka.globalContext.globalKTableWrapper;

import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;

public class GlobalKTableValuesUpdater<T> implements Processor<String, T, String, T> {

    private ProcessorContext<String, T> context;
    private KeyValueStore<String, T> globalStore;
    private final TableUpdaterProcessor<T, T, T> valuesUpdater;
    private final String globalStoreName;

    public GlobalKTableValuesUpdater(final TableUpdaterProcessor<T, T, T> valuesUpdater, final String globalStoreName){
        this.valuesUpdater = valuesUpdater;
        this.globalStoreName = globalStoreName;

    }
    @Override
    public void init(ProcessorContext<String, T> context) {
        this.context = context;
        this.globalStore = context.getStateStore(globalStoreName);
    }

    @Override
    public void process(final Record<String, T> record) {
        T updatedValue = this.valuesUpdater.apply(this.globalStore.get(record.key()), record.value());
        this.globalStore.put(record.key(), updatedValue);
        this.context.forward(record);
    }

}