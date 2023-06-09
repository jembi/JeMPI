package org.jembi.jempi.libapi;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class NotificationStreamProcessor {

   private static final Logger LOGGER = LogManager.getLogger(NotificationStreamProcessor.class);
   private final KafkaStreams notificationKafkaStreams = null;
   private PsqlNotifications psqlNotifications;

//   public void open(
//         final String pgDatabase,
//         final String pgPassword,
//         final String kafkaApplicationId,
//         final String kafkaBootstrapServers) {
//      LOGGER.info("Stream Processor");
//      psqlQueries = new PsqlQueries(pgDatabase);
//      final Properties props = new Properties();
//      props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers);
//      props.put(StreamsConfig.APPLICATION_ID_CONFIG, kafkaApplicationId);
//      props.put(StreamsConfig.POLL_MS_CONFIG, 50);
//
//      final Serde<String> stringSerde = Serdes.String();
//      final Serializer<Notification> notificationSerializer = new JsonPojoSerializer<>();
//      final Deserializer<Notification> notificationDeserializer = new JsonPojoDeserializer<>(Notification.class);
//      final Serde<Notification> notificationSerde = Serdes.serdeFrom(notificationSerializer, notificationDeserializer);
//      final StreamsBuilder streamsBuilder = new StreamsBuilder();
//      final KStream<String, Notification> notificationStream = streamsBuilder.stream(
//            GlobalConstants.TOPIC_NOTIFICATIONS,
//            Consumed.with(stringSerde, notificationSerde));
//      notificationStream
//            .foreach((key, value) -> {
//               try {
//
//                  LOGGER.debug("key:{}, value:{}", key, value);
//                  UUID id = UUID.randomUUID();
//                  psqlQueries.insert(pgPassword,
//                                     id,
//                                     value.notificationType().toString(),
//                                     value.patientNames(),
//                                     value.linkedTo().score(),
//                                     value.timeStamp(),
//                                     value.linkedTo().gID(),
//                                     value.dID());
//
//                  for (int i = 0; i < value.candidates().size(); i++) {
//                     psqlQueries.insertCandidates(pgPassword,
//                                                  id,
//                                                  value.candidates().get(i).score(),
//                                                  value.candidates().get(i).gID());
//                  }
//               } catch (SQLException e) {
//                  LOGGER.debug(e.toString());
//               }
//               LOGGER.debug("Linked To data : {}", value.linkedTo());
//               LOGGER.debug("Candidates data : {}", value.candidates().get(0).gID());
//            });
//
//      notificationKafkaStreams = new KafkaStreams(streamsBuilder.build(), props);
//      notificationKafkaStreams.cleanUp();
//      notificationKafkaStreams.start();
//      Runtime.getRuntime().addShutdownHook(new Thread(notificationKafkaStreams::close));
//      LOGGER.info("Notifications started");
//   }

}
