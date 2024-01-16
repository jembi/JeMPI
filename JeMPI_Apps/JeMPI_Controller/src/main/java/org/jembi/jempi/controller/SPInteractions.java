package org.jembi.jempi.controller;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.kafka.common.serialization.*;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.processor.TopicNameExtractor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.AppConfig;
import org.jembi.jempi.shared.kafka.KafkaTopicManager;
import org.jembi.jempi.shared.kafka.MyKafkaProducer;
import org.jembi.jempi.shared.models.GlobalConstants;
import org.jembi.jempi.shared.models.InteractionEnvelop;
import org.jembi.jempi.shared.serdes.JsonPojoDeserializer;
import org.jembi.jempi.shared.serdes.JsonPojoSerializer;
import org.jembi.jempi.shared.utils.AppUtils;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

public final class SPInteractions {

   private static final Logger LOGGER = LogManager.getLogger(SPInteractions.class);
   private final TopicNameExtractor<String, InteractionEnvelop> topicNameExtractor = (key, value, recordContext) -> value.tag();
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
      final KStream<String, InteractionEnvelop> batchPatientRecordKStream =
            streamsBuilder.stream(GlobalConstants.TOPIC_INTERACTION_CONTROLLER,
                                  Consumed.with(stringSerde, batchPatientRecordSerde));
      topicEM = new MyKafkaProducer<>(AppConfig.KAFKA_BOOTSTRAP_SERVERS,
                                      GlobalConstants.TOPIC_INTERACTION_EM,
                                      new StringSerializer(),
                                      new JsonPojoSerializer<>(),
                                      AppConfig.KAFKA_CLIENT_ID);
      batchPatientRecordKStream
            .peek((key, batchPatient) -> {
               switch (batchPatient.contentType()) {
                  case BATCH_START_SENTINEL:
                     try {
                        LOGGER.debug("START SENTINEL {}", AppUtils.OBJECT_MAPPER.writeValueAsString(batchPatient));
                     } catch (JsonProcessingException e) {
                        LOGGER.error(e.getLocalizedMessage(), e);
                     }
                     var kafkaTopicManager = new KafkaTopicManager(AppConfig.KAFKA_BOOTSTRAP_SERVERS);
                     try {
                        kafkaTopicManager.createTopic(batchPatient.tag(), 1, (short) 1, 24 * 60 * 60, 4 * 1024 * 1024);
                     } catch (ExecutionException e) {
                        LOGGER.error(e.getLocalizedMessage(), e);
                     } catch (InterruptedException e) {
                        LOGGER.error(e.getLocalizedMessage(), e);
                     }
                     kafkaTopicManager.close();
                     break;
                  case BATCH_END_SENTINEL:
                     try {
                        LOGGER.debug("END SENTINEL {}", AppUtils.OBJECT_MAPPER.writeValueAsString(batchPatient));
                     } catch (JsonProcessingException e) {
                        LOGGER.error(e.getLocalizedMessage(), e);
                     }
                     break;
                  default:
                     break;
               }
               topicEM.produceAsync(key, batchPatient, ((metadata, exception) -> {
                  if (exception != null) {
                     LOGGER.error(exception.toString());
                  }
               }));
            })
            .to(topicNameExtractor /* GlobalConstants.TOPIC_INTERACTION_LINKER*/,
                Produced.with(stringSerde, batchPatientRecordSerde));
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
