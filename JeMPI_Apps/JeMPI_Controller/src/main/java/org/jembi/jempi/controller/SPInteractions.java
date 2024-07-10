package org.jembi.jempi.controller;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import org.apache.kafka.common.serialization.*;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Branched;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.AppConfig;
import org.jembi.jempi.shared.models.*;
import org.jembi.jempi.shared.serdes.JsonPojoDeserializer;
import org.jembi.jempi.shared.serdes.JsonPojoSerializer;
import org.jembi.jempi.shared.utils.AppUtils;
import java.util.Properties;

public final class SPInteractions {

   private static final Logger LOGGER = LogManager.getLogger(SPInteractions.class);
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
      batchPatientRecordKStream.split()
                               .branch((key, batchPatient) -> {
                                          batchPatient = updateControllerMetadataTimeStamp(batchPatient);
                                          return batchPatient.sessionMetadata()
                                                             .commonMetaData()
                                                             .uploadConfig() != null && batchPatient.sessionMetadata()
                                                                                                    .commonMetaData()
                                                                                                    .uploadConfig()
                                                                                                    .uploadWorkflow()
                                                                                                    .equals(UploadConfig.UploadWorkflow.UPLOAD_WORKFLOW_EM);
                                             },
                                       Branched.withConsumer((ks) -> ks.to(GlobalConstants.TOPIC_INTERACTION_EM)))
                               .branch((key, batchPatient) -> {
                                          batchPatient = updateControllerMetadataTimeStamp(batchPatient);
                                          return batchPatient.sessionMetadata()
                                                             .commonMetaData()
                                                             .uploadConfig() == null || batchPatient.sessionMetadata()
                                                                                                    .commonMetaData()
                                                                                                    .uploadConfig()
                                                                                                    .uploadWorkflow()
                                                                                                    .equals(UploadConfig.UploadWorkflow.UPLOAD_WORKFLOW_LINK);
                                       },
                                       Branched.withConsumer((ks) -> ks.to(GlobalConstants.TOPIC_INTERACTION_LINKER)))
                               .noDefaultBranch();
      interactionKafkaStreams = new KafkaStreams(streamsBuilder.build(), props);
      interactionKafkaStreams.cleanUp();
      interactionKafkaStreams.start();
      Runtime.getRuntime().addShutdownHook(new Thread(interactionKafkaStreams::close));
      LOGGER.info("KafkaStreams started");
   }

   public void close() {
      interactionKafkaStreams.close();
   }

   private Properties loadConfig() {
      final Properties props = new Properties();
      props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, AppConfig.KAFKA_BOOTSTRAP_SERVERS);
      props.put(StreamsConfig.APPLICATION_ID_CONFIG, AppConfig.KAFKA_APPLICATION_ID + "-INTERACTIONS");
      props.put(StreamsConfig.POLL_MS_CONFIG, 10);
      return props;
   }

   private InteractionEnvelop updateControllerMetadataTimeStamp(final InteractionEnvelop interactionEnvelop) {
      var sessionMetadata = interactionEnvelop.sessionMetadata();
      return new InteractionEnvelop(interactionEnvelop.contentType(),
                                    interactionEnvelop.tag(),
                                    interactionEnvelop.stan(),
                                    interactionEnvelop.interaction(),
                                    new SessionMetadata(sessionMetadata.commonMetaData(),
                                                        sessionMetadata.uiMetadata(),
                                                        sessionMetadata.asyncReceiverMetadata(),
                                                        sessionMetadata.etlMetadata(),
                                                        new ControllerMetadata(AppUtils.timeStamp()),
                                                        sessionMetadata.linkerMetadata()));
   }
}
