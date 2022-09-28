package org.jembi.jempi.shared.kafka;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.AppConfig;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

public class MyKafkaConsumerByPartition<KEY_TYPE, VAL_TYPE> {

    private static final Logger LOGGER = LogManager.getLogger(MyKafkaConsumerByPartition.class);
    private final String topic;
    private Consumer<KEY_TYPE, VAL_TYPE> consumer;

    public MyKafkaConsumerByPartition(final String topic,
                                      final Deserializer<KEY_TYPE> keyDeserializer,
                                      final Deserializer<VAL_TYPE> valueDeserializer,
                                      final String clientId,
                                      final String groupId,
                                      final int maxPollRecords,
                                      final int fetchMaxWaitMSConfig) {
        this.topic = topic;
        final Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, AppConfig.KAFKA_BOOTSTRAP_SERVERS);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.put(ConsumerConfig.CLIENT_ID_CONFIG, clientId);
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 1800000);
        properties.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecords);
        properties.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, fetchMaxWaitMSConfig);
        try {
            consumer = new KafkaConsumer<>(properties, keyDeserializer, valueDeserializer);
            List<TopicPartition> partitions = new ArrayList<>();
            List<PartitionInfo> partitionInfos = consumer.partitionsFor(topic);
            if (partitionInfos != null) {
                for (PartitionInfo partition : partitionInfos) {
                    partitions.add(new TopicPartition(partition.topic(), partition.partition()));
                }
                consumer.assign(partitions);
            } else {
                consumer = null;
            }
        } catch (Exception ex) {
            LOGGER.error(ex.getLocalizedMessage(), ex);
            consumer = null;
        }
    }

    public ConsumerRecords<KEY_TYPE, VAL_TYPE> poll(Duration duration) {
        return consumer.poll(duration);
    }

    public Map<Integer, Long> getEndingOffsets() {
        LOGGER.debug("getEndingOffsets: {}", topic);
        Map<Integer, Long> retValue = new HashMap<>();

        Map<String, List<PartitionInfo>> topics = consumer.listTopics();
        List<PartitionInfo> partitionInfos = topics.get(topic);
        if (partitionInfos == null) {
            LOGGER.warn("Partition information was not found for topic {}", topic);
        } else {
            Collection<TopicPartition> partitions = new ArrayList<>();
            for (PartitionInfo partitionInfo : partitionInfos) {
                partitions.add(new TopicPartition(topic, partitionInfo.partition()));
            }
            Map<TopicPartition, Long> endingOffsets = consumer.endOffsets(partitions);
            for (TopicPartition partition : endingOffsets.keySet()) {
                retValue.put(partition.partition(), endingOffsets.get(partition));
            }
        }

        LOGGER.debug("getEndingOffsets: {}", retValue);
        return retValue;
    }

    public void setOffset(Integer partition, Long offset) {
        TopicPartition tp = new TopicPartition(topic, partition);
        LOGGER.info("Set offset {} {}", tp, offset);
        // Get topic partitions
        List<TopicPartition> partitions = consumer
                .partitionsFor(topic)
                .stream()
                .map(partitionInfo -> new TopicPartition(topic, partitionInfo.partition()))
                .collect(Collectors.toList());
        // Explicitly assign the partitions to our consumer
        consumer.assign(partitions);
        //seek, query offsets, or poll
        consumer.seek(tp, offset);
    }

    public final void close() {
        consumer.close();
    }

}
