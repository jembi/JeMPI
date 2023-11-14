package org.jembi.jempi.shared.kafka.globalContext;

import org.jembi.jempi.shared.kafka.globalContext.globalKTableWrapper.GlobalKTableWrapperInstance;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.concurrent.ExecutionException;
import static org.junit.jupiter.api.Assertions.*;
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GlobalKTableWrapperInstanceTest {

    TestUtils testUtils;

    @BeforeAll
    void prepareForTests(){
        testUtils = new TestUtils("localhost:9092");
    }

    @Test
    void testCanGetValue() throws ExecutionException, InterruptedException {
        GlobalKTableWrapperInstance kTableInstance = testUtils.getGlobalKTableWrapperInstance(true).getCreate(testUtils.getTestTopicName("sample-topic"), TestUtils.MockTableData.class);
        Thread.sleep(2000);
        assertNull(kTableInstance.getValue());
    }
    @Test
    void testCanUpdateAndGetTableValue() throws ExecutionException, InterruptedException {
        GlobalKTableWrapperInstance<TestUtils.MockTableData> kTableInstance = testUtils.getGlobalKTableWrapperInstance(true).getCreate(testUtils.getTestTopicName("sample-topic"), TestUtils.MockTableData.class);

        TestUtils.MockTableData updateInstance = new TestUtils.MockTableData();
        updateInstance.totalValues = 1;

        kTableInstance.updateValue(updateInstance);
        Thread.sleep(2000);
        assertNotNull(kTableInstance.getValue());
        kTableInstance.getValue();
        assertEquals(kTableInstance.getValue().getTotalValues(), 1);
    }

    @Test
    void testCanHaveMultipleInstance() throws ExecutionException, InterruptedException {
        GlobalKTableWrapperInstance<TestUtils.MockTableData> instance1 = testUtils.getGlobalKTableWrapperInstance(true).getCreate(testUtils.getTestTopicName("sample-topic"), TestUtils.MockTableData.class);

        GlobalKTableWrapperInstance<TestUtils.MockTableData> instance2 = testUtils.getGlobalKTableWrapperInstance(false).getCreate(testUtils.getTestTopicName("sample-topic"), TestUtils.MockTableData.class);

        TestUtils.MockTableData updateInstance = new TestUtils.MockTableData();
        updateInstance.totalValues = 1;
        instance1.updateValue(updateInstance);
        Thread.sleep(2000);

        assertEquals(instance2.getValue().getTotalValues(), 1);

        updateInstance.totalValues += 1;
        instance2.updateValue(updateInstance);
        Thread.sleep(2000);

        assertEquals(instance1.getValue().getTotalValues(), 2);

        GlobalKTableWrapperInstance<TestUtils.MockTableData> instance3 = testUtils.getGlobalKTableWrapperInstance(false).getCreate(testUtils.getTestTopicName("sample-topic"), TestUtils.MockTableData.class);
        assertEquals(instance3.getValue().getTotalValues(), 2);

    }
}
