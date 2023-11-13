package org.jembi.jempi.shared.kafka.globalContext.globalKTableWrapper;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.GlobalKTable;
import org.apache.kafka.streams.state.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.shared.kafka.MyKafkaConsumerByPartition;
import org.jembi.jempi.shared.kafka.MyKafkaProducer;
import org.jembi.jempi.shared.kafka.globalContext.globalKTableWrapper.serde.KTableDeserializer;
import org.jembi.jempi.shared.kafka.globalContext.globalKTableWrapper.serde.KTableSerde;
import org.jembi.jempi.shared.kafka.globalContext.globalKTableWrapper.serde.KTableSerializer;

import java.time.Duration;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;


public class GlobalKTableWrapperInstance<T> {

    private static final Logger LOGGER = LogManager.getLogger(GlobalKTableWrapper.class);
    private final String topicName;
    private final String uniqueId;
    private final ReadOnlyKeyValueStore<String, T> keyValueStore;
    private final MyKafkaProducer<String, T> updater;
    private final GlobalKTable<String, T> globalTable;

    GlobalKTableWrapperInstance(final String bootStrapServers, final String topicName){

        this.topicName = topicName;
        this.uniqueId = getUniqueId(topicName);

        StreamsBuilder builder = new StreamsBuilder();
        globalTable = builder.globalTable(topicName);
        builder.addStateStore(Stores.keyValueStoreBuilder(
                Stores.persistentKeyValueStore(String.format("%s-store", uniqueId)),
                Serdes.String(),
                new KTableSerde<T>()
        ));

        KafkaStreams streams = new KafkaStreams(builder.build(), this.getProperties(bootStrapServers, uniqueId));
        streams.start();

        keyValueStore = streams.store(StoreQueryParameters.fromNameAndType(String.format("%s-store", uniqueId), QueryableStoreTypes.keyValueStore()));
        updater = new MyKafkaProducer(bootStrapServers,
                                        topicName,
                                        new StringSerializer(),
                                        new KTableSerializer<T>(),
                                        String.format("%s-producer", uniqueId));
    }

    private String getUniqueId(final String topicName){
        return String.format("jempi-global-ktable-wrapper-%s-%s", topicName, UUID.randomUUID());
    }
    private Properties getProperties(final String bootStrapServers, final String uniqueName){
        Properties properties = new Properties();
        properties.put(StreamsConfig.APPLICATION_ID_CONFIG, String.format("%s-app.id", uniqueName));
        properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootStrapServers);
        properties.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        properties.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, new KTableSerde<T>().getClass());

        return properties;
    }

    public T getValue(){
        return keyValueStore.get(topicName);
    }

    public void updateValue(T value) throws ExecutionException, InterruptedException {
        updater.produceSync(this.topicName, value);
    }

    public void onTableUpdate(Consumer<T> consumer){

    }

    public void updateValueToTopicEvent(final String bootstrapServers,
                                        final String topic,
                                        final String clientId,
                                        final String groupId,
                                        final TableUpdaterProcessor<T, T, T> processor){

        // maybe listen on global store/all patitions
        MyKafkaConsumerByPartition<String, T> p = new MyKafkaConsumerByPartition<String, T>(
                bootstrapServers,
                topic,
                new StringDeserializer(),
                new KTableDeserializer<T>(),
                clientId,
                groupId,
                500,
                10
                );

        p.poll(Duration.ofMillis(200)).forEach(record -> {
            try {
                updateValue(processor.apply(getValue(), record.value()));
            } catch (ExecutionException | InterruptedException e) {
                LOGGER.error(String.format("Failing to update the global kTable %s with the record %s", topicName, ""), e); //TODO
                throw new RuntimeException(e);
            }
        });

    }
}
