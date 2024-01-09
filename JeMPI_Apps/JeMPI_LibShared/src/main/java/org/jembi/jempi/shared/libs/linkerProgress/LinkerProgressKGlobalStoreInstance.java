package org.jembi.jempi.shared.libs.linkerProgress;

import org.jembi.jempi.shared.kafka.global_context.store_processor.StoreProcessor;
import org.jembi.jempi.shared.kafka.global_context.store_processor.StoreUpdaterProcessor;

import java.util.Objects;
import java.util.concurrent.ExecutionException;

public final class LinkerProgressKGlobalStoreInstance extends StoreProcessor<LinkerProgressData> {

    public LinkerProgressKGlobalStoreInstance(final String bootStrapServers, final String topicName, final String sinkName, final Class<LinkerProgressData> serializeCls) throws InterruptedException, ExecutionException {
        super(bootStrapServers, topicName, sinkName, serializeCls);
    }

    @Override
    protected StoreUpdaterProcessor<LinkerProgressData, LinkerProgressData, LinkerProgressData> getValueUpdater() {
        return (LinkerProgressData globalValue, final LinkerProgressData currentValue) -> {

            if (globalValue == null) {
                globalValue = new LinkerProgressData(0, 0, 0, null);
            }

            if (!Objects.equals(currentValue.fileName(), globalValue.fileName())) {
                return currentValue;
            }

            return new LinkerProgressData(currentValue.interactionCount() + globalValue.interactionCount(),
                                                currentValue.interactionSize() + globalValue.interactionSize(),
                                                            currentValue.fileSize(),
                                                            currentValue.fileName());
        };
    }
}
