package org.jembi.jempi.shared.kafka.globalContext.globalKTableWrapper;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.errors.InvalidStateStoreException;
import org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.GlobalKTable;
import org.apache.kafka.streams.kstream.Materialized;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;


public class GlobalKTableWrapperInstance<T> {

    private static final Logger LOGGER = LogManager.getLogger(GlobalKTableWrapper.class);
    private final String topicName;
    private final String uniqueId;
    private final ReadOnlyKeyValueStore<String, T> keyValueStore;
    private final MyKafkaProducer<String, T> updater;
    private final GlobalKTable<String, T> globalTable;
    private final Class<T> serializeCls;
    KafkaStreams streams;
    GlobalKTableWrapperInstance(final String bootStrapServers, final String topicName,  Class<T> serializeCls) throws InterruptedException, ExecutionException {

        this.serializeCls = serializeCls;
        this.topicName = topicName;
        this.uniqueId = getUniqueId(topicName);

        StreamsBuilder builder = new StreamsBuilder();

        globalTable = builder.globalTable(topicName,
                                          Consumed.with(Serdes.String(),  new KTableSerde<T>(this.serializeCls)),
                                          Materialized.as(String.format("%s-store", topicName)));

        streams = new KafkaStreams(builder.build(), this.getProperties(bootStrapServers, uniqueId));

        streams.setUncaughtExceptionHandler(( exception) -> {
            LOGGER.error(String.format("A error occurred on the global KTable stream %s", topicName), exception);
            return StreamsUncaughtExceptionHandler.StreamThreadExceptionResponse.SHUTDOWN_CLIENT;
        });

        streams.start();


        keyValueStore = streams.store(StoreQueryParameters.fromNameAndType(String.format("%s-store", topicName),
                                        QueryableStoreTypes.keyValueStore()));
        waitUntilStoreIsQueryable(streams).get();


        updater = new MyKafkaProducer(bootStrapServers,
                                        topicName,
                                        new StringSerializer(),
                                        new KTableSerializer<T>(),
                                        String.format("%s-producer", getUniqueId(topicName)));
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }

    private CompletableFuture<Boolean> waitUntilStoreIsQueryable(KafkaStreams streams) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        CompletableFuture.runAsync(() -> {
            while (true) {
                try {
                    getValue();
                    future.complete(true);
                    break;
                } catch (InvalidStateStoreException ignored) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        future.completeExceptionally(e);
                    }
                }
            }
        });

        future.orTimeout(5000, TimeUnit.MILLISECONDS)
                .exceptionally(throwable -> {
                    future.completeExceptionally(new TimeoutException("Timeout waiting for the store to become Queryable ."));
                    return null;
                });

        return future;
    }

    private String getUniqueId(final String topicName){
        return String.format("jempi-global-ktable-wrapper-%s-%s", topicName, UUID.randomUUID());
    }
    private Properties getProperties(final String bootStrapServers, final String uniqueName){
        Properties properties = new Properties();
        properties.put(StreamsConfig.APPLICATION_ID_CONFIG, String.format("%s-app.id", uniqueName));
        properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootStrapServers);
        properties.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        properties.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, new KTableSerde<T>(this.serializeCls).getClass());

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

        // TODO maybe listen on global store/all patitions
        // TODO: maybe use the process api that listen to all table and update, it wont change
        MyKafkaConsumerByPartition<String, T> p = new MyKafkaConsumerByPartition<String, T>(
                bootstrapServers,
                topic,
                new StringDeserializer(),
                new KTableDeserializer<T>(this.serializeCls),
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
