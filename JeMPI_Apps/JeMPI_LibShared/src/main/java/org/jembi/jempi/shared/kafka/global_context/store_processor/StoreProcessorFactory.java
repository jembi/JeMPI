package org.jembi.jempi.shared.kafka.global_context.store_processor;

import org.apache.kafka.common.errors.TopicExistsException;
import org.apache.kafka.common.errors.UnknownTopicIdException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.shared.kafka.KafkaTopicManager;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class StoreProcessorFactory<T> {
    private static final Logger LOGGER = LogManager.getLogger(StoreProcessorFactory.class);
    private final HashMap<String, StoreProcessor<T>> tables = new HashMap<>();
    private final KafkaTopicManager topicManager;
    protected final String bootStrapServers;

    public StoreProcessorFactory(final String bootStrapServers) {
        topicManager = new KafkaTopicManager(bootStrapServers);
        this.bootStrapServers = bootStrapServers;
    }
    public StoreProcessor<T> getCreate(final String name, Class<T> serializeCls) throws TopicExistsException, ExecutionException, InterruptedException {
        if (Boolean.FALSE.equals(topicManager.hasTopic(name))){
            topicManager.createTopic(name,
                            1,
                            (short) 1,
                            86400000,
                            4194304);
        }
        return get(name, serializeCls);
    }
    public StoreProcessor<T> get(final String name, Class<T> serializeCls) throws TopicExistsException, ExecutionException, InterruptedException {
        if (Boolean.FALSE.equals(topicManager.hasTopic(name))){
            throw new UnknownTopicIdException(String.format("Could not find the global KTable with the name '%s'. Try running getCreate instead.", name));
        }
        if (!tables.containsKey(name)){
            StoreProcessor<T> instance;
            try{
                instance = getInstanceClass(name, serializeCls);
            } catch (Exception e){
                LOGGER.error(String.format("Failed to create global kTable with the name %s. Reason: %s", name, e.getMessage()), e);
                throw e;
            }
            tables.put(name, instance);
        }
        return tables.get(name);
    }

    protected StoreProcessor<T> getInstanceClass(final String name, Class<T> serializeCls) throws ExecutionException, InterruptedException {
        return new StoreProcessor<>(bootStrapServers, name, serializeCls);
    }
}
