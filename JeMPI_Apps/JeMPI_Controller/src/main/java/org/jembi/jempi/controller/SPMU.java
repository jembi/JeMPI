package org.jembi.jempi.controller;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import com.fasterxml.jackson.core.JsonProcessingException;
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
import org.jembi.jempi.shared.models.CustomMU;
import org.jembi.jempi.shared.models.GlobalConstants;
import org.jembi.jempi.shared.serdes.JsonPojoDeserializer;
import org.jembi.jempi.shared.serdes.JsonPojoSerializer;
import org.jembi.jempi.shared.utils.AppUtils;

import java.util.Properties;

public final class SPMU {

   private static final Logger LOGGER = LogManager.getLogger(SPMU.class);
   private KafkaStreams customMUKafkaStreams = null;

   void open(
         final ActorSystem<Void> system,
         final ActorRef<BackEnd.Event> backEnd) {
      LOGGER.info("Stream Processor");
      final Properties props = loadConfig();
      final Serde<String> stringSerde = Serdes.String();
      final Serializer<CustomMU> customMUSerializer = new JsonPojoSerializer<>();
      final Deserializer<CustomMU> customMUDeserializer = new JsonPojoDeserializer<>(CustomMU.class);
      final Serde<CustomMU> customMUSerde = Serdes.serdeFrom(customMUSerializer, customMUDeserializer);
      final StreamsBuilder streamsBuilder = new StreamsBuilder();
      final KStream<String, CustomMU> customMUKStream = streamsBuilder.stream(GlobalConstants.TOPIC_MU_CONTROLLER,
                                                                              Consumed.with(stringSerde, customMUSerde));
      customMUKStream
            .peek((key, customMU) -> {
               try {
                  LOGGER.debug(AppUtils.OBJECT_MAPPER.writeValueAsString(customMU));
               } catch (JsonProcessingException e) {
                  LOGGER.error(e.getLocalizedMessage(), e);
               }
            })
            .to(GlobalConstants.TOPIC_MU_LINKER);
      customMUKafkaStreams = new KafkaStreams(streamsBuilder.build(), props);
      customMUKafkaStreams.cleanUp();
      customMUKafkaStreams.start();
      Runtime.getRuntime().addShutdownHook(new Thread(customMUKafkaStreams::close));
      LOGGER.info("KafkaStreams started");
   }

   public void close() {
      customMUKafkaStreams.close();
   }

   private Properties loadConfig() {
      final Properties props = new Properties();
      props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, AppConfig.KAFKA_BOOTSTRAP_SERVERS);
      props.put(StreamsConfig.APPLICATION_ID_CONFIG, AppConfig.KAFKA_APPLICATION_ID + "-MU");
      props.put(StreamsConfig.POLL_MS_CONFIG, 10);
      return props;
   }


}
