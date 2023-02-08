package org.jembi.jempi.pre_processor;

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

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Properties;

import static org.apache.commons.codec.digest.DigestUtils.sha256;

public class CustomSourceRecordStream {

   private static final Logger LOGGER = LogManager.getLogger(CustomSourceRecordStream.class);
   private KafkaStreams patientKafkaStreams = null;

/*
                                 FP_CLEAR
                                    |
                                    V
              +--------+   KEY    +-----+
   EMR_ID --> | SHA256 | -------> | AES | --> FP_CIPHER
              +--------+          +-----+

*/

   private String getNationalFingerprintID(final String emr, final String emrFingerPrint) {
      LOGGER.debug("EMR FP: {} {}", emr, emrFingerPrint);
      if (StringUtils.isBlank(emr) || StringUtils.isBlank(emrFingerPrint)) {
         return null;
      }
      final var cipherText = Base64.getDecoder().decode(emrFingerPrint);
      final var key = sha256(emr);
      try {
         final var secretKey = new SecretKeySpec(key, "AES");
         final var cipher = Cipher.getInstance("AES/ECB/NoPadding");
         cipher.init(Cipher.DECRYPT_MODE, secretKey);
         return new String(cipher.doFinal(cipherText), StandardCharsets.UTF_8);
      } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException |
               BadPaddingException ex) {
         LOGGER.error(ex.getLocalizedMessage(), ex);
      }
      return null;
   }

   public void open() {

      final Properties props = loadConfig();
      final Serde<String> stringSerde = Serdes.String();
      final Serializer<CustomSourceRecord> customSourceRecordSerializer = new JsonPojoSerializer<>();
      final Deserializer<CustomSourceRecord> customSourceRecordDeserializer = new JsonPojoDeserializer<>(
            CustomSourceRecord.class);
      final Serializer<BatchEntity> batchEntitySerializer = new JsonPojoSerializer<>();
      final Deserializer<BatchEntity> batchEntityDeserializer = new JsonPojoDeserializer<>(BatchEntity.class);
      final Serde<CustomSourceRecord> customSourceRecordSerde = Serdes.serdeFrom(customSourceRecordSerializer,
                                                                                 customSourceRecordDeserializer);
      final Serde<BatchEntity> batchEntitySerde = Serdes.serdeFrom(batchEntitySerializer, batchEntityDeserializer);
      final StreamsBuilder streamsBuilder = new StreamsBuilder();
      final KStream<String, CustomSourceRecord> patientKStream = streamsBuilder.stream(
            GlobalConstants.TOPIC_PATIENT_ASYNC_PREPROCESSOR, Consumed.with(stringSerde, customSourceRecordSerde));
      patientKStream
            .map((key, rec) -> {
               var k = rec.dob();
               if (StringUtils.isBlank(k)) {
                  k = "missing";
               }
               k = getEncodedMF(k, OperationType.OPERATION_TYPE_DOUBLE_METAPHONE);
               LOGGER.info("{} : {}", k, rec);
               var entityType = switch (rec.recordType().type) {
                  case CustomSourceRecord.RecordType.BATCH_START_VALUE -> BatchEntity.EntityType.BATCH_START;
                  case CustomSourceRecord.RecordType.BATCH_END_VALUE -> BatchEntity.EntityType.BATCH_END;
                  default -> BatchEntity.EntityType.BATCH_RECORD;
               };
               var entity = new BatchEntity(
                     entityType,
                     rec.stan(),
               new CustomEntity(null,
                                new SourceId(null, rec.EMR(), rec.pID()),
                                rec.auxId(),
                                getNationalFingerprintID(rec.EMR(), rec.fID()),
                                rec.fID(),
                                rec.gender(),
                                rec.dob()));
               return KeyValue.pair(k, entity);
            })
            .filter((key, value) -> !(value.entityType() == BatchEntity.EntityType.BATCH_RECORD && StringUtils.isBlank(
                  value.entity().auxId())))
            .to(GlobalConstants.TOPIC_PATIENT_CONTROLLER, Produced.with(stringSerde, batchEntitySerde));
      patientKafkaStreams = new KafkaStreams(streamsBuilder.build(), props);
      patientKafkaStreams.cleanUp();
      patientKafkaStreams.start();
   }

   private String getEncodedMF(String value, OperationType algorithmType) {
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
