package org.jembi.jempi.shared.kafka;

import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.common.errors.UnknownTopicOrPartitionException;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

public final class KafkaTopicManager {

   private static final Logger LOGGER = LogManager.getLogger(KafkaTopicManager.class);

   private final AdminClient adminClient;

   public KafkaTopicManager(final String bootStrapServers) {
      Properties properties = new Properties();
      properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootStrapServers);
      adminClient = AdminClient.create(properties);
   }

   public void close() {
      adminClient.close();
   }

   public Collection<TopicListing> getAllTopics() throws ExecutionException, InterruptedException {
      return adminClient.listTopics(new ListTopicsOptions().listInternal(false)).listings().get();
   }

   public Boolean hasTopic(final String name) throws ExecutionException, InterruptedException {
      return getAllTopics().stream().anyMatch(r -> r.name().equals(name));
   }

   public void checkTopicsWithWait(final Function<Collection<TopicListing>, Boolean> checkFunc, final Integer timeoutMs) {
      boolean isComplete = false;
      int count = 0;
      while (!isComplete) {
         try {
            Thread.sleep(200);
            isComplete = checkFunc.apply(this.getAllTopics()) || count > timeoutMs;
            count += 200;
         } catch (ExecutionException | InterruptedException e) {
            isComplete = true;
         }
      }
   }
   public Map<String, TopicDescription> describeTopic(final String topic) throws ExecutionException, InterruptedException {
      return adminClient.describeTopics(Collections.singletonList(topic)).allTopicNames().get();
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
      config.put(TopicConfig.SEGMENT_BYTES_CONFIG, Integer.toString(segments_bytes));

      newTopic.configs(config);
      KafkaFuture<Void> createFuture = adminClient.createTopics(Collections.singleton(newTopic)).all();
      createFuture.get();
   }

   public void deleteTopic(final String topicName) throws ExecutionException, InterruptedException {
      KafkaFuture<Void> deleteFuture =
            adminClient.deleteTopics(Collections.singleton(topicName), new DeleteTopicsOptions()).all();
      try {
         deleteFuture.get(); // Wait for the topic deletion to complete
      } catch (ExecutionException e) {
         if (!(e.getCause() instanceof UnknownTopicOrPartitionException)) {
            LOGGER.error(e.getLocalizedMessage(), e);
            throw (e);
         }
      }
   }

}
