package org.jembi.jempi.shared.kafka.global_context.store_processor;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.errors.InvalidStateStoreException;
import org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.state.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.shared.kafka.MyKafkaProducer;
import org.jembi.jempi.shared.kafka.global_context.store_processor.serde.StoreValueSerde;

import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class StoreProcessor<T> {
    private static final Logger LOGGER = LogManager.getLogger(StoreProcessor.class);
    private final String topicName;
    private final String sinkTopicName;
    private final String topicStoreName;
    private final ReadOnlyKeyValueStore<String, T> keyValueStore;
    private final MyKafkaProducer<String, T> updater;
    KafkaStreams streams;
    protected StoreProcessor(final String bootStrapServers, final String topicNameIn, final String sinkTopicNameIn, final Class<T> serializeCls) throws InterruptedException, ExecutionException {

        this.topicName = topicNameIn;
        this.sinkTopicName = sinkTopicNameIn;

        String uniqueId = Utilities.getUniqueAppId(topicName, "" + ProcessHandle.current().pid());

        this.topicStoreName = String.format("%s-store", topicName);

        StreamsBuilder builder = new StreamsBuilder();
        builder.addGlobalStore(Stores.keyValueStoreBuilder(
                        Stores.inMemoryKeyValueStore(topicStoreName),
                        Serdes.String(),
                        new StoreValueSerde<T>(serializeCls)),
                topicName,
                Consumed.with(Serdes.String(), new StoreValueSerde<T>(serializeCls)),
                () -> new StoreProcessorValuesUpdater<>(getValueUpdater(),
                                                        topicName,
                                                        topicStoreName,
                                                        new StoreProcessorSinkManager<>(topicName, sinkTopicName, bootStrapServers, serializeCls)));

        streams = new KafkaStreams(builder.build(), this.getProperties(bootStrapServers, uniqueId));

        streams.setUncaughtExceptionHandler(exception -> {
            LOGGER.error(String.format("A error occurred on the global KTable stream %s", topicName), exception);
            return StreamsUncaughtExceptionHandler.StreamThreadExceptionResponse.SHUTDOWN_CLIENT;
        });

        streams.start();
        keyValueStore = streams.store(StoreQueryParameters.fromNameAndType(topicStoreName,
                                        QueryableStoreTypes.keyValueStore()));

        waitUntilStoreIsQueryable().get();

        updater = Utilities.getTopicProducer(topicName, bootStrapServers);

        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }

    public boolean validateIsAlive() {
        if (!streams.state().isRunningOrRebalancing()) {
            return false;
        }

        try {
            getValue();
        } catch (InvalidStateStoreException e) {
            return false;
        }

        return true;
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
                } catch (Exception e) {
                    future.completeExceptionally(e);
                }
            }
        });

        future.orTimeout(5000, TimeUnit.MILLISECONDS);

        return future;
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
