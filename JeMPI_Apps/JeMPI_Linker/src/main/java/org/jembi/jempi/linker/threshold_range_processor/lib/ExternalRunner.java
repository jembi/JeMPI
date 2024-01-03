package org.jembi.jempi.linker.threshold_range_processor.lib;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.AppConfig;
import org.jembi.jempi.libmpi.LibMPI;
import org.jembi.jempi.linker.backend.LinkerDWH;
import org.jembi.jempi.shared.kafka.KafkaTopicManager;
import org.jembi.jempi.shared.kafka.global_context.store_processor.Utilities;
import org.jembi.jempi.shared.models.CustomDemographicData;
import org.apache.kafka.clients.admin.TopicListing;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.jembi.jempi.shared.models.CustomSourceId;
import org.jembi.jempi.shared.models.CustomUniqueInteractionData;
import org.jembi.jempi.shared.models.Interaction;
import java.util.*;
public final class ExternalRunner {

    private static final Logger LOGGER = LogManager.getLogger(ExternalRunner.class);
    private ExternalRunner() { }

    public static LibMPI getLibMPI() {
        final var host = AppConfig.getDGraphHosts();
        final var port = AppConfig.getDGraphPorts();
        return new LibMPI(AppConfig.GET_LOG_LEVEL,
                host,
                port,
                AppConfig.KAFKA_BOOTSTRAP_SERVERS,
                "CLIENT_ID_LINKER-" + UUID.randomUUID());
    }
    private static Interaction interactionFromDemographicData(final CustomDemographicData demographicData) {
        return new Interaction(UUID.randomUUID().toString(),
                new CustomSourceId(UUID.randomUUID().toString(), null, null),
                new CustomUniqueInteractionData(LocalDateTime.now(), null, null),
                 demographicData);
    }
    private  static void addInterationsFromFile(final LibMPI libMPI, final String csvFile, final String matchThreshold) throws ExecutionException, InterruptedException {
        try {
            CSVParser csvParser = CSVFormat.DEFAULT.withHeader().parse(new InputStreamReader(new FileInputStream(csvFile)));

            for (CSVRecord record : csvParser) {
                LinkerDWH.linkInteraction(libMPI,  interactionFromDemographicData(new CustomDemographicData(
                        record.get("givenName"),
                        record.get("familyName"),
                        record.get("gender"),
                        record.get("dob"),
                        record.get("city"),
                        record.get("phoneNumber"),
                        record.get("nationalId")
                )), null, Float.parseFloat(matchThreshold));

            }

            csvParser.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void deletePreviousMatrix() throws ExecutionException, InterruptedException {
        KafkaTopicManager kafkaTopicManager = new KafkaTopicManager(AppConfig.KAFKA_BOOTSTRAP_SERVERS);

        Collection<String> collection = kafkaTopicManager.getAllTopics().stream()
                .map(TopicListing::name)
                .filter(name -> name.startsWith(Utilities.JEMPI_GLOBAL_STORE_PREFIX))
                .collect(Collectors.toCollection(ArrayList::new));


        for (String topic: collection) {
            kafkaTopicManager.deleteTopic(topic);
        }


        kafkaTopicManager.checkTopicsWithWait(topics -> topics.stream().filter(t -> t.name().startsWith(Utilities.JEMPI_GLOBAL_STORE_PREFIX)).count() == 0, 5000);
    }

    public static void main(final String[] args) {
        LibMPI libMPI = getLibMPI();
        try {
            deletePreviousMatrix();
            libMPI.startTransaction();
            libMPI.dropAll(); libMPI.createSchema();
            addInterationsFromFile(libMPI, args[0], args[1]);
            System.exit(0);

        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            e.printStackTrace();
        }
    }
}
