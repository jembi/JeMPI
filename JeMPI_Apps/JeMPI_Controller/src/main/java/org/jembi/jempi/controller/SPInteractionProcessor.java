package org.jembi.jempi.controller;

import org.apache.kafka.common.serialization.Deserializer;
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
import org.jembi.jempi.controller.interactions_processor.InteractionProcessorRunner;
import org.jembi.jempi.libmpi.LibMPI;
import org.jembi.jempi.shared.models.GlobalConstants;
import org.jembi.jempi.shared.libs.interactionProcessor.models.InteractionProcessorEnvelop;
import org.jembi.jempi.shared.serdes.JsonPojoDeserializer;
import org.jembi.jempi.shared.serdes.JsonPojoSerializer;

import java.util.Locale;
import java.util.Properties;
import java.util.UUID;

public class SPInteractionProcessor {
    private static final Logger LOGGER = LogManager.getLogger(SPInteractionProcessor.class);
    private static LibMPI getLibMPI(final boolean useDGraph) {
        LibMPI libMPI;
        if (useDGraph) {
            final var host = AppConfig.getDGraphHosts();
            final var port = AppConfig.getDGraphPorts();
            libMPI = new LibMPI(AppConfig.GET_LOG_LEVEL,
                    host,
                    port,
                    AppConfig.KAFKA_BOOTSTRAP_SERVERS,
                    "CLIENT_ID_CONTROLLER_INTERACTION_PROCESSOR-" + UUID.randomUUID());
        } else {
            libMPI = new LibMPI(String.format(Locale.ROOT, "jdbc:postgresql://%s:%d/%s", AppConfig.POSTGRESQL_IP, AppConfig.POSTGRESQL_PORT, AppConfig.POSTGRESQL_DATABASE),
                    AppConfig.POSTGRESQL_USER,
                    AppConfig.POSTGRESQL_PASSWORD,
                    AppConfig.KAFKA_BOOTSTRAP_SERVERS,
                    "CLIENT_ID_CONTROLLER_INTERACTION_PROCESSOR-" + UUID.randomUUID());
        }
        libMPI.startTransaction();
        return libMPI;
    }

    void open() {
        LOGGER.info("Interaction processor stream");
        final Properties props = new Properties();
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, AppConfig.KAFKA_BOOTSTRAP_SERVERS);
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, AppConfig.KAFKA_APPLICATION_ID + "-INTERACTION_PROCESSOR");
        props.put(StreamsConfig.POLL_MS_CONFIG, 50);

        final Serializer<InteractionProcessorEnvelop> interactionProcessorEnvelopSerializer = new JsonPojoSerializer<>();
        final Deserializer<InteractionProcessorEnvelop> interactionProcessorEnvelopDeserializer = new JsonPojoDeserializer<>(InteractionProcessorEnvelop.class);


        final StreamsBuilder streamsBuilder = new StreamsBuilder();
        final KStream<String, InteractionProcessorEnvelop> interactionProcessorStream = streamsBuilder.stream(
                GlobalConstants.TOPIC_INTERACTION_PROCESSOR_CONTROLLER,
                Consumed.with(Serdes.String(), Serdes.serdeFrom(interactionProcessorEnvelopSerializer, interactionProcessorEnvelopDeserializer)));

        LibMPI libMPI = getLibMPI(true);
        interactionProcessorStream
                .foreach((key, value) -> {
                    try {
                        InteractionProcessorRunner.run(value, libMPI);
                    } catch (Exception e) {
                        LOGGER.debug(String.format("Failed to run the interaction processor %s", value.processorToUse()), e.toString());
                    }
                });

        final var interactionProcessorBuiltStreams = new KafkaStreams(streamsBuilder.build(), props);
        interactionProcessorBuiltStreams.cleanUp();
        interactionProcessorBuiltStreams.start();
        Runtime.getRuntime().addShutdownHook(new Thread(interactionProcessorBuiltStreams::close));
        LOGGER.info("Controller interaction processor started");
    }
}
