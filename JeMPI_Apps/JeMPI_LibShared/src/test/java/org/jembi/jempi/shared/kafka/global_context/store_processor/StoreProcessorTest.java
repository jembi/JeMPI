package org.jembi.jempi.shared.kafka.global_context.store_processor;

import org.jembi.jempi.shared.kafka.global_context.store_processor.utils.TestUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.concurrent.ExecutionException;
import static org.junit.jupiter.api.Assertions.*;
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class StoreProcessorTest {
    TestUtils testUtils;

    @BeforeAll
    void prepareForTests(){
        testUtils = new TestUtils("localhost:9097");
    }

    @Test
    void testCanGetValue() throws ExecutionException, InterruptedException {
        StoreProcessor<TestUtils.MockTableData> kTableInstance = testUtils.<TestUtils.MockTableData>getGlobalKTableWrapperInstance(true).getCreate(testUtils.getTestTopicName("sample-topic"), TestUtils.MockTableData.class);
        Thread.sleep(2000);
        assertNull(kTableInstance.getValue());
    }
    @Test
    void testCanUpdateAndGetTableValue() throws ExecutionException, InterruptedException {
        StoreProcessor<TestUtils.MockTableData> kTableInstance = testUtils.<TestUtils.MockTableData> getGlobalKTableWrapperInstance(true).getCreate(testUtils.getTestTopicName("sample-topic"), TestUtils.MockTableData.class);

        TestUtils.MockTableData updateInstance = new TestUtils.MockTableData();
        updateInstance.totalValues = 1;

        kTableInstance.updateValue(updateInstance);
        Thread.sleep(2000);
        assertNotNull(kTableInstance.getValue());
        kTableInstance.getValue();
        assertEquals(1, kTableInstance.getValue().getTotalValues());
    }

    @Test
    void testCanHaveMultipleInstance() throws ExecutionException, InterruptedException {
        StoreProcessor<TestUtils.MockTableData> instance1 = testUtils.<TestUtils.MockTableData>getGlobalKTableWrapperInstance(true).getCreate(testUtils.getTestTopicName("sample-topic"), TestUtils.MockTableData.class);

        StoreProcessor<TestUtils.MockTableData> instance2 = testUtils.<TestUtils.MockTableData>getGlobalKTableWrapperInstance(false).getCreate(testUtils.getTestTopicName("sample-topic"), TestUtils.MockTableData.class);

        TestUtils.MockTableData updateInstance = new TestUtils.MockTableData();
        updateInstance.totalValues = 1;
        instance1.updateValue(updateInstance);
        Thread.sleep(2000);

        assertEquals(1, instance2.getValue().getTotalValues());

        updateInstance.totalValues += 1;
        instance2.updateValue(updateInstance);
        Thread.sleep(2000);

        assertEquals(2, instance1.getValue().getTotalValues());

        StoreProcessor<TestUtils.MockTableData> instance3 = testUtils.<TestUtils.MockTableData>getGlobalKTableWrapperInstance(false).getCreate(testUtils.getTestTopicName("sample-topic"), TestUtils.MockTableData.class);
        assertEquals(2, instance3.getValue().getTotalValues());

    }
}
