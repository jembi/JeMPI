package org.jembi.jempi.shared.kafka.globalContext.globalKTableWrapper;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.errors.InvalidStateStoreException;
import org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorSupplier;
import org.apache.kafka.streams.state.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.shared.kafka.MyKafkaProducer;
import org.jembi.jempi.shared.kafka.globalContext.globalKTableWrapper.serde.KTableSerde;
import org.jembi.jempi.shared.kafka.globalContext.globalKTableWrapper.serde.KTableSerializer;

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
    private final String globalStoreName;
    private final ReadOnlyKeyValueStore<String, T> keyValueStore;
    private final MyKafkaProducer<String, T> updater;
    private final Class<T> serializeCls;
    KafkaStreams streams;
    public GlobalKTableWrapperInstance(final String bootStrapServers, final String topicName,  Class<T> serializeCls) throws InterruptedException, ExecutionException {

        this.serializeCls = serializeCls;
        this.topicName = topicName;
        this.uniqueId = getUniqueId(topicName);
        this.globalStoreName = String.format("%s-store", topicName);

        StreamsBuilder builder = new StreamsBuilder();
        builder.addGlobalStore(Stores.keyValueStoreBuilder(
                        Stores.persistentKeyValueStore(globalStoreName), // TODO: Consider in memory. Also consider umique name
                        Serdes.String(),
                        new KTableSerde<T>(this.serializeCls))
                ,
                topicName,
                Consumed.with(Serdes.String(),  new KTableSerde<T>(this.serializeCls)),
                new ProcessorSupplier<String, T, Void, Void>() {
                    public Processor<String, T, Void, Void> get() {
                        return new GlobalKTableValuesUpdater(GetValueUpdater(), globalStoreName);
                    }
                });


        streams = new KafkaStreams(builder.build(), this.getProperties(bootStrapServers, uniqueId)); // TODO: Everytime called creates own unique

        streams.setUncaughtExceptionHandler(( exception) -> {
            LOGGER.error(String.format("A error occurred on the global KTable stream %s", topicName), exception);
            return StreamsUncaughtExceptionHandler.StreamThreadExceptionResponse.SHUTDOWN_CLIENT;
        });

        streams.start();

        keyValueStore = streams.store(StoreQueryParameters.fromNameAndType(globalStoreName,
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

        return properties;
    }
    protected TableUpdaterProcessor<T, T, T> GetValueUpdater(){
        return (T globalValue, T currentValue) -> currentValue;
    }
    public T getValue(){
        return keyValueStore.get(topicName);
    }

    public void updateValue(T value) throws ExecutionException, InterruptedException {
        updater.produceSync(this.topicName, value);
    }

    public void onTableUpdate(Consumer<T> consumer){

    }


}
