package org.jembi.jempi.shared.kafka.global_context.store_processor;

import org.apache.kafka.clients.admin.DeleteTopicsOptions;
import org.apache.kafka.clients.admin.ListTopicsOptions;
import org.apache.kafka.clients.admin.TopicListing;
import org.apache.kafka.common.errors.UnknownTopicIdException;

import java.util.Collection;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.jembi.jempi.shared.kafka.global_context.store_processor.utils.TestUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class StoreProcessorFactoryTest {
    TestUtils testUtils;

    @BeforeAll
    void prepareForTests(){
        testUtils = new TestUtils("localhost:9097");
    }

    @Test
    void testCanCreateNewInstance() throws ExecutionException, InterruptedException {
        StoreProcessorFactory<TestUtils.MockTableData> factory = testUtils.getGlobalKTableWrapperInstance(true);
        StoreProcessor<TestUtils.MockTableData> sampleInstance = factory.getCreate(testUtils.getTestTopicName("sample-table"), TestUtils.MockTableData.class);
        assertInstanceOf(StoreProcessor.class, sampleInstance);
    }
    @Test
    void testItErrorsOutWhenGlobalKTableDoesNotExists() throws ExecutionException, InterruptedException {
        assertThrows(UnknownTopicIdException.class, () -> {
            testUtils.<TestUtils.MockTableData>getGlobalKTableWrapperInstance(true).get(testUtils.getTestTopicName("sample-table"), TestUtils.MockTableData.class);
        });
    }
    @Test
    void testDoesNotRecreateIfGlobalKTableAlreadyExists() throws ExecutionException, InterruptedException {
        StoreProcessorFactory<TestUtils.MockTableData>  factory = testUtils.getGlobalKTableWrapperInstance(true);
        StoreProcessor<TestUtils.MockTableData> sampleInstance = factory.getCreate(testUtils.getTestTopicName("sample-table"), TestUtils.MockTableData.class);
        assertInstanceOf(StoreProcessor.class, sampleInstance);

        assertEquals(sampleInstance.hashCode(), factory.get(testUtils.getTestTopicName("sample-table"), TestUtils.MockTableData.class).hashCode());
    }
}
