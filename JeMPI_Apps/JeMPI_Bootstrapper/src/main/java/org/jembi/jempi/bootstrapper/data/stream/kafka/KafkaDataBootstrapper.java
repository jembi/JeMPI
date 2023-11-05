package org.jembi.jempi.bootstrapper.data.stream.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

public class KafkaDataBootstrapper extends DataBootstrapper {
   protected static final Logger LOGGER = BootstrapperLogger.getChildLogger(DataBootstrapper.LOGGER, "Kafka");
   protected KafkaBootstrapConfig kafkaBootstrapConfig;
   protected KafkaTopicManager kafkaTopicManager;

    public KafkaDataBootstrapper(String configFilePath) throws IOException {
        super(configFilePath);
        this.LoadKafkaConfig();
        this.LoadKafkaTopicManager();
    }
    protected void LoadKafkaTopicManager(){
        LOGGER.info(String.format("Connecting to the kafka bootstrap server '%s'", this.loadedConfig.KAFKA_BOOTSTRAP_SERVERS ));
        kafkaTopicManager = new KafkaTopicManager(this.loadedConfig.KAFKA_BOOTSTRAP_SERVERS);
    }
    protected void LoadKafkaConfig() throws IOException {
        InputStream keycloakConfigStream = this.getClass().getResourceAsStream(DataBootstraperConsts.JSON_CONFIG_FILE_NAME);
        ObjectMapper objectMapper = new ObjectMapper();

        this.kafkaBootstrapConfig =  objectMapper.readValue(keycloakConfigStream, KafkaBootstrapConfig.class);
    }

    private void awaitOperationComplete(Function<Collection<TopicListing>, Boolean> CheckFunc){
        boolean isComplete = false;
        int count = 0;
        while(!isComplete){
            try{
                Thread.sleep(1000);
                isComplete = CheckFunc.apply(kafkaTopicManager.getAllTopics()) || count > 30000;
                count += 1000;
            } catch (ExecutionException | InterruptedException e){
                isComplete = true;
            }
        }
    }
    @Override
    public Boolean createSchema() throws ExecutionException, InterruptedException {
        LOGGER.info("Loading Kafka schema data.");
        for (HashMap.Entry<String, KafkaBootstrapConfig.BootstrapperTopicConfig> topicDetails : this.kafkaBootstrapConfig.topics.entrySet()) {
            KafkaBootstrapConfig.BootstrapperTopicConfig topic = topicDetails.getValue();

            LOGGER.info(String.format("--> Creating topic '%s'", topic.getTopicName()));
            try{
                kafkaTopicManager.createTopic(topic.getTopicName(),
                        topic.getPartition(),
                        topic.getReplications(),
                        topic.getRetention_ms(),
                        topic.getSegments_bytes());
            } catch (ExecutionException e){
                LOGGER.warn(e.getMessage());
            }
        }
        awaitOperationComplete(topics -> topics.size() >=  this.kafkaBootstrapConfig.topics.size());
        return true;
    }

    @Override
    public Boolean deleteData() throws InterruptedException {
        LOGGER.info("Deleting kafka topics.");
        for (HashMap.Entry<String, KafkaBootstrapConfig.BootstrapperTopicConfig> topicDetails : this.kafkaBootstrapConfig.topics.entrySet()) {

            KafkaBootstrapConfig.BootstrapperTopicConfig topic = topicDetails.getValue();
            LOGGER.info(String.format("--> Deleting topic '%s'", topic.getTopicName()));
            try{
                kafkaTopicManager.deleteTopic(topic.getTopicName());
            } catch (ExecutionException e){
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
