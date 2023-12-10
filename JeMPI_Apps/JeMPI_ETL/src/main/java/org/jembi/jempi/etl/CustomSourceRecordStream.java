package org.jembi.jempi.etl;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.jembi.jempi.AppConfig;
import org.jembi.jempi.shared.models.GlobalConstants;
import org.jembi.jempi.shared.models.Interaction;
import org.jembi.jempi.shared.models.InteractionEnvelop;
import org.jembi.jempi.shared.serdes.JsonPojoDeserializer;
import org.jembi.jempi.shared.serdes.JsonPojoSerializer;

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class CustomSourceRecordStream {

   private static final Logger LOGGER = LogManager.getLogger(CustomSourceRecordStream.class);
   ExecutorService executorService = Executors.newFixedThreadPool(1);
   private KafkaStreams interactionKafkaStreams = null;

   public CustomSourceRecordStream() {
      Configurator.setLevel(this.getClass(), AppConfig.GET_LOG_LEVEL);
   }

   public void open() {

      final Properties props = loadConfig();
      final Serde<String> stringSerde = Serdes.String();
      final Serializer<InteractionEnvelop> interactionEnvelopSerializer = new JsonPojoSerializer<>();
      final Deserializer<InteractionEnvelop> interactionEnvelopDeserializer = new JsonPojoDeserializer<>(
            InteractionEnvelop.class);
      final Serde<InteractionEnvelop> interactionEnvelopSerde = Serdes.serdeFrom(interactionEnvelopSerializer,
            interactionEnvelopDeserializer);
      final StreamsBuilder streamsBuilder = new StreamsBuilder();
      final KStream<String, InteractionEnvelop> sourceKStream = streamsBuilder.stream(
            GlobalConstants.TOPIC_INTERACTION_ETL,
            Consumed.with(stringSerde,
                  interactionEnvelopSerde));
      sourceKStream
            .map((key, rec) -> {
               if (rec.contentType() == InteractionEnvelop.ContentType.BATCH_INTERACTION) {
                  final var interaction = rec.interaction();
                  final var demographicData = interaction.demographicData();
                  final var newEnvelop = new InteractionEnvelop(
                        rec.contentType(),
                        rec.tag(),
                        rec.stan(),
                        new Interaction(null,
                              rec.interaction().sourceId(),
                              interaction.uniqueInteractionData(),
                              demographicData.clean()));
                  return KeyValue.pair(key, newEnvelop);
               } else {
                  return KeyValue.pair(key, rec);
               }
            })
            .to(GlobalConstants.TOPIC_INTERACTION_CONTROLLER, Produced.with(stringSerde, interactionEnvelopSerde));
      interactionKafkaStreams = new KafkaStreams(streamsBuilder.build(), props);
      interactionKafkaStreams.cleanUp();
      interactionKafkaStreams.start();
   }

   public void close() {
      interactionKafkaStreams.close();
   }

   private Properties loadConfig() {
      final Properties props = new Properties();
      props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, AppConfig.KAFKA_BOOTSTRAP_SERVERS);
      props.put(StreamsConfig.APPLICATION_ID_CONFIG, AppConfig.KAFKA_APPLICATION_ID);
      props.put(StreamsConfig.POLL_MS_CONFIG, 10);
      return props;
   }

}
