package org.jembi.jempi.shared.kafka.globalContext.globalKTableWrapper;

import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.GlobalKTable;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.jembi.jempi.shared.kafka.MyKafkaConsumerByPartition;
import org.jembi.jempi.shared.kafka.MyKafkaProducer;

import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;


public class GlobalKTableWrapperInstance<T> {

    private final String topicName;
    private final ReadOnlyKeyValueStore<String, T> keyValueStore;
    private final MyKafkaProducer<String, T> updater;

    GlobalKTableWrapperInstance(final String topicName){
        this.topicName = topicName;

        StreamsBuilder builder = new StreamsBuilder();
        GlobalKTable<String, T> globalTable = builder.globalTable(topicName);
        KafkaStreams streams = new KafkaStreams(builder.build(), properties);
        streams.start();

        keyValueStore = streams.store(StoreQueryParameters.fromNameAndType(topicName, QueryableStoreTypes.keyValueStore()));
        updater = new  MyKafkaProducer("", topicName);
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
            updateValue(processor.apply(getValue(), record.value()));
        });

    }
}
