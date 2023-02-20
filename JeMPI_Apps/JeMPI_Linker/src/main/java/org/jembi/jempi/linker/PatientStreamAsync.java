package org.jembi.jempi.linker;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.AskPattern;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.AppConfig;
import org.jembi.jempi.shared.models.BatchPatientRecord;
import org.jembi.jempi.shared.models.GlobalConstants;
import org.jembi.jempi.shared.serdes.JsonPojoDeserializer;
import org.jembi.jempi.shared.serdes.JsonPojoSerializer;

import java.util.Properties;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public final class PatientStreamAsync {

   private static final Logger LOGGER = LogManager.getLogger(PatientStreamAsync.class);
   private KafkaStreams patientKafkaStreams;

   private PatientStreamAsync() {
      LOGGER.info("PatientStreamAsync constructor");
   }

   public static PatientStreamAsync create() {
      return new PatientStreamAsync();
   }

   private void linkPatient(
         final ActorSystem<Void> system,
         final ActorRef<BackEnd.Event> backEnd,
         final String key,
         final BatchPatientRecord batchPatientRecord) {
      if (batchPatientRecord.batchType() != BatchPatientRecord.BatchType.BATCH_PATIENT) {
         return;
      }
      LOGGER.debug("{}", batchPatientRecord.patientRecord());
      final CompletionStage<BackEnd.EventLinkPatientAsyncRsp> result =
            AskPattern.ask(
                  backEnd,
                  replyTo -> new BackEnd.EventLinkPatientAsyncReq(key, batchPatientRecord, replyTo),
                  java.time.Duration.ofSeconds(60),
                  system.scheduler());
      final var completableFuture = result.toCompletableFuture();
      try {
         final var reply = completableFuture.get(65, TimeUnit.SECONDS);
         if (reply.linkInfo() == null) {
            LOGGER.error("BACK END RESPONSE(ERROR)");
         }
      } catch (InterruptedException | ExecutionException | TimeoutException ex) {
         LOGGER.error(ex.getLocalizedMessage(), ex);
         close();
      }
   }

   public void open(
         final ActorSystem<Void> system,
         final ActorRef<BackEnd.Event> backEnd) {
      LOGGER.info("EM Stream Processor");
      final Properties props = loadConfig();
      final var stringSerde = Serdes.String();
      final var batchPatientRecordSerde = Serdes.serdeFrom(
            new JsonPojoSerializer<>(),
            new JsonPojoDeserializer<>(BatchPatientRecord.class));
      final StreamsBuilder streamsBuilder = new StreamsBuilder();
      final KStream<String, BatchPatientRecord> patientsStream = streamsBuilder.stream(
            GlobalConstants.TOPIC_PATIENT_LINKER,
            Consumed.with(stringSerde, batchPatientRecordSerde));
      patientsStream.foreach((key, patient) -> linkPatient(system, backEnd, key, patient));
      patientKafkaStreams = new KafkaStreams(streamsBuilder.build(), props);
      patientKafkaStreams.cleanUp();
      patientKafkaStreams.start();
      LOGGER.info("KafkaStreams started");
   }

   public void close() {
      LOGGER.warn("Stream closed");
      patientKafkaStreams.close();
   }

   private Properties loadConfig() {
      final Properties props = new Properties();
      props.put(StreamsConfig.APPLICATION_ID_CONFIG, AppConfig.KAFKA_APPLICATION_ID_ENTITIES);
      props.put(StreamsConfig.CLIENT_ID_CONFIG, AppConfig.KAFKA_CLIENT_ID_ENTITIES);
      props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, AppConfig.KAFKA_BOOTSTRAP_SERVERS);
      return props;
   }

}
