package org.jembi.jempi.linker.threshold_range_processor.lib.mu_lib;

import org.jembi.jempi.shared.kafka.globalContext.globalKTableWrapper.GlobalKTableWrapper;
import org.jembi.jempi.shared.kafka.globalContext.globalKTableWrapper.GlobalKTableWrapperInstance;

import java.util.concurrent.ExecutionException;

public class MUKGlobalStoreFactory extends GlobalKTableWrapper {
    public MUKGlobalStoreFactory(String bootStrapServers) {
        super(bootStrapServers);
    }

    @Override
    protected <T> GlobalKTableWrapperInstance<T> getInstanceClass(String name, Class<T> serializeCls) throws ExecutionException, InterruptedException {
        return (GlobalKTableWrapperInstance<T>) new MUKGlobalStoreInstance(this.bootStrapServers, name, Object.class );
    }
}
