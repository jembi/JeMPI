package org.jembi.jempi.api;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import org.apache.kafka.common.serialization.*;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.AppConfig;
import org.jembi.jempi.shared.models.GlobalConstants;
import org.jembi.jempi.shared.models.Notification;
import org.jembi.jempi.shared.serdes.JsonPojoDeserializer;
import org.jembi.jempi.shared.serdes.JsonPojoSerializer;

import java.util.Properties;

public class NotificationStreamProcessor {

    private static final Logger LOGGER = LogManager.getLogger(NotificationStreamProcessor.class);
    private KafkaStreams notificationKafkaStreams = null;

    void open(final ActorSystem<Void> system, final ActorRef<BackEnd.Event> backEnd) {
        LOGGER.info("Stream Processor");

        final Properties props = loadConfig();
        final Serde<String> stringSerde = Serdes.String();
        final Serializer<Notification> batchEntitySerializer = new JsonPojoSerializer<>();
        final Deserializer<Notification> batchEntityDeserializer = new JsonPojoDeserializer<>(Notification.class);
        final Serde<Notification> notificationSerde = Serdes.serdeFrom(batchEntitySerializer, batchEntityDeserializer);
        final StreamsBuilder streamsBuilder = new StreamsBuilder();
        final KStream<String, Notification> notificationStream = streamsBuilder.stream(
                GlobalConstants.TOPIC_NOTIFICATIONS,
                Consumed.with(stringSerde, notificationSerde));
        notificationStream
                .foreach((key, value) -> {
                            LOGGER.debug("key:{}, value:{}", key, value);
                            // TODO: Write value to database
                        }
                );
        LOGGER.debug("tag");
        notificationKafkaStreams = new KafkaStreams(streamsBuilder.build(), props);
        LOGGER.debug("tag");
        notificationKafkaStreams.cleanUp();
        LOGGER.debug("tag");
        notificationKafkaStreams.start();
        LOGGER.debug("tag");
        Runtime.getRuntime().addShutdownHook(new Thread(notificationKafkaStreams::close));
        LOGGER.info("Notifications started");
    }

    private Properties loadConfig() {
        final Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, AppConfig.KAFKA_APPLICATION_ID);
        props.put(StreamsConfig.CLIENT_ID_CONFIG, AppConfig.KAFKA_CLIENT_ID);
//        props.put(StreamsConfig.GROUP_ID, AppConfig.KAFKA_GROUP_ID);  TODO check howto set group id
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, AppConfig.KAFKA_BOOTSTRAP_SERVERS);
        props.put(StreamsConfig.POLL_MS_CONFIG, 50);
        return props;
    }


}
