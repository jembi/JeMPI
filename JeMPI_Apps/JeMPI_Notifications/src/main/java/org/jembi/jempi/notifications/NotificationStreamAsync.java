package org.jembi.jempi.notifications;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import org.apache.kafka.common.serialization.Serdes;
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

import java.util.Properties;

public class NotificationStreamAsync {

   private static final Logger LOGGER = LogManager.getLogger(NotificationStreamAsync.class);
   private KafkaStreams journalKafkaStreams;

   private void processEntry(ActorSystem<Void> system,
                             final ActorRef<BackEnd.Event> backEnd,
                             String key,
                             Notification notification) {
      LOGGER.info("{}", notification);

   }

   public void open(final ActorSystem<Void> system, final ActorRef<BackEnd.Event> backEnd) {
      LOGGER.info("Notification Stream Processor");
      final Properties props = loadConfig();
      final var stringSerde = Serdes.String();
      final var batchEntitySerde = Serdes.serdeFrom(
            new JsonPojoSerializer<>(),
            new JsonPojoDeserializer<>(Notification.class));
      final StreamsBuilder streamsBuilder = new StreamsBuilder();
      final KStream<String, Notification> entitiesStream = streamsBuilder.stream(
            GlobalConstants.TOPIC_NOTIFICATIONS,
            Consumed.with(stringSerde, batchEntitySerde));
      entitiesStream.foreach((key, notification) -> processEntry(system, backEnd, key, notification));
      journalKafkaStreams = new KafkaStreams(streamsBuilder.build(), props);
      journalKafkaStreams.cleanUp();
      journalKafkaStreams.start();
      LOGGER.info("KafkaStreams started");
   }

   public void close(ActorSystem<Void> actorSystem) {
      LOGGER.warn("Stream closed");
      final var name = actorSystem.name();
      LOGGER.info(name);
      journalKafkaStreams.close();
   }

   private Properties loadConfig() {
      final Properties props = new Properties();
      props.put(StreamsConfig.APPLICATION_ID_CONFIG, AppConfig.KAFKA_APPLICATION_ID_NOTIFICATIONS);
      props.put(StreamsConfig.CLIENT_ID_CONFIG, AppConfig.KAFKA_CLIENT_ID_NOTIFICATIONS);
      props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, AppConfig.KAFKA_BOOTSTRAP_SERVERS);
      return props;
   }

}
