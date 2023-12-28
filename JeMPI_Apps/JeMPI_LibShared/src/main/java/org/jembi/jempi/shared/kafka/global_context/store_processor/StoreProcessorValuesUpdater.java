package org.jembi.jempi.shared.kafka.global_context.store_processor;

import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.processor.api.ProcessorContext;

import java.util.concurrent.ExecutionException;

public final class StoreProcessorValuesUpdater<T> implements Processor<String, T, Void, Void> {

    private ProcessorContext<Void, Void> context;
    private KeyValueStore<String, T> topicStore;
    private final StoreUpdaterProcessor<T, T, T> valuesUpdater;
    private final String topicStoreName;
    private final String topicName;
    private final StoreProcessorSinkManager<T> sinkManager;

    public StoreProcessorValuesUpdater(final StoreUpdaterProcessor<T, T, T> valuesUpdater,
                                       final String topicName,
                                       final String topicStoreName,
                                       final StoreProcessorSinkManager<T> sinkManager
                                       ) {
        this.valuesUpdater = valuesUpdater;
        this.topicName = topicName;
        this.topicStoreName = topicStoreName;
        this.sinkManager = sinkManager;
    }

    private T readLastValue(Record<String, T> recordToProcess){
        T lastValue = null;

        if (recordToProcess != null) {
            lastValue = this.topicStore.get(recordToProcess.key());
            if (lastValue != null) {
                return lastValue;
            }
        }
        // This only happens on process starts to prime the global store
        return this.sinkManager.readSink();

    }
    @Override
    public void init(final ProcessorContext<Void, Void> context) {
        this.context = context;
        this.topicStore = context.getStateStore(topicStoreName);
        T lastValue = readLastValue(null);
        if (lastValue != null){
            this.topicStore.put(topicName, lastValue);
        }
    }

    @Override
    public void process(final Record<String, T> recordToProcess) {
        T updatedValue = this.valuesUpdater.apply(readLastValue(recordToProcess), recordToProcess.value());
        try {
            this.sinkManager.updateSink(updatedValue);
            this.topicStore.put(recordToProcess.key(), updatedValue);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        this.context.commit();
    }

}
