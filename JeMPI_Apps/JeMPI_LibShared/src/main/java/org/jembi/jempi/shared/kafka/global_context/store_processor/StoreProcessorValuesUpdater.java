package org.jembi.jempi.shared.kafka.global_context.store_processor;

import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;
import org.jembi.jempi.shared.kafka.MyKafkaProducer;

import java.util.concurrent.ExecutionException;

public final class StoreProcessorValuesUpdater<T> implements Processor<String, T, Void, Void> {

    private ProcessorContext<Void, Void> context;
    private KeyValueStore<String, T> sinkStore;
    private final StoreUpdaterProcessor<T, T, T> valuesUpdater;
    private final String sinkStoreName;

    private MyKafkaProducer<String, T> sinkUpdater;
    public StoreProcessorValuesUpdater(final StoreUpdaterProcessor<T, T, T> valuesUpdater, final String sinkStoreName, final MyKafkaProducer<String, T> sinkUpdater) {
        this.valuesUpdater = valuesUpdater;
        this.sinkStoreName = sinkStoreName;
        this.sinkUpdater = sinkUpdater;

    }
    @Override
    public void init(final ProcessorContext<Void, Void> context) {
        this.context = context;
        this.sinkStore = context.getStateStore(sinkStoreName);
    }

    @Override
    public void process(final Record<String, T> recordToProcess) {
        T updatedValue = this.valuesUpdater.apply(this.sinkStore.get(recordToProcess.key()), recordToProcess.value());
        try {
            this.sinkUpdater.produceSync(recordToProcess.key(), updatedValue);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        this.context.commit();
    }

}
