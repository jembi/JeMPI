package org.jembi.jempi.shared.libs.interactionProcessor.processors.tptn;

import org.jembi.jempi.shared.kafka.global_context.store_processor.StoreProcessorFactory;
import java.util.concurrent.ExecutionException;

public final class TPTNKGlobalStoreFactory extends StoreProcessorFactory<TPTNMatrix>  {
    public TPTNKGlobalStoreFactory(final String bootStrapServers) {
        super(bootStrapServers);
    }

    @Override
    protected TPTNKGlobalStoreInstance getInstanceClass(final String name, final String sinkName, final Class<TPTNMatrix> serializeCls) throws ExecutionException, InterruptedException {
        return  new TPTNKGlobalStoreInstance(this.bootStrapServers, name, sinkName, TPTNMatrix.class);
    }
}
