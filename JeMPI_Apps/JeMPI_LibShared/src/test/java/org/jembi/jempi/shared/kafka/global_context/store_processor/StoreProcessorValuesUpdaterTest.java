package org.jembi.jempi.shared.kafka.global_context.store_processor;

import org.jembi.jempi.shared.kafka.global_context.store_processor.utils.TestUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class StoreProcessorValuesUpdaterTest {
    TestUtils testUtils;

    @BeforeAll
    void prepareForTests(){
        testUtils = new TestUtils("localhost:9097");
    }

    @Test
    void itCanHaveCustomValueUpdater() throws ExecutionException, InterruptedException {
        StoreProcessor<TestUtils.MockTableData> processor = testUtils.getMockStoreProcessor(true);

        TestUtils.MockTableData updateInstance = new TestUtils.MockTableData();
        updateInstance.totalValues = 1;

        processor.updateValue(updateInstance);
        Thread.sleep(2000);

        assertEquals(1, processor.getValue().getTotalValues());

        processor.updateValue(updateInstance);
        Thread.sleep(2000);

        processor.updateValue(updateInstance);
        Thread.sleep(2000);

        assertEquals(3, processor.getValue().getTotalValues());
    }
    @Test
    void itKeepsTrackOfAggregatedData() throws ExecutionException, InterruptedException {

        StoreProcessor<TestUtils.MockTableData> processor = testUtils.getMockStoreProcessor(true);

        TestUtils.MockTableData updateInstance = new TestUtils.MockTableData();
        updateInstance.totalValues = 1;

        processor.updateValue(updateInstance);
        Thread.sleep(2000);

        assertEquals(1, processor.getValue().getTotalValues());

        processor.updateValue(updateInstance);
        Thread.sleep(2000);

        processor.updateValue(updateInstance);
        Thread.sleep(2000);

        assertEquals(3, processor.getValue().getTotalValues());

        StoreProcessor<TestUtils.MockTableData> processorNew = testUtils.getMockStoreProcessor(false);
        assertEquals(3, processorNew.getValue().getTotalValues());

        processorNew.updateValue(updateInstance);
        Thread.sleep(2000);

        assertEquals(4, processorNew.getValue().getTotalValues());
        assertEquals(4, processor.getValue().getTotalValues());

    }
}
