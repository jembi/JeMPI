package org.jembi.jempi.shared.kafka.global_context.store_processor.utils;

import org.jembi.jempi.shared.kafka.global_context.store_processor.StoreProcessor;
import org.jembi.jempi.shared.kafka.global_context.store_processor.StoreProcessorFactory;

import java.util.concurrent.ExecutionException;

public class MockStoreProcessorFactor extends StoreProcessorFactory<TestUtils.MockTableData> {
    public MockStoreProcessorFactor(String bootStrapServers) {
        super(bootStrapServers);
    }
    @Override
    protected StoreProcessor<TestUtils.MockTableData> getInstanceClass(final String name, final String sinkName, Class<TestUtils.MockTableData> serializeCls) throws ExecutionException, InterruptedException {
        return new MockStoreProcessor(bootStrapServers, name, sinkName, serializeCls);
    }
}
