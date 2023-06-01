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
import org.jembi.jempi.shared.models.*;
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
      final Serializer<AsyncSourceRecord> sourceRecordSerializer = new JsonPojoSerializer<>();
      final Deserializer<AsyncSourceRecord> sourceRecordDeserializer = new JsonPojoDeserializer<>(
            AsyncSourceRecord.class);
      final Serializer<BatchInteraction> batchInteractiontSerializer = new JsonPojoSerializer<>();
      final Deserializer<BatchInteraction> batchInteractionDeserializer = new JsonPojoDeserializer<>(BatchInteraction.class);
      final Serde<AsyncSourceRecord> sourceRecordSerde = Serdes.serdeFrom(sourceRecordSerializer, sourceRecordDeserializer);
      final Serde<BatchInteraction> batchInteractionSerde =
            Serdes.serdeFrom(batchInteractiontSerializer, batchInteractionDeserializer);
      final StreamsBuilder streamsBuilder = new StreamsBuilder();
      final KStream<String, AsyncSourceRecord> patientKStream = streamsBuilder.stream(
            GlobalConstants.TOPIC_INTERACTION_ASYNC_ETL, Consumed.with(stringSerde, sourceRecordSerde));
      patientKStream
            .map((key, rec) -> {
               LOGGER.info("{} : {}", key, rec);
               var batchType = switch (rec.recordType().type) {
                  case AsyncSourceRecord.RecordType.BATCH_START_VALUE -> BatchInteraction.BatchType.BATCH_START;
                  case AsyncSourceRecord.RecordType.BATCH_END_VALUE -> BatchInteraction.BatchType.BATCH_END;
                  default -> BatchInteraction.BatchType.BATCH_PATIENT;
               };
               if (batchType == BatchInteraction.BatchType.BATCH_PATIENT) {
                  var batchPatient = new BatchInteraction(
                        batchType,
                        rec.batchMetaData(),
                        rec.customSourceRecord().stan(),
                        new Interaction(null,
                                        new SourceId(null,
                                                     FACILITY.get(random.nextInt(FACILITY.size())),
                                                     StringUtils.isNotBlank(rec.customSourceRecord().nationalID())
                                                           ? rec.customSourceRecord().nationalID()
                                                           : "ANON"),
                                        new CustomDemographicData(rec.customSourceRecord().auxId(),
                                                                  rec.customSourceRecord().givenName().replace("'", ""),
                                                                  rec.customSourceRecord().familyName().replace("'", ""),
                                                                  rec.customSourceRecord().gender().replace("'", ""),
                                                                  rec.customSourceRecord().dob().replace("'", ""),
                                                                  rec.customSourceRecord().city().replace("'", ""),
                                                                  rec.customSourceRecord().phoneNumber().replace("'", ""),
                                                                  rec.customSourceRecord().nationalID().replace("'", ""))));
                  return KeyValue.pair(key, batchPatient);
               } else {
                  return KeyValue.pair("SENTINEL", new BatchInteraction(batchType, rec.batchMetaData(), null, null));
               }
            })
            .filter((key, value) -> !(value.batchType() == BatchInteraction.BatchType.BATCH_PATIENT && StringUtils.isBlank(
                  value.interaction().demographicData().auxId)))
            .to(GlobalConstants.TOPIC_INTERACTION_CONTROLLER, Produced.with(stringSerde, batchInteractionSerde));
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
      props.put(StreamsConfig.APPLICATION_ID_CONFIG, AppConfig.KAFKA_APPLICATION_ID);
      props.put(StreamsConfig.CLIENT_ID_CONFIG, AppConfig.KAFKA_CLIENT_ID);
      props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, AppConfig.KAFKA_BOOTSTRAP_SERVERS);
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
