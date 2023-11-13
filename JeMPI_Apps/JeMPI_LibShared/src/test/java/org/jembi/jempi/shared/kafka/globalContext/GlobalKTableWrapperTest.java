package org.jembi.jempi.shared.kafka.globalContext;

import org.jembi.jempi.shared.kafka.globalContext.globalKTableWrapper.GlobalKTableWrapper;
import org.jembi.jempi.shared.kafka.globalContext.globalKTableWrapper.GlobalKTableWrapperInstance;

import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GlobalKTableWrapperTest {

    class MockTableData{
        private int count;

        public int getCount() {
            return count;
        }
    }

    String getTestTopicName(final String topicName){
        return String.format("testTopic-%s", topicName);
    }
    GlobalKTableWrapper getGlobalKTableWrapperInstance(Boolean restAll){
        return new GlobalKTableWrapper("localhost:9092");
    }

    @Test
    void testCanCreateNewInstance() throws ExecutionException, InterruptedException {
        GlobalKTableWrapperInstance<MockTableData> sampleInstance =  getGlobalKTableWrapperInstance(true).getCreate(getTestTopicName("sample-table"));
    }

    void testItErrorsOutWhenGlobalKTableDoesNotExists(){

    }

    void testDoesNotRecreateIfGlobalKTableAlreadyExists(){

    }
}
