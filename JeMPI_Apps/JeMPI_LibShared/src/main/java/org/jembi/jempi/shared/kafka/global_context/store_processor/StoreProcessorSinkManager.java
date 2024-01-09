package org.jembi.jempi.shared.kafka.global_context.store_processor;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.shared.kafka.MyKafkaProducer;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public final class StoreProcessorSinkManager<T> {
    private static final Logger LOGGER = LogManager.getLogger(StoreProcessorSinkManager.class);
    private final String topicName;
    private final String sinkTopicName;
    private final Consumer<String, T> sinkReader;
    private final MyKafkaProducer<String, T> sinkUpdater;

    public StoreProcessorSinkManager(final String topicName, final String sinkTopicName, final String bootStrapServers, final Class<T> serializeCls) {
        this.topicName = topicName;
        this.sinkTopicName = sinkTopicName;
        this.sinkUpdater = Utilities.getTopicProducer(sinkTopicName, bootStrapServers);
        this.sinkReader = Utilities.getTopicReader(sinkTopicName, bootStrapServers, serializeCls);
    }
    public void updateSink(final T updatedValue) throws ExecutionException, InterruptedException {
        this.sinkUpdater.produceSync(sinkTopicName, updatedValue);
    }

    public T readSink() {
        try {
            Map<String, List<PartitionInfo>> topics = this.sinkReader.listTopics();
            List<PartitionInfo> partitions = topics.get(sinkTopicName);

            if (partitions != null) {
                int lastPartition = partitions.size() - 1;
                TopicPartition topicPartition = new TopicPartition(sinkTopicName, lastPartition);
                this.sinkReader.assign(Collections.singletonList(topicPartition));
                this.sinkReader.seekToEnd(Collections.singletonList(topicPartition));
                long lastOffset = this.sinkReader.position(topicPartition);
                if (lastOffset == 0) {
                    return null;
                }
                this.sinkReader.seek(topicPartition, lastOffset - 1);


                ConsumerRecords<String, T> records = this.sinkReader.poll(Duration.ofMillis(1000));

                T lastRecord = null;
                for (ConsumerRecord<String, T> r: records) {
                    lastRecord = r.value();
                }

                return lastRecord;
            }
        } catch (Exception e) {
            LOGGER.error(String.format("An error occurred trying to get the global store %s last value. Defaulting to null", this.topicName), e);
        }
        return null;
    }
}
