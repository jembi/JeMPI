package org.jembi.jempi.bootstrapper.data.stream.kafka;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;

public class KafkaBootstrapConfig {

   public HashMap<String, BootstrapperTopicConfig> topics;

   public HashMap<String, BootstrapperTopicConfig> getTopics() {
      return topics;
   }

   public static class BootstrapperTopicConfig {
      private String topicName;
      private Integer partition;
      private short replications;
      @JsonProperty("retention_ms")
      private Integer retentionMs;

      @JsonProperty("segments_bytes")
      private Integer segmentsBytes;

      public Integer getPartition() {
         return partition;
      }

      public short getReplications() {
         return replications;
      }

      public Integer getRetentionMs() {
         return retentionMs;
      }

      public Integer getSegmentsBytes() {
         return segmentsBytes;
      }

      public String getTopicName() {
         return topicName;
      }
   }
}
