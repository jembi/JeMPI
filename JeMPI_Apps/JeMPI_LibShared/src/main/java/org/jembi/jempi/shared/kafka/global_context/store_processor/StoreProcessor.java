package org.jembi.jempi.shared.kafka.global_context.store_processor;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.errors.InvalidStateStoreException;
import org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.state.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.shared.kafka.MyKafkaProducer;
import org.jembi.jempi.shared.kafka.global_context.store_processor.serde.StoreValueDeserializer;
import org.jembi.jempi.shared.kafka.global_context.store_processor.serde.StoreValueSerde;
import org.jembi.jempi.shared.kafka.global_context.store_processor.serde.StoreValueSerializer;

import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class StoreProcessor<T> {
    private static final Logger LOGGER = LogManager.getLogger(StoreProcessor.class);
    private final String topicName;
    private final String globalStoreName;
    private final ReadOnlyKeyValueStore<String, T> keyValueStore;
    private final MyKafkaProducer<String, T> updater;
    KafkaStreams streams;
    public StoreProcessor(final String bootStrapServers, final String topicName, final Class<T> serializeCls) throws InterruptedException, ExecutionException {

        this.topicName = topicName;
        String uniqueId = getUniqueId(topicName);

        this.globalStoreName = String.format("%s-store", topicName);


        Topology storeProcessorTopology = new Topology();
        storeProcessorTopology.addGlobalStore(Stores.keyValueStoreBuilder(
                        Stores.inMemoryKeyValueStore(globalStoreName),
                        Serdes.String(),
                        new StoreValueSerde<T>(serializeCls)).withLoggingDisabled(),
                "rootNode",
                new StringDeserializer(),
                new StoreValueDeserializer<T>(serializeCls),
                topicName,
                "rootProcessor",
                () -> new StoreProcessorValuesUpdater<>(getValueUpdater(), globalStoreName));


//        StreamsBuilder builder = new StreamsBuilder();
//        builder.addGlobalStore(Stores.keyValueStoreBuilder(
//                        Stores.inMemoryKeyValueStore(globalStoreName),
//                        Serdes.String(),
//                        new StoreValueSerde<T>(serializeCls)),
//                topicName,
//                Consumed.with(Serdes.String(), new StoreValueSerde<T>(serializeCls)),
//                () -> new StoreProcessorValuesUpdater<>(getValueUpdater(), globalStoreName));


        streams = new KafkaStreams(storeProcessorTopology, this.getProperties(bootStrapServers, uniqueId));

        streams.setUncaughtExceptionHandler(exception -> {
            LOGGER.error(String.format("A error occurred on the global KTable stream %s", topicName), exception);
            return StreamsUncaughtExceptionHandler.StreamThreadExceptionResponse.SHUTDOWN_CLIENT;
        });

        streams.start();
        keyValueStore = streams.store(StoreQueryParameters.fromNameAndType(globalStoreName,
                                        QueryableStoreTypes.keyValueStore()));

        waitUntilStoreIsQueryable().get();

        updater = new MyKafkaProducer<>(bootStrapServers,
                                        topicName,
                                        new StringSerializer(),
                                        new StoreValueSerializer<>(),
                                        String.format("%s-producer", getUniqueId(topicName)));

        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }

    private CompletableFuture<Boolean> waitUntilStoreIsQueryable() {
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

        future.orTimeout(15000, TimeUnit.MILLISECONDS)
                .exceptionally(throwable -> {
                    future.completeExceptionally(new TimeoutException("Timeout waiting for the store to become Queryable ."));
                    return null;
                });

        return future;
    }

    private String getUniqueId(final String topicName) {
        return String.format("jempi-global-store-wrapper-%s-%s", topicName, UUID.randomUUID());
    }
    private Properties getProperties(final String bootStrapServers, final String uniqueName) {
        Properties properties = new Properties();
        properties.put(StreamsConfig.APPLICATION_ID_CONFIG, String.format("%s-app.id", uniqueName));
        properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootStrapServers);

        return properties;
    }
    protected StoreUpdaterProcessor<T, T, T> getValueUpdater() {
        return (T globalValue, T currentValue) -> currentValue;
    }
    public T getValue() {
        return keyValueStore.get(topicName);
    }

    public void updateValue(final T value) throws ExecutionException, InterruptedException {
        updater.produceSync(this.topicName, value);
    }

}
