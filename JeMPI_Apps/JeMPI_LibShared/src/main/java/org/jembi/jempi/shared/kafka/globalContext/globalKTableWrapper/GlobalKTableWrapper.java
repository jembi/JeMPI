package org.jembi.jempi.shared.kafka.globalContext.globalKTableWrapper;

import org.apache.kafka.common.errors.TopicExistsException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.shared.kafka.KafkaTopicManager;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class GlobalKTableWrapper {
    private static final Logger LOGGER = LogManager.getLogger(GlobalKTableWrapper.class);
    private final HashMap<String, GlobalKTableWrapperInstance> tables = new HashMap<>();
    private final KafkaTopicManager topicManager;
    protected final String bootStrapServers;

    public GlobalKTableWrapper(final String bootStrapServers) {
        topicManager = new KafkaTopicManager(bootStrapServers);
        this.bootStrapServers = bootStrapServers;
    }
    public <T> GlobalKTableWrapperInstance<T> getCreate(final String name, Class<T> serializeCls) throws TopicExistsException, ExecutionException, InterruptedException {
        if (!topicManager.hasTopic(name)){
            topicManager.createTopic(name,
                            1,
                            (short) 1,
                            86400000,
                            4194304);
        }
        return get(name, serializeCls);
    }
    public <T> GlobalKTableWrapperInstance<T> get(final String name, Class<T> serializeCls) throws TopicExistsException, ExecutionException, InterruptedException {
        if (!topicManager.hasTopic(name)){
            throw new TopicExistsException(String.format("Could not find the global KTable with the name '%s'. Try running getCreate instead.", name));
        }
        if (!tables.containsKey(name)){
            GlobalKTableWrapperInstance<T> instance;
            try{
                instance = getInstanceClass(name, serializeCls);
            } catch (Exception e){
                LOGGER.error(String.format("Failed to create global kTable with the name %s. Reason:", name, e.getMessage()), e);
                throw e;
            }
            tables.put(name, instance);
        }
        return tables.get(name);
    }

    protected <T> GlobalKTableWrapperInstance<T> getInstanceClass(final String name, Class<T> serializeCls) throws ExecutionException, InterruptedException {
        return new GlobalKTableWrapperInstance<T>(bootStrapServers, name, serializeCls);
    }
}
