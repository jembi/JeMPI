package org.jembi.jempi.linker.thresholdRangeProcessor.lib.muLib;

import org.jembi.jempi.shared.kafka.globalContext.globalKTableWrapper.GlobalKTableWrapperInstance;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class MUKGlobalStoreInstance extends GlobalKTableWrapperInstance<Object> {

    public MUKGlobalStoreInstance(String bootStrapServers, String topicName, Class<Object> serializeCls) throws InterruptedException, ExecutionException {
        super(bootStrapServers, topicName, serializeCls);
    }
}
