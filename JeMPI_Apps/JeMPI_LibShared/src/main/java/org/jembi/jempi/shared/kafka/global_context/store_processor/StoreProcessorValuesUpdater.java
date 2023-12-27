package org.jembi.jempi.shared.kafka.global_context.store_processor;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.jembi.jempi.shared.kafka.MyKafkaProducer;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public final class StoreProcessorValuesUpdater<T> implements Processor<String, T, Void, Void> {

    private ProcessorContext<Void, Void> context;
    private KeyValueStore<String, T> topicStore;
    private final StoreUpdaterProcessor<T, T, T> valuesUpdater;
    private final String topicStoreName;
    private final String topicName;
    private final String sinkStoreTopic;
    private final Consumer<String, T> sinkReader;
    private MyKafkaProducer<String, T> sinkUpdater;

    public StoreProcessorValuesUpdater(final StoreUpdaterProcessor<T, T, T> valuesUpdater,
                                       final String topicStoreName,
                                       final String topicName,
                                       final String sinkStoreTopic,
                                       final String bootStrapServers, final Class<T> serializeCls) {
        this.valuesUpdater = valuesUpdater;
        this.topicName = topicName;
        this.topicStoreName = topicStoreName;
        this.sinkStoreTopic = sinkStoreTopic;
        this.sinkUpdater = Utilities.getTopicProducer(sinkStoreTopic, bootStrapServers);
        this.sinkReader = Utilities.getTopicReader(sinkStoreTopic, bootStrapServers, serializeCls);

    }

    private T readLastValue(Record<String, T> recordToProcess){
        T lastValue = null;

        if (recordToProcess != null) {
            lastValue = this.topicStore.get(recordToProcess.key());
            if (lastValue != null) {
                return lastValue;
            }
        }

        // This only happens when another process starts, so as to prime the global store
        try{
            Map<String, List<PartitionInfo>> topics = this.sinkReader.listTopics();
            List<PartitionInfo> partitions = topics.get(sinkStoreTopic);

            if (partitions != null) {
                int lastPartition = partitions.size() - 1;
                TopicPartition topicPartition = new TopicPartition(sinkStoreTopic, lastPartition);
                this.sinkReader.assign(Collections.singletonList(topicPartition));
                this.sinkReader.seekToEnd(Collections.singletonList(topicPartition));
                long lastOffset = this.sinkReader.position(topicPartition);
                this.sinkReader.seek(topicPartition, lastOffset-1);


                ConsumerRecords<String, T> records = this.sinkReader.poll(Duration.ofMillis(1000));

                T lastRecord = null;
                for (ConsumerRecord<String, T> r: records){
                    lastRecord = r.value();
                }

                return lastRecord;
            }
        }
        catch (Exception e) {
        // TODO
        }
        return null;

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
            this.sinkUpdater.produceSync(sinkStoreTopic, updatedValue);
            this.topicStore.put(recordToProcess.key(), updatedValue);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        this.context.commit();
    }

}
