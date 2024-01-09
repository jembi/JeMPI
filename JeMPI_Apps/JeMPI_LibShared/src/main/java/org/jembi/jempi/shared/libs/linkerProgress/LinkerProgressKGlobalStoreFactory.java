package org.jembi.jempi.shared.libs.linkerProgress;

import org.jembi.jempi.shared.kafka.global_context.store_processor.StoreProcessorFactory;

import java.util.concurrent.ExecutionException;

public final class LinkerProgressKGlobalStoreFactory extends StoreProcessorFactory<LinkerProgressData>  {
    public LinkerProgressKGlobalStoreFactory(final String bootStrapServers) {
        super(bootStrapServers);
    }

    @Override
    protected LinkerProgressKGlobalStoreInstance getInstanceClass(final String name, final String sinkName, final Class<LinkerProgressData> serializeCls) throws ExecutionException, InterruptedException {
        return  new LinkerProgressKGlobalStoreInstance(this.bootStrapServers, name, sinkName, LinkerProgressData.class);
    }
}
