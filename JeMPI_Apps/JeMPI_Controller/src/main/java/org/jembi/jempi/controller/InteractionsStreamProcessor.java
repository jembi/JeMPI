package org.jembi.jempi.controller;

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
import org.jembi.jempi.shared.kafka.MyKafkaProducer;
import org.jembi.jempi.shared.models.GlobalConstants;
import org.jembi.jempi.shared.models.InteractionEnvelop;
import org.jembi.jempi.shared.serdes.JsonPojoDeserializer;
import org.jembi.jempi.shared.serdes.JsonPojoSerializer;

import java.util.Properties;

public final class InteractionsStreamProcessor {

   private static final Logger LOGGER = LogManager.getLogger(InteractionsStreamProcessor.class);
   private MyKafkaProducer<String, InteractionEnvelop> topicEM;
   private KafkaStreams interactionKafkaStreams = null;

   void open(
         final ActorSystem<Void> system,
         final ActorRef<BackEnd.Event> backEnd) {
      LOGGER.info("Stream Processor");

      final Properties props = loadConfig();

      final Serde<String> stringSerde = Serdes.String();
      final Serializer<InteractionEnvelop> batchPatientRecordSerializer = new JsonPojoSerializer<>();
      final Deserializer<InteractionEnvelop> batchPatientRecordDeserializer =
            new JsonPojoDeserializer<>(InteractionEnvelop.class);
      final Serde<InteractionEnvelop> batchPatientRecordSerde =
            Serdes.serdeFrom(batchPatientRecordSerializer, batchPatientRecordDeserializer);
      final StreamsBuilder streamsBuilder = new StreamsBuilder();
      final KStream<String, InteractionEnvelop> batchPatientRecordKStream = streamsBuilder.stream(
            GlobalConstants.TOPIC_INTERACTION_CONTROLLER,
            Consumed.with(stringSerde, batchPatientRecordSerde));
      topicEM = new MyKafkaProducer<>(AppConfig.KAFKA_BOOTSTRAP_SERVERS,
                                      GlobalConstants.TOPIC_INTERACTION_EM,
                                      new StringSerializer(), new JsonPojoSerializer<>(),
                                      AppConfig.KAFKA_CLIENT_ID);
      batchPatientRecordKStream
            .peek((key, batchPatient) -> {
               LOGGER.info("{}/{}", key, batchPatient);
               topicEM.produceAsync(key, batchPatient, ((metadata, exception) -> {
                  if (exception != null) {
                     LOGGER.error(exception.toString());
                  }
               }));
            })
            .to(GlobalConstants.TOPIC_INTERACTION_LINKER, Produced.with(stringSerde, batchPatientRecordSerde));
      interactionKafkaStreams = new KafkaStreams(streamsBuilder.build(), props);
      interactionKafkaStreams.cleanUp();
      interactionKafkaStreams.start();
      Runtime.getRuntime().addShutdownHook(new Thread(interactionKafkaStreams::close));
      LOGGER.info("KafkaStreams started");
   }

   public void close() {
      interactionKafkaStreams.close();
      topicEM.close();
   }

   private Properties loadConfig() {
      final Properties props = new Properties();
      props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, AppConfig.KAFKA_BOOTSTRAP_SERVERS);
      props.put(StreamsConfig.APPLICATION_ID_CONFIG, AppConfig.KAFKA_APPLICATION_ID + "-INTERACTIONS");
      props.put(StreamsConfig.POLL_MS_CONFIG, 10);
      return props;
   }


}
