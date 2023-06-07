package org.jembi.jempi.controller;

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
import org.jembi.jempi.shared.models.AuditEvent;
import org.jembi.jempi.shared.models.GlobalConstants;
import org.jembi.jempi.shared.serdes.JsonPojoDeserializer;
import org.jembi.jempi.shared.serdes.JsonPojoSerializer;

import java.util.Properties;

public final class AuditTrailStreamProcessor {

   private static final Logger LOGGER = LogManager.getLogger(AuditTrailStreamProcessor.class);

   public void open() {
      LOGGER.info("Stream Processor");
      final var psqlAuditTrail = new PsqlAuditTrail(AppConfig.POSTGRESQL_DATABASE, AppConfig.POSTGRESQL_USER, AppConfig.POSTGRESQL_PASSWORD);
      psqlAuditTrail.createSchemas();
      final Properties props = new Properties();
      props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, AppConfig.KAFKA_BOOTSTRAP_SERVERS);
      props.put(StreamsConfig.APPLICATION_ID_CONFIG, AppConfig.KAFKA_APPLICATION_ID + "-AUDIT_TRAIL");
      props.put(StreamsConfig.POLL_MS_CONFIG, 50);
      final Serde<String> stringSerde = Serdes.String();
      final Serializer<AuditEvent> auditEventSerializer = new JsonPojoSerializer<>();
      final Deserializer<AuditEvent> auditEventDeserializer = new JsonPojoDeserializer<>(AuditEvent.class);
      final Serde<AuditEvent> auditEventSerde = Serdes.serdeFrom(auditEventSerializer, auditEventDeserializer);
      final StreamsBuilder streamsBuilder = new StreamsBuilder();
      final KStream<String, AuditEvent> auditEventKStream = streamsBuilder.stream(
            GlobalConstants.TOPIC_AUDIT_TRAIL,
            Consumed.with(stringSerde, auditEventSerde));
      auditEventKStream.foreach((key, value) -> psqlAuditTrail.addAuditEvent(value));
      final var auditTrailKafkaStreams = new KafkaStreams(streamsBuilder.build(), props);
      auditTrailKafkaStreams.cleanUp();
      auditTrailKafkaStreams.start();
      Runtime.getRuntime().addShutdownHook(new Thread(auditTrailKafkaStreams::close));
      LOGGER.info("Notifications started");
   }

}
