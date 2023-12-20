package org.jembi.jempi.shared.kafka.global_context.store_processor.utils;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DeleteTopicsOptions;
import org.apache.kafka.clients.admin.ListTopicsOptions;
import org.apache.kafka.clients.admin.TopicListing;
import org.apache.kafka.streams.StreamsConfig;
import org.jembi.jempi.shared.kafka.global_context.store_processor.StoreProcessor;
import org.jembi.jempi.shared.kafka.global_context.store_processor.StoreProcessorFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class TestUtils {

    public static class MockTableData{
        public int totalValues;

        public int getTotalValues() {
            return totalValues;
        }
    }

    public final AdminClient kafkaAdminClient;
    public final  String bootStrapServer;

    public TestUtils(final String bootStrapServer){
        this.bootStrapServer = bootStrapServer;
        Properties properties = new Properties();
        properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootStrapServer);
        kafkaAdminClient = AdminClient.create(properties);
    }

    public String getTestTopicName(final String topicName){
        return String.format("testTopic-%s", topicName);
    }

    public void resetAllTopics() throws ExecutionException, InterruptedException  {
        Collection<String> collection = kafkaAdminClient.listTopics(new ListTopicsOptions().listInternal(false)).listings().get().stream()
                .map(TopicListing::name)
                .filter(name -> name.startsWith("testTopic"))
                .collect(Collectors.toCollection(ArrayList::new));


        kafkaAdminClient.deleteTopics(collection, new DeleteTopicsOptions()).all().get();
        Thread.sleep(1000);
    }
    public <T> StoreProcessorFactory<T>  getGlobalKTableWrapperInstance(Boolean restAll) throws ExecutionException, InterruptedException {
        if (restAll){
            this.resetAllTopics();
        }
        return new StoreProcessorFactory<T>(bootStrapServer);
    }

    public StoreProcessor<MockTableData> getMockStoreProcessor(Boolean restAll) throws ExecutionException, InterruptedException{
        if (restAll){
            this.resetAllTopics();
        }
        return new MockStoreProcessorFactor(bootStrapServer).getCreate("testTopic", TestUtils.MockTableData.class);
    }
}
