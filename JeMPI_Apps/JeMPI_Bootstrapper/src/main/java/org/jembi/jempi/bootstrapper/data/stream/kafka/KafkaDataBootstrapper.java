package org.jembi.jempi.bootstrapper.data.stream.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.clients.admin.TopicListing;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.bootstrapper.data.DataBootstrapper;
import org.jembi.jempi.bootstrapper.data.utils.DataBootstraperConsts;
import org.jembi.jempi.bootstrapper.utils.BootstrapperLogger;
import org.jembi.jempi.shared.kafka.KafkaTopicManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

public class KafkaDataBootstrapper extends DataBootstrapper {
   protected static final Logger LOGGER = BootstrapperLogger.getChildLogger(DataBootstrapper.LOGGER, "Kafka");
   protected KafkaBootstrapConfig kafkaBootstrapConfig;
   protected KafkaTopicManager kafkaTopicManager;

   public KafkaDataBootstrapper(final String configFilePath) throws IOException {
      super(configFilePath);
      this.loadKafkaConfig();
      this.loadKafkaTopicManager();
   }

   protected void loadKafkaTopicManager() {
      LOGGER.info(String.format("Connecting to the kafka bootstrap server '%s'", this.loadedConfig.KAFKA_BOOTSTRAP_SERVERS));
      kafkaTopicManager = new KafkaTopicManager(this.loadedConfig.KAFKA_BOOTSTRAP_SERVERS);
   }

   protected void loadKafkaConfig() throws IOException {
      InputStream keycloakConfigStream = this.getClass().getResourceAsStream(DataBootstraperConsts.KAFKA_BOOT_STRAP_CONFIG_JSON);
      ObjectMapper objectMapper = new ObjectMapper();

      this.kafkaBootstrapConfig = objectMapper.readValue(keycloakConfigStream, KafkaBootstrapConfig.class);
   }

   private void awaitOperationComplete(final Function<Collection<TopicListing>, Boolean> checkFunc) {
      boolean isComplete = false;
      int count = 0;
      while (!isComplete) {
         try {
            Thread.sleep(1000);
            isComplete = checkFunc.apply(kafkaTopicManager.getAllTopics()) || count > 5000;
            count += 1000;
         } catch (ExecutionException | InterruptedException e) {
            isComplete = true;
         }
      }
   }

   @Override
   public Boolean createSchema() throws InterruptedException {
      LOGGER.info("Loading Kafka schema data.");
      for (HashMap.Entry<String, KafkaBootstrapConfig.BootstrapperTopicConfig> topicDetails
              : this.kafkaBootstrapConfig.topics.entrySet()) {
         KafkaBootstrapConfig.BootstrapperTopicConfig topic = topicDetails.getValue();

         LOGGER.info(String.format("--> Creating topic '%s'", topic.getTopicName()));
         try {
            kafkaTopicManager.createTopic(topic.getTopicName(),
                                          topic.getPartition(),
                                          topic.getReplications(),
                                          topic.getRetentionMs(),
                                          topic.getSegmentsBytes());
         } catch (ExecutionException e) {
            LOGGER.warn(e.getMessage());
         }
      }
      awaitOperationComplete(topics -> topics.size() >= this.kafkaBootstrapConfig.topics.size());
      return true;
   }

   public Boolean listTopics() throws ExecutionException, InterruptedException {
      for (TopicListing t : kafkaTopicManager.getAllTopics()) {
         System.out.println(t.toString());
      }
      return true;
   }

   public Boolean describeTopic(final String topicName) throws ExecutionException, InterruptedException {
      for (Map.Entry<String, TopicDescription> t : kafkaTopicManager.describeTopic(topicName).entrySet()) {
         System.out.println(t.getValue().toString());
      }
      return true;
   }

   @Override
   public Boolean deleteData() throws InterruptedException {
      LOGGER.info("Deleting kafka topics.");
      for (HashMap.Entry<String, KafkaBootstrapConfig.BootstrapperTopicConfig> topicDetails
              : this.kafkaBootstrapConfig.topics.entrySet()) {

         KafkaBootstrapConfig.BootstrapperTopicConfig topic = topicDetails.getValue();
         LOGGER.info(String.format("--> Deleting topic '%s'", topic.getTopicName()));
         try {
            kafkaTopicManager.deleteTopic(topic.getTopicName());
         } catch (ExecutionException e) {
            LOGGER.warn(e.getMessage());
         }
      }

      awaitOperationComplete(topics -> topics.size() == 0);
      return true;
   }

   @Override
   public Boolean resetAll() throws ExecutionException, InterruptedException {
      LOGGER.info("Resetting kafka data and schemas.");
      return this.deleteData() && this.createSchema();
   }
}
