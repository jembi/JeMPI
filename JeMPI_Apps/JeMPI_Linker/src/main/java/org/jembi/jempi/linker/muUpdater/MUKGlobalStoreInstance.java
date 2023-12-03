package org.jembi.jempi.linker.muUpdater;

import org.jembi.jempi.shared.kafka.globalContext.globalKTableWrapper.GlobalKTableWrapperInstance;
import java.util.concurrent.ExecutionException;

public class MUKGlobalStoreInstance extends GlobalKTableWrapperInstance<FieldPairEqualityMatrix> {

    public MUKGlobalStoreInstance(String bootStrapServers, String topicName, Class<FieldPairEqualityMatrix> serializeCls) throws InterruptedException, ExecutionException {
        super(bootStrapServers, topicName, serializeCls);
    }
}
