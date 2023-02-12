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

public class CustomSourceRecordStream {

   private static final Logger LOGGER = LogManager.getLogger(CustomSourceRecordStream.class);
   private static final List<String> FACILITY = Arrays.asList("CLINIC", "PHARMACY", "LABORATORY");
   private final Random random = new Random(1234);
   ExecutorService executorService = Executors.newFixedThreadPool(1);
   private KafkaStreams patientKafkaStreams = null;

   public void open() {

      final Properties props = loadConfig();
      final Serde<String> stringSerde = Serdes.String();
      final Serializer<CustomSourceRecord> customSourceRecordSerializer = new JsonPojoSerializer<>();
      final Deserializer<CustomSourceRecord> customSourceRecordDeserializer = new JsonPojoDeserializer<>(
            CustomSourceRecord.class);
      final Serializer<BatchPatientRecord> batchPatientSerializer = new JsonPojoSerializer<>();
      final Deserializer<BatchPatientRecord> batchPatientDeserializer = new JsonPojoDeserializer<>(BatchPatientRecord.class);
      final Serde<CustomSourceRecord> customSourceRecordSerde = Serdes.serdeFrom(customSourceRecordSerializer,
                                                                                 customSourceRecordDeserializer);
      final Serde<BatchPatientRecord> batchPatientSerde = Serdes.serdeFrom(batchPatientSerializer, batchPatientDeserializer);
      final StreamsBuilder streamsBuilder = new StreamsBuilder();
      final KStream<String, CustomSourceRecord> patientKStream = streamsBuilder.stream(
            GlobalConstants.TOPIC_PATIENT_ASYNC_ETL, Consumed.with(stringSerde, customSourceRecordSerde));
      patientKStream
            .map((key, rec) -> {
               var k = rec.familyName();
               if (StringUtils.isBlank(k)) {
                  k = "anon";
               }
               k = getEncodedMF(k, OperationType.OPERATION_TYPE_DOUBLE_METAPHONE);
               LOGGER.info("{} : {}", k, rec);
               var batchType = switch (rec.recordType().type) {
                  case CustomSourceRecord.RecordType.BATCH_START_VALUE -> BatchPatientRecord.BatchType.BATCH_START;
                  case CustomSourceRecord.RecordType.BATCH_END_VALUE -> BatchPatientRecord.BatchType.BATCH_END;
                  default -> BatchPatientRecord.BatchType.BATCH_PATIENT;
               };
               var batchPatient = new BatchPatientRecord(
                     batchType,
                     rec.stan(),
                     new PatientRecord(null,
                                       new SourceId(null,
                                                    FACILITY.get(random.nextInt(FACILITY.size())),
                                                    StringUtils.isNotBlank(rec.nationalID())
                                                          ? rec.nationalID()
                                                          : "ANON"),
                                       new CustomDemographicData(rec.auxId(),
                                                                 rec.givenName(),
                                                                 rec.familyName(),
                                                                 rec.gender(),
                                                                 rec.dob(),
                                                                 rec.city(),
                                                                 rec.phoneNumber(),
                                                                 rec.nationalID())));
               return KeyValue.pair(k, batchPatient);
            })
            .filter((key, value) -> !(value.batchType() == BatchPatientRecord.BatchType.BATCH_PATIENT && StringUtils.isBlank(
                  value.patientRecord().demographicData().auxId())))
            .to(GlobalConstants.TOPIC_PATIENT_CONTROLLER, Produced.with(stringSerde, batchPatientSerde));
      patientKafkaStreams = new KafkaStreams(streamsBuilder.build(), props);
      patientKafkaStreams.cleanUp();
      patientKafkaStreams.start();
   }

   private String getEncodedMF(
         String value,
         OperationType algorithmType) {
      return switch (algorithmType) {
         case OPERATION_TYPE_METAPHONE -> (new Metaphone()).metaphone(value);
         case OPERATION_TYPE_DOUBLE_METAPHONE -> (new DoubleMetaphone()).doubleMetaphone(value);
         case OPERATION_TYPE_SOUNDEX -> (new Soundex()).encode(value);
         case OPERATION_TYPE_REFINED_SOUNDEX -> (new RefinedSoundex()).encode(value);
      };
   }

   public void close() {
      patientKafkaStreams.close();
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
