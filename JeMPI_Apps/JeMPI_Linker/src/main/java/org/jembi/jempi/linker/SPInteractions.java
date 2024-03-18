package org.jembi.jempi.linker;

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
import org.jembi.jempi.linker.backend.BackEnd;
import org.jembi.jempi.shared.models.CustomMU;
import org.jembi.jempi.shared.models.InteractionEnvelop;
import org.jembi.jempi.shared.serdes.JsonPojoDeserializer;
import org.jembi.jempi.shared.serdes.JsonPojoSerializer;

import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.jembi.jempi.shared.models.InteractionEnvelop.ContentType.BATCH_END_SENTINEL;
import static org.jembi.jempi.shared.models.InteractionEnvelop.ContentType.BATCH_INTERACTION;

public final class SPInteractions {

   private static final Logger LOGGER = LogManager.getLogger(SPInteractions.class);
   private final String topic;
   private KafkaStreams interactionEnvelopKafkaStreams;

   private SPInteractions(final String topic_) {
      LOGGER.info("SPInteractions constructor");
      this.topic = topic_;
   }

   public static SPInteractions create(final String topic_) {
      return new SPInteractions(topic_);
   }

   private void linkPatient(
         final ActorSystem<Void> system,
         final ActorRef<BackEnd.Request> backEnd,
         final String key,
         final InteractionEnvelop interactionEnvelop) {

/*
      if (interactionEnvelop.contentType() == InteractionEnvelop.ContentType.BATCH_START_SENTINEL
              || interactionEnvelop.contentType() == BATCH_END_SENTINEL) {
         final var completableFuture = Ask.runStartEndHooks(system, backEnd, key, interactionEnvelop).toCompletableFuture();
         try {
            List<MpiGeneralError> hookErrors = completableFuture.get(65, TimeUnit.SECONDS).hooksResults();
            if (!hookErrors.isEmpty()) {
               LOGGER.error(hookErrors);
            }
         } catch (InterruptedException | ExecutionException | TimeoutException ex) {
            LOGGER.error(ex.getLocalizedMessage(), ex);
            this.close();
         }
      }
*/

      if (interactionEnvelop.contentType() != BATCH_INTERACTION) {

         return;
      }
      final var completableFuture = Ask.linkInteraction(system, backEnd, key, interactionEnvelop).toCompletableFuture();
      try {
         final var reply = completableFuture.get(65, TimeUnit.SECONDS);
         if (reply.linkInfo() == null) {
            LOGGER.error("BACK END RESPONSE(ERROR)");
         }
      } catch (InterruptedException | ExecutionException | TimeoutException ex) {
         LOGGER.error(ex.getLocalizedMessage(), ex);
         this.close();
      }

   }

   public void open(
         final ActorSystem<Void> system,
         final ActorRef<BackEnd.Request> backEnd) {
      LOGGER.info("SPInteractions Stream Processor");
      final Properties props = loadConfig();
      final var stringSerde = Serdes.String();
      final var interactionEnvelopSerde = Serdes.serdeFrom(new JsonPojoSerializer<>(),
                                                           new JsonPojoDeserializer<>(InteractionEnvelop.class));
      final StreamsBuilder streamsBuilder = new StreamsBuilder();
      final KStream<String, InteractionEnvelop> interactionStream =
            streamsBuilder.stream(topic, Consumed.with(stringSerde, interactionEnvelopSerde));
      interactionStream.foreach((key, interactionEnvelop) -> {
         linkPatient(system, backEnd, key, interactionEnvelop);
         if (!CustomMU.SEND_INTERACTIONS_TO_EM && interactionEnvelop.contentType() == BATCH_END_SENTINEL) {
            this.close();
         }
      });
      interactionEnvelopKafkaStreams = new KafkaStreams(streamsBuilder.build(), props);
      interactionEnvelopKafkaStreams.cleanUp();
      interactionEnvelopKafkaStreams.start();
      LOGGER.info("KafkaStreams started");
   }

   private void close() {
      LOGGER.info("Stream closed");
      interactionEnvelopKafkaStreams.close(new KafkaStreams.CloseOptions().leaveGroup(true));
   }

   private Properties loadConfig() {
      final Properties props = new Properties();
      props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, AppConfig.KAFKA_BOOTSTRAP_SERVERS);
      props.put(StreamsConfig.APPLICATION_ID_CONFIG, AppConfig.KAFKA_APPLICATION_ID_INTERACTIONS + topic);
      return props;
   }

}
