package org.jembi.jempi.bootstrapper.data.stream.kafka;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.TopicListing;
import org.apache.kafka.streams.StreamsConfig;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;


import java.util.Collection;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class KafkaDataBootstrapperTest {

    private AdminClient kafkaAdminClient;
    private KafkaDataBootstrapper kafkaDataBootstrapper;
    @BeforeAll
    public void createMockObjects() throws Exception{
        Properties properties = new Properties();
        properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        kafkaAdminClient = AdminClient.create(properties);
        kafkaDataBootstrapper = new KafkaDataBootstrapper("aFile");

    }
    @Test
    public void testCanCreateSchemaTopics() throws InterruptedException, ExecutionException {

        kafkaDataBootstrapper.deleteData();
        kafkaDataBootstrapper.createSchema();

        Collection<TopicListing> topicsAfterUpdateFuture = kafkaAdminClient.listTopics().listings().get();
        assertEquals(7, topicsAfterUpdateFuture.size());

    }
    @Test
    public void testCanDeleteTopics() throws InterruptedException, ExecutionException {
        kafkaDataBootstrapper.createSchema();
        kafkaDataBootstrapper.deleteData();
        Collection<TopicListing> topicsAfterUpdateFuture = kafkaAdminClient.listTopics().listings().get();
        assertEquals(0, topicsAfterUpdateFuture.size());
    }
    @Test
    public void testCanResetAll() throws ExecutionException, InterruptedException {
        kafkaDataBootstrapper.resetAll();
        Collection<TopicListing> topicsAfterUpdateFuture = kafkaAdminClient.listTopics().listings().get();
        assertEquals(7, topicsAfterUpdateFuture.size());
    }
}
