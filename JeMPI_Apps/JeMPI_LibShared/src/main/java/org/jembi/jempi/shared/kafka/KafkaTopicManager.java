package org.jembi.jempi.shared.kafka;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DeleteTopicsOptions;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.streams.StreamsConfig;

import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

public final class KafkaTopicManager {

   private final AdminClient adminClient;

   public KafkaTopicManager(final String bootStrapServers) {
      Properties properties = new Properties();
      properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootStrapServers);
      adminClient = AdminClient.create(properties);
   }

   public void createTopic(
         final String topicName,
         final int partitions,
         final short replicationFactor) throws ExecutionException, InterruptedException {
      NewTopic newTopic = new NewTopic(topicName, partitions, replicationFactor);
      newTopic.configs(Collections.singletonMap(TopicConfig.RETENTION_MS_CONFIG, "86400000"));
      KafkaFuture<Void> createFuture = adminClient.createTopics(Collections.singleton(newTopic)).all();
      createFuture.get(); // Wait for the topic creation to complete
   }

   public void deleteTopic(final String topicName) throws ExecutionException, InterruptedException {
      KafkaFuture<Void> deleteFuture =
            adminClient.deleteTopics(Collections.singleton(topicName), new DeleteTopicsOptions()).all();
      deleteFuture.get(); // Wait for the topic deletion to complete
   }

}

