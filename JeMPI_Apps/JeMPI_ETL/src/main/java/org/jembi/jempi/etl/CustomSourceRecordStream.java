package org.jembi.jempi.etl;

import org.apache.commons.codec.language.DoubleMetaphone;
import org.apache.commons.codec.language.Metaphone;
import org.apache.commons.codec.language.RefinedSoundex;
import org.apache.commons.codec.language.Soundex;
import org.apache.commons.lang3.StringUtils;
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
import org.jembi.jempi.AppConfig;
import org.jembi.jempi.shared.models.GlobalConstants;
import org.jembi.jempi.shared.models.Interaction;
import org.jembi.jempi.shared.models.InteractionEnvelop;
import org.jembi.jempi.shared.models.SourceId;
import org.jembi.jempi.shared.serdes.JsonPojoDeserializer;
import org.jembi.jempi.shared.serdes.JsonPojoSerializer;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class CustomSourceRecordStream {

   private static final Logger LOGGER = LogManager.getLogger(CustomSourceRecordStream.class);
   private static final List<String> FACILITY = Arrays.asList("CLINIC", "PHARMACY", "LABORATORY");
   private final Random random = new Random(1234);
   ExecutorService executorService = Executors.newFixedThreadPool(1);
   private KafkaStreams interactionKafkaStreams = null;

   public void open() {

      final Properties props = loadConfig();
      final Serde<String> stringSerde = Serdes.String();
      final Serializer<InteractionEnvelop> interactionEnvelopSerializer = new JsonPojoSerializer<>();
      final Deserializer<InteractionEnvelop> interactionEnvelopDeserializer =
            new JsonPojoDeserializer<>(InteractionEnvelop.class);
      final Serde<InteractionEnvelop> interactionEnvelopSerde =
            Serdes.serdeFrom(interactionEnvelopSerializer, interactionEnvelopDeserializer);
      final StreamsBuilder streamsBuilder = new StreamsBuilder();
      final KStream<String, InteractionEnvelop> sourceKStream =
            streamsBuilder.stream(GlobalConstants.TOPIC_INTERACTION_ASYNC_ETL,
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
                                        new SourceId(null,
                                                     FACILITY.get(random.nextInt(FACILITY.size())),
                                                     StringUtils.isNotBlank(demographicData.nationalId)
                                                           ? demographicData.nationalId
                                                           : "ANON"),
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

   private String getEncodedMF(
         final String value,
         final OperationType algorithmType) {
      return switch (algorithmType) {
         case OPERATION_TYPE_METAPHONE -> (new Metaphone()).metaphone(value);
         case OPERATION_TYPE_DOUBLE_METAPHONE -> (new DoubleMetaphone()).doubleMetaphone(value);
         case OPERATION_TYPE_SOUNDEX -> (new Soundex()).encode(value);
         case OPERATION_TYPE_REFINED_SOUNDEX -> (new RefinedSoundex()).encode(value);
      };
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

   public enum OperationType {
      OPERATION_TYPE_METAPHONE,
      OPERATION_TYPE_DOUBLE_METAPHONE,
      OPERATION_TYPE_SOUNDEX,
      OPERATION_TYPE_REFINED_SOUNDEX
   }

}
