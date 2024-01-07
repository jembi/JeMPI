package org.jembi.jempi.shared.libs.m_and_u;

import org.jembi.jempi.shared.kafka.global_context.store_processor.StoreProcessor;
import org.jembi.jempi.shared.kafka.global_context.store_processor.StoreProcessorFactory;

import java.util.concurrent.ExecutionException;

public final class MUKGlobalStoreFactory extends StoreProcessorFactory<Object> {
    public MUKGlobalStoreFactory(final String bootStrapServers) {
        super(bootStrapServers);
    }

    @Override
    protected StoreProcessor<Object> getInstanceClass(final String name, final String sinkName, final Class<Object> serializeCls) throws ExecutionException, InterruptedException {
        return  new MUKGlobalStoreInstance(this.bootStrapServers, name, sinkName, Object.class);
    }
}
