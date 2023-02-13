package org.jembi.jempi.linker;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.AskPattern;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.AppConfig;
import org.jembi.jempi.shared.models.CustomMU;
import org.jembi.jempi.shared.models.GlobalConstants;
import org.jembi.jempi.shared.serdes.JsonPojoDeserializer;
import org.jembi.jempi.shared.serdes.JsonPojoSerializer;

import java.util.Properties;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public final class FrontEndMUStream {

   private static final Logger LOGGER = LogManager.getLogger(FrontEndMUStream.class);
   private KafkaStreams muKafkaStreams;

   FrontEndMUStream() {
      LOGGER.info("FrontEndMUStream constructor");
   }

   void installMU(
         final ActorSystem<Void> system,
         final ActorRef<BackEnd.Event> backEnd,
         final String key,
         final CustomMU mu) {
      LOGGER.info("New MU set: {}/{}", key, mu);
      final CompletionStage<BackEnd.EventUpdateMURsp> result =
            AskPattern.ask(
                  backEnd,
                  replyTo -> new BackEnd.EventUpdateMUReq(mu, replyTo),
                  java.time.Duration.ofSeconds(5),
                  system.scheduler());
      final var completableFuture = result.toCompletableFuture();
      try {
         final var reply = completableFuture.get(6, TimeUnit.SECONDS);
         if (!reply.rc()) {
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
      LOGGER.info("MY Stream Processor");
      final Properties props = loadConfig();
      final Serde<String> stringSerde = Serdes.String();
      final Serde<CustomMU> muSerde = Serdes.serdeFrom(new JsonPojoSerializer<>(),
                                                       new JsonPojoDeserializer<>(CustomMU.class));
      final StreamsBuilder streamsBuilder = new StreamsBuilder();
      final KStream<String, CustomMU> muStream = streamsBuilder.stream(
            GlobalConstants.TOPIC_MU_LINKER,
            Consumed.with(stringSerde, muSerde));
      muStream.foreach((key, mu) -> installMU(system, backEnd, key, mu));
      muKafkaStreams = new KafkaStreams(streamsBuilder.build(), props);
      muKafkaStreams.cleanUp();
      muKafkaStreams.start();
      LOGGER.info("KafkaStreams started");
   }

   public void close() {
      LOGGER.warn("Stream closed");
      muKafkaStreams.close();
   }

   private Properties loadConfig() {
      final Properties props = new Properties();
      props.put(StreamsConfig.APPLICATION_ID_CONFIG, AppConfig.KAFKA_APPLICATION_ID_MU);
      props.put(StreamsConfig.CLIENT_ID_CONFIG, AppConfig.KAFKA_CLIENT_ID_MU);
      props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, AppConfig.KAFKA_BOOTSTRAP_SERVERS);
      return props;
   }

}
