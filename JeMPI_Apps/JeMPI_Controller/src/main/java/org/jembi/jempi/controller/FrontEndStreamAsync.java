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
import org.jembi.jempi.shared.models.BatchPatientRecord;
import org.jembi.jempi.shared.models.GlobalConstants;
import org.jembi.jempi.shared.serdes.JsonPojoDeserializer;
import org.jembi.jempi.shared.serdes.JsonPojoSerializer;

import java.util.Properties;

public class FrontEndStreamAsync {

   private static final Logger LOGGER = LogManager.getLogger(FrontEndStreamAsync.class);
   private MyKafkaProducer<String, BatchPatientRecord> topicEM;
   private KafkaStreams patientKafkaStreams = null;

   void open(
         final ActorSystem<Void> system,
         final ActorRef<BackEnd.Event> backEnd) {
      LOGGER.info("Stream Processor");

      final Properties props = loadConfig();

      final Serde<String> stringSerde = Serdes.String();
      final Serializer<BatchPatientRecord> batchPatientRecordSerializer = new JsonPojoSerializer<>();
      final Deserializer<BatchPatientRecord> batchPatientRecordDeserializer =
            new JsonPojoDeserializer<>(BatchPatientRecord.class);
      final Serde<BatchPatientRecord> batchPatientRecordSerde =
            Serdes.serdeFrom(batchPatientRecordSerializer, batchPatientRecordDeserializer);
      final StreamsBuilder streamsBuilder = new StreamsBuilder();
      final KStream<String, BatchPatientRecord> batchPatientRecordKStream = streamsBuilder.stream(
            GlobalConstants.TOPIC_PATIENT_CONTROLLER,
            Consumed.with(stringSerde, batchPatientRecordSerde));
      topicEM = new MyKafkaProducer<>(GlobalConstants.TOPIC_PATIENT_EM,
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
            .to(GlobalConstants.TOPIC_PATIENT_LINKER, Produced.with(stringSerde, batchPatientRecordSerde));
      patientKafkaStreams = new KafkaStreams(streamsBuilder.build(), props);
      patientKafkaStreams.cleanUp();
      patientKafkaStreams.start();
      Runtime.getRuntime().addShutdownHook(new Thread(patientKafkaStreams::close));
      LOGGER.info("KafkaStreams started");
   }

   public void close() {
      patientKafkaStreams.close();
      topicEM.close();
   }

   private Properties loadConfig() {
      final Properties props = new Properties();
      props.put(StreamsConfig.APPLICATION_ID_CONFIG, AppConfig.KAFKA_APPLICATION_ID);
      props.put(StreamsConfig.CLIENT_ID_CONFIG, AppConfig.KAFKA_CLIENT_ID);
      props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, AppConfig.KAFKA_BOOTSTRAP_SERVERS);
      props.put(StreamsConfig.POLL_MS_CONFIG, 10);
      return props;
   }


}
