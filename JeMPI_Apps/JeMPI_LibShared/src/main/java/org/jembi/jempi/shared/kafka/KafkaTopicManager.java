package org.jembi.jempi.shared.kafka;

import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.streams.StreamsConfig;

import java.util.*;
import java.util.concurrent.ExecutionException;
public final class KafkaTopicManager {

   private final AdminClient adminClient;

   public KafkaTopicManager(final String bootStrapServers) {
      Properties properties = new Properties();
      properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootStrapServers);
      adminClient = AdminClient.create(properties);
   }

   public Collection<TopicListing> getAllTopics() throws ExecutionException, InterruptedException {
      return adminClient.listTopics(new ListTopicsOptions().listInternal(false)).listings().get();
   }

   public Boolean hasTopic(final String name) throws ExecutionException, InterruptedException {
      return getAllTopics().stream().anyMatch(r -> r.name().equals(name));
   }
   public Map<String, TopicDescription> describeTopic(final String topic) throws ExecutionException, InterruptedException {
      return adminClient.describeTopics(Arrays.asList(topic)).allTopicNames().get();
   }

   public void createTopic(
         final String topicName,
         final int partitions,
         final short replicationFactor,
         final int retention_ms,
         final int segments_bytes) throws ExecutionException, InterruptedException {
      NewTopic newTopic = new NewTopic(topicName, partitions, replicationFactor);

      HashMap<String, String> config = new HashMap<>();
      config.put(TopicConfig.RETENTION_MS_CONFIG, Integer.toString(retention_ms));
      config.put(TopicConfig.SEGMENT_BYTES_CONFIG,  Integer.toString(segments_bytes));

      newTopic.configs(config);
      KafkaFuture<Void> createFuture = adminClient.createTopics(Collections.singleton(newTopic)).all();
      createFuture.get();
   }

   public void deleteTopic(final String topicName) throws ExecutionException, InterruptedException {
      KafkaFuture<Void> deleteFuture =
            adminClient.deleteTopics(Collections.singleton(topicName), new DeleteTopicsOptions()).all();
      deleteFuture.get(); // Wait for the topic deletion to complete
   }

}

