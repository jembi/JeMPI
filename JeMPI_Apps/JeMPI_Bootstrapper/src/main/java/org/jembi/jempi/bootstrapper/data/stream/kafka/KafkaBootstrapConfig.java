package org.jembi.jempi.bootstrapper.data.stream.kafka;

import java.util.HashMap;

public class KafkaBootstrapConfig {

    public static class BootstrapperTopicConfig {
        private String topicName;
        private Integer partition;
        private short replications;
        private Integer retention_ms;
        private Integer segments_bytes;

        public Integer getPartition() {
            return partition;
        }

        public short getReplications() {
            return replications;
        }

        public Integer getRetention_ms() {
            return retention_ms;
        }

        public Integer getSegments_bytes() {
            return segments_bytes;
        }

        public String getTopicName() {
            return topicName;
        }
    }
    public HashMap<String, BootstrapperTopicConfig> topics;

    public HashMap<String, BootstrapperTopicConfig> getTopics() {
        return topics;
    }
}
