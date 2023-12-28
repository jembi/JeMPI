package org.jembi.jempi.bootstrapper.data.stream.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.admin.DeleteTopicsOptions;
import org.apache.kafka.clients.admin.ListTopicsOptions;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.clients.admin.TopicListing;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.bootstrapper.data.DataBootstrapper;
import org.jembi.jempi.bootstrapper.data.utils.DataBootstraperConsts;
import org.jembi.jempi.bootstrapper.utils.BootstrapperLogger;
import org.jembi.jempi.shared.kafka.KafkaTopicManager;
import org.jembi.jempi.shared.kafka.global_context.store_processor.Utilities;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;

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

        this.kafkaBootstrapConfig =  objectMapper.readValue(keycloakConfigStream, KafkaBootstrapConfig.class);
    }

    @Override
    public Boolean createSchema() throws InterruptedException {
        LOGGER.info("Loading Kafka schema data.");
        for (HashMap.Entry<String, KafkaBootstrapConfig.BootstrapperTopicConfig> topicDetails : this.kafkaBootstrapConfig.topics.entrySet()) {
            KafkaBootstrapConfig.BootstrapperTopicConfig topic = topicDetails.getValue();

            LOGGER.info(String.format("--> Creating topic '%s'", topic.getTopicName()));
            try {
                kafkaTopicManager.createTopic(topic.getTopicName(),
                        topic.getPartition(),
                        topic.getReplications(),
                        topic.getRetention_ms(),
                        topic.getSegments_bytes());
            } catch (ExecutionException e) {
                LOGGER.warn(e.getMessage());
            }
        }
        kafkaTopicManager.checkTopicsWithWait(topics -> topics.size() >=  this.kafkaBootstrapConfig.topics.size(), 5000);
        return true;
    }

    public Boolean listTopics() throws ExecutionException, InterruptedException {
        for (TopicListing t: kafkaTopicManager.getAllTopics()) {
            System.out.println(t.toString());
        };
        return true;
    }

    public Boolean describeTopic(final String topicName) throws ExecutionException, InterruptedException {
        for (Map.Entry<String, TopicDescription> t: kafkaTopicManager.describeTopic(topicName).entrySet()) {
            System.out.println(t.getValue().toString());
        };
        return true;
    }

    private void doTopicDelete(final String topicName) {
        LOGGER.info(String.format("--> Deleting topic '%s'", topicName));
        try {
            kafkaTopicManager.deleteTopic(topicName);
        } catch (ExecutionException | InterruptedException e) {
            LOGGER.warn(e.getMessage());
        }
    }

    public Boolean deleteGlobalStoreTopicsData() throws ExecutionException, InterruptedException {
        LOGGER.info("Deleting global store kafka topics.");
        Collection<String> collection = kafkaTopicManager.getAllTopics().stream()
                .map(TopicListing::name)
                .filter(name -> name.startsWith(Utilities.JEMPI_GLOBAL_STORE_PREFIX))
                .collect(Collectors.toCollection(ArrayList::new));

        for (String topic: collection) {
            doTopicDelete(topic);
        }

        kafkaTopicManager.checkTopicsWithWait(topics -> topics.stream().filter(t -> t.name().startsWith(Utilities.JEMPI_GLOBAL_STORE_PREFIX)).count() == 0, 5000);
        return true;
    }

    @Override
    public Boolean deleteData() throws InterruptedException, ExecutionException {
        LOGGER.info("Deleting kafka topics.");
        for (HashMap.Entry<String, KafkaBootstrapConfig.BootstrapperTopicConfig> topicDetails : this.kafkaBootstrapConfig.topics.entrySet()) {
            KafkaBootstrapConfig.BootstrapperTopicConfig topic = topicDetails.getValue();
            doTopicDelete(topic.getTopicName());
        }
        deleteGlobalStoreTopicsData();
        kafkaTopicManager.checkTopicsWithWait(topics -> topics.size() == 0, 5000);
        return true;
    }

    @Override
    public Boolean resetAll() throws ExecutionException, InterruptedException {
        LOGGER.info("Resetting kafka data and schemas.");
        return this.deleteData() && this.createSchema();
    }
}
