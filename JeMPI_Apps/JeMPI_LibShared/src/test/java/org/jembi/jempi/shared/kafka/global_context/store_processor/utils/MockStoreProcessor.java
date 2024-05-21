package org.jembi.jempi.shared.kafka.global_context.store_processor.utils;

import org.jembi.jempi.shared.kafka.global_context.store_processor.StoreProcessor;
import org.jembi.jempi.shared.kafka.global_context.store_processor.StoreUpdaterProcessor;

import java.util.concurrent.ExecutionException;

public class MockStoreProcessor extends StoreProcessor<TestUtils.MockTableData> {
    public MockStoreProcessor(String bootStrapServers, String topicName, String sinkName, Class<TestUtils.MockTableData> serializeCls) throws InterruptedException, ExecutionException {
        super(bootStrapServers, topicName, sinkName, serializeCls);
    }
    @Override
    protected StoreUpdaterProcessor<TestUtils.MockTableData, TestUtils.MockTableData, TestUtils.MockTableData> getValueUpdater(){
        return (TestUtils.MockTableData globalValue,TestUtils.MockTableData currentValue) -> {
            TestUtils.MockTableData updateMockTable = new TestUtils.MockTableData();
            updateMockTable.totalValues = globalValue == null ? currentValue.totalValues :  globalValue.totalValues + currentValue.totalValues;
            return updateMockTable;
        };
    }
}
