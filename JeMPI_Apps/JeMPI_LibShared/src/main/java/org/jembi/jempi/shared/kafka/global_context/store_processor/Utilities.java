package org.jembi.jempi.shared.kafka.global_context.store_processor;

import java.util.UUID;

public class Utilities {

    public record TopicStoreNames (String topicName, String topicSinkName) { }
    public static final String JEMPI_GLOBAL_STORE_PREFIX = "jempi-global-store-topic";

    private static String getTopicWithPrefix(String topicName) {
        return String.format("%s-%s", JEMPI_GLOBAL_STORE_PREFIX, topicName);
    }
    public static TopicStoreNames getStoreNames(String topicName){
        String topicNameWithPrefix = Utilities.getTopicWithPrefix(topicName);
        return new TopicStoreNames(topicNameWithPrefix, String.format("%s-sink", topicNameWithPrefix));
    }
    public static String getUniqueAppId(final String topicName) {
        return String.format("jempi-global-store-app-%s-%s", topicName, UUID.randomUUID());
    }
}
