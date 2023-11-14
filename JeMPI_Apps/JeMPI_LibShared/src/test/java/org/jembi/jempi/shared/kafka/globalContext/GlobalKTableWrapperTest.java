package org.jembi.jempi.shared.kafka.globalContext;

import org.apache.kafka.clients.admin.DeleteTopicsOptions;
import org.apache.kafka.clients.admin.ListTopicsOptions;
import org.apache.kafka.clients.admin.TopicListing;
import org.apache.kafka.streams.errors.StreamsException;
import org.jembi.jempi.shared.kafka.globalContext.globalKTableWrapper.GlobalKTableWrapper;
import org.jembi.jempi.shared.kafka.globalContext.globalKTableWrapper.GlobalKTableWrapperInstance;

import java.util.Collection;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GlobalKTableWrapperTest {

    TestUtils testUtils;

    @BeforeAll
    void prepareForTests(){
        testUtils = new TestUtils("localhost:9092");
    }

    GlobalKTableWrapper getGlobalKTableWrapperInstance(Boolean restAll) throws ExecutionException, InterruptedException {
        if (restAll){
            Collection<String> collection = testUtils.kafkaAdminClient.listTopics(new ListTopicsOptions().listInternal(false)).listings().get().stream()
                    .map(TopicListing::name)
                    .filter(name -> name.startsWith("testTopic"))
                    .collect(Collectors.toCollection(ArrayList::new));


            testUtils.kafkaAdminClient.deleteTopics(collection, new DeleteTopicsOptions()).all().get();
            Thread.sleep(1000);
        }
        return new GlobalKTableWrapper(testUtils.bootStrapServer);
    }
    @Test
    void testCanCreateNewInstance() throws ExecutionException, InterruptedException {
        GlobalKTableWrapperInstance<TestUtils.MockTableData> sampleInstance = getGlobalKTableWrapperInstance(true).getCreate(testUtils.getTestTopicName("sample-table"), TestUtils.MockTableData.class);
        assertInstanceOf(GlobalKTableWrapperInstance.class, sampleInstance);
    }
    @Test
    void testItErrorsOutWhenGlobalKTableDoesNotExists() throws ExecutionException, InterruptedException {
        assertThrows(StreamsException.class, () -> {
            getGlobalKTableWrapperInstance(true).get(testUtils.getTestTopicName("sample-table"), TestUtils.MockTableData.class);
        });
    }
    @Test
    void testDoesNotRecreateIfGlobalKTableAlreadyExists() throws ExecutionException, InterruptedException {
        GlobalKTableWrapper gktableWrapper = getGlobalKTableWrapperInstance(true);
        GlobalKTableWrapperInstance<TestUtils.MockTableData> sampleInstance = gktableWrapper.getCreate(testUtils.getTestTopicName("sample-table"), TestUtils.MockTableData.class);
        assertInstanceOf(GlobalKTableWrapperInstance.class, sampleInstance);

        assertEquals(sampleInstance.hashCode(), gktableWrapper.get(testUtils.getTestTopicName("sample-table"), TestUtils.MockTableData.class).hashCode());


    }
}
