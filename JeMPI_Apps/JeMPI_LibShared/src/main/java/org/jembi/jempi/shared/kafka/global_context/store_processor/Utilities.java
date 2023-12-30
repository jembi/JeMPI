package org.jembi.jempi.shared.kafka.global_context.store_processor;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.jembi.jempi.shared.kafka.MyKafkaProducer;
import org.jembi.jempi.shared.kafka.global_context.store_processor.serde.StoreValueDeserializer;
import org.jembi.jempi.shared.kafka.global_context.store_processor.serde.StoreValueSerializer;

import java.util.Properties;
import java.util.UUID;

public class Utilities {

    protected Utilities() { }

    public record TopicStoreNames(String topicName, String topicSinkName) { }
    public static final String JEMPI_GLOBAL_STORE_PREFIX = "jempi-global-store-topic";

    private static String getTopicWithPrefix(final String topicName) {
        return String.format("%s-%s", JEMPI_GLOBAL_STORE_PREFIX, topicName);
    }
    public static TopicStoreNames getStoreNames(final String topicName) {
        String topicNameWithPrefix = Utilities.getTopicWithPrefix(topicName);
        return new TopicStoreNames(topicNameWithPrefix, String.format("%s-sink", topicNameWithPrefix));
    }
    public static String getUniqueAppId(final String topicName, final String idSuffix) {
        return String.format("jempi-global-store-app-%s-%s", topicName, idSuffix == null ? UUID.randomUUID() : idSuffix);
    }

    public static <T> MyKafkaProducer<String, T> getTopicProducer(final String topicName, final String bootStrapServers) {
        return new MyKafkaProducer<>(bootStrapServers,
                topicName,
                new StringSerializer(),
                new StoreValueSerializer<>(),
                String.format("%s-producer", Utilities.getUniqueAppId(topicName, null)));
    }

    public static <T> Consumer<String, T> getTopicReader(final String topicName, final String bootStrapServers,  final Class<T> serializeCls) {
        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootStrapServers);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, String.format("%s-group", topicName));

        return new KafkaConsumer<>(properties, new StringDeserializer(), new StoreValueDeserializer<>(serializeCls));
    }
}
