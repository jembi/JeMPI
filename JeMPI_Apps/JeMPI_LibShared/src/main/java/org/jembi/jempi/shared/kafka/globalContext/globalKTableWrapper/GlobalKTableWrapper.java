package org.jembi.jempi.shared.kafka.globalContext.globalKTableWrapper;

import org.jembi.jempi.shared.kafka.KafkaTopicManager;

import java.util.HashMap;


public class GlobalKTableWrapper {

    private static final HashMap<String, GlobalKTableWrapperInstance> tables = new HashMap<>();
    private final KafkaTopicManager topicManager;
    public GlobalKTableWrapper(final String bootStrapServers) {
        topicManager = new KafkaTopicManager(bootStrapServers);
    }
    public GlobalKTableWrapperInstance getCreate(final String name){
        if (!topicManager.hasTopic(name)){
            topicManager.createTopic(name, 0, 1.0 );
        }
        return get(name);
    }
    public GlobalKTableWrapperInstance get(final String name){
        if (!GlobalKTableWrapper.tables.containsKey(name)){
            GlobalKTableWrapper.tables.put(name, new GlobalKTableWrapperInstance(name));
        }
        return  GlobalKTableWrapper.tables.get(name);
    }
}
