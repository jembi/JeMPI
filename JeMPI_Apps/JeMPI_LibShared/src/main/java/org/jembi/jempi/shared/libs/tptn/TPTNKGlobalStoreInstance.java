package org.jembi.jempi.shared.libs.tptn;

import org.jembi.jempi.shared.kafka.global_context.store_processor.StoreProcessor;
import org.jembi.jempi.shared.kafka.global_context.store_processor.StoreUpdaterProcessor;
import java.util.concurrent.ExecutionException;

public final class TPTNKGlobalStoreInstance extends StoreProcessor<TPTNMatrix> {

    public TPTNKGlobalStoreInstance(final String bootStrapServers, final String topicName, final String sinkName, final Class<TPTNMatrix> serializeCls) throws InterruptedException, ExecutionException {
        super(bootStrapServers, topicName, sinkName, serializeCls);
    }

    public TPTNMatrix getValue() {
        TPTNMatrix storedValue = super.getValue();

        if (storedValue == null) {
            return new TPTNMatrix();
        }
        return storedValue;
    }

    @Override
    protected StoreUpdaterProcessor<TPTNMatrix, TPTNMatrix, TPTNMatrix> getValueUpdater() {
        return (TPTNMatrix globalValue, final TPTNMatrix currentValue) -> {

            if (globalValue == null) {
                globalValue = new TPTNMatrix();
            }

            return globalValue.merge(globalValue, currentValue);
        };
    }
}
