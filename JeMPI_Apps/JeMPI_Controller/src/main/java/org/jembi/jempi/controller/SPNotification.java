package org.jembi.jempi.controller;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.AppConfig;
import org.jembi.jempi.shared.models.GlobalConstants;
import org.jembi.jempi.shared.models.Notification;
import org.jembi.jempi.shared.serdes.JsonPojoDeserializer;
import org.jembi.jempi.shared.serdes.JsonPojoSerializer;

import java.sql.SQLException;
import java.util.Properties;
import java.util.UUID;

final class SPNotification {

   private static final Logger LOGGER = LogManager.getLogger(SPNotification.class);

   void open() {
      LOGGER.info("Stream Processor");
      final var psqlNotifications = new PsqlNotifications();
      final Properties props = new Properties();
      props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, AppConfig.KAFKA_BOOTSTRAP_SERVERS);
      props.put(StreamsConfig.APPLICATION_ID_CONFIG, AppConfig.KAFKA_APPLICATION_ID + "-NOTIFICATIONS");
      props.put(StreamsConfig.POLL_MS_CONFIG, 50);

      final Serde<String> stringSerde = Serdes.String();
      final Serializer<Notification> notificationSerializer = new JsonPojoSerializer<>();
      final Deserializer<Notification> notificationDeserializer = new JsonPojoDeserializer<>(Notification.class);
      final Serde<Notification> notificationSerde = Serdes.serdeFrom(notificationSerializer, notificationDeserializer);
      final StreamsBuilder streamsBuilder = new StreamsBuilder();
      final KStream<String, Notification> notificationStream = streamsBuilder.stream(
            GlobalConstants.TOPIC_NOTIFICATIONS,
            Consumed.with(stringSerde, notificationSerde));
      notificationStream
            .foreach((key, value) -> {
               try {
                  UUID id = UUID.randomUUID();
                  psqlNotifications.insert(id,
                                           value.notificationType().toString(),
                                           value.patientNames(),
                                           value.linkedTo().score(),
                                           value.timeStamp(),
                                           value.linkedTo().gID(),
                                           value.dID());

                  for (int i = 0; i < value.candidates().size(); i++) {
                     psqlNotifications.insertCandidates(id,
                                                        value.candidates().get(i).score(),
                                                        value.candidates().get(i).gID());
                  }
               } catch (SQLException e) {
                  LOGGER.debug(e.toString());
               }
            });

      final var notificationKafkaStreams = new KafkaStreams(streamsBuilder.build(), props);
      notificationKafkaStreams.cleanUp();
      notificationKafkaStreams.start();
      Runtime.getRuntime().addShutdownHook(new Thread(notificationKafkaStreams::close));
      LOGGER.info("Notifications started");
   }

}
