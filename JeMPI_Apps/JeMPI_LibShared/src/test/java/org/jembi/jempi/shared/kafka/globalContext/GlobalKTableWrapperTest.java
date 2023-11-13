package org.jembi.jempi.shared.kafka.globalContext;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DeleteTopicsOptions;
import org.apache.kafka.clients.admin.ListTopicsOptions;
import org.apache.kafka.clients.admin.TopicListing;
import org.apache.kafka.streams.errors.StreamsException;
import org.jembi.jempi.shared.kafka.globalContext.globalKTableWrapper.GlobalKTableWrapper;
import org.jembi.jempi.shared.kafka.globalContext.globalKTableWrapper.GlobalKTableWrapperInstance;

import org.apache.kafka.streams.StreamsConfig;

import java.util.Collection;
import java.util.Properties;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GlobalKTableWrapperTest {

    class MockTableData{
        private int count;

        public int getCount() {
            return count;
        }
    }

    private AdminClient kafkaAdminClient;
    private String bootStrapServer;

    @BeforeAll
    void resetKafkaData(){
        bootStrapServer = "localhost:9092";
        Properties properties = new Properties();
        properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootStrapServer);
        kafkaAdminClient = AdminClient.create(properties);
    }

    String getTestTopicName(final String topicName){
        return String.format("testTopic-%s", topicName);
    }
    GlobalKTableWrapper getGlobalKTableWrapperInstance(Boolean restAll) throws ExecutionException, InterruptedException {
        if (restAll){
            Collection<String> collection = kafkaAdminClient.listTopics(new ListTopicsOptions().listInternal(false)).listings().get().stream()
                    .map(TopicListing::name)
                    .filter(name -> name.startsWith("testTopic"))
                    .collect(Collectors.toCollection(ArrayList::new));


            kafkaAdminClient.deleteTopics(collection, new DeleteTopicsOptions()).all().get();
            Thread.sleep(1000);
        }
        return new GlobalKTableWrapper(bootStrapServer);
    }
    @Test
    void testCanCreateNewInstance() throws ExecutionException, InterruptedException {
        GlobalKTableWrapperInstance<MockTableData> sampleInstance = getGlobalKTableWrapperInstance(true).getCreate(getTestTopicName("sample-table"));
        assertInstanceOf(GlobalKTableWrapperInstance.class, sampleInstance);
    }
    @Test
    void testItErrorsOutWhenGlobalKTableDoesNotExists() throws ExecutionException, InterruptedException {
        assertThrows(StreamsException.class, () -> {
            getGlobalKTableWrapperInstance(true).get(getTestTopicName("sample-table"));
        });
    }
    @Test
    void testDoesNotRecreateIfGlobalKTableAlreadyExists() throws ExecutionException, InterruptedException {
        GlobalKTableWrapper gktableWrapper = getGlobalKTableWrapperInstance(true);
        GlobalKTableWrapperInstance<MockTableData> sampleInstance = gktableWrapper.getCreate(getTestTopicName("sample-table"));
        assertInstanceOf(GlobalKTableWrapperInstance.class, sampleInstance);

        assertEquals(sampleInstance.hashCode(), gktableWrapper.get(getTestTopicName("sample-table")).hashCode());


    }
}
