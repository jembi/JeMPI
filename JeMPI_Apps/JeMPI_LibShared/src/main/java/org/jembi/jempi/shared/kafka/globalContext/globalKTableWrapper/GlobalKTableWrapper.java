package org.jembi.jempi.shared.kafka.globalContext.globalKTableWrapper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.shared.kafka.KafkaTopicManager;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class GlobalKTableWrapper {
    private static final Logger LOGGER = LogManager.getLogger(GlobalKTableWrapper.class);
    private static final HashMap<String, GlobalKTableWrapperInstance> tables = new HashMap<>();
    private final KafkaTopicManager topicManager;
    private final String bootStrapServers;

    public GlobalKTableWrapper(final String bootStrapServers) {
        topicManager = new KafkaTopicManager(bootStrapServers);
        this.bootStrapServers = bootStrapServers;
    }
    public <T> GlobalKTableWrapperInstance<T> getCreate(final String name) throws ExecutionException, InterruptedException {
        if (!topicManager.hasTopic(name)){
            topicManager.createTopic(name,
                            1,
                                    (short) 1,
                        86400000,
                        4194304);
        }
        return get(name);
    }
    public <T> GlobalKTableWrapperInstance<T> get(final String name){
        if (!GlobalKTableWrapper.tables.containsKey(name)){
            GlobalKTableWrapperInstance<T> instance;
            try{
                instance = new GlobalKTableWrapperInstance<T>(name, bootStrapServers);
            } catch (Exception e){
                LOGGER.error(String.format("Failed to create global kTable with the name %s", name), e);
                throw e;
            }
            GlobalKTableWrapper.tables.put(name, instance);
        }
        return GlobalKTableWrapper.tables.get(name);
    }
}
