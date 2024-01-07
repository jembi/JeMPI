package org.jembi.jempi.linker.threshold_range_processor.lib;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.AppConfig;
import org.jembi.jempi.libmpi.LibMPI;
import org.jembi.jempi.linker.backend.LinkerDWH;
import org.jembi.jempi.shared.kafka.KafkaTopicManager;
import org.jembi.jempi.shared.kafka.global_context.store_processor.Utilities;
import org.jembi.jempi.shared.libs.m_and_u.FieldEqualityPairMatchMatrix;
import org.jembi.jempi.shared.libs.m_and_u.MuAccesor;
import org.jembi.jempi.shared.models.CustomDemographicData;
import org.apache.kafka.clients.admin.TopicListing;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.jembi.jempi.shared.models.CustomSourceId;
import org.jembi.jempi.shared.models.CustomUniqueInteractionData;
import org.jembi.jempi.shared.models.Interaction;
import java.util.*;

import static java.lang.Thread.sleep;

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
        LOGGER.info("======> Processing started <========");
        long processingTimeStart = System.currentTimeMillis();
        try {
            CSVParser csvParser = CSVFormat.DEFAULT.withHeader().parse(new InputStreamReader(new FileInputStream(csvFile)));

            for (CSVRecord record : csvParser) {
                LOGGER.info(String.format("======> Processing record %s", record.getRecordNumber()));
                long recordUpdateTimeStart = System.currentTimeMillis();
                LinkerDWH.linkInteraction(libMPI,  interactionFromDemographicData(new CustomDemographicData(
                        record.get("givenName"),
                        record.get("familyName"),
                        record.get("gender"),
                        record.get("dob"),
                        record.get("city"),
                        record.get("phoneNumber"),
                        record.get("nationalId")
                )), null, Float.parseFloat(matchThreshold));
                long recordUpdateTimeEnd = System.currentTimeMillis();
                LOGGER.info(String.format("======> Processing complete. Duration %s seconds", (recordUpdateTimeEnd - recordUpdateTimeStart) / 1000));
                LOGGER.info(String.format("======> Process current run time %s minutes", (recordUpdateTimeEnd - processingTimeStart) / 1000 / 60));
            }

            csvParser.close();
            long processingTimeEnd = System.currentTimeMillis();
            LOGGER.info(String.format("======> Process complete time: %s minutes", (processingTimeEnd - processingTimeStart) / 1000 / 60));
            LOGGER.info("======> Processing end <========");
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

    public record MandU(Float m, Float u) { }
    public record ReportMetaData(Float matchThreshold, Float windowLowerBound, Float windowUpperBound) { }
    public record Report(ReportMetaData reportMetaData, Map<String, MandU> mAndU) { }
    public static void printSaved(final String reportFilePath, final Float matchThreshold, final Float windowLowerBound, final Float windowUpperBound) throws ExecutionException, InterruptedException {
        sleep(2000);
        HashMap<String, FieldEqualityPairMatchMatrix> resultMatrix = MuAccesor.getKafkaMUUpdater("linker",  AppConfig.KAFKA_BOOTSTRAP_SERVERS).getValue();

        ReportMetaData reportMetaData = new ReportMetaData(matchThreshold, windowLowerBound, windowUpperBound);
        Map<String, MandU> fieldMap = new HashMap<>();

        for (Map.Entry<String, FieldEqualityPairMatchMatrix> field: resultMatrix.entrySet()) {
            FieldEqualityPairMatchMatrix matrix = field.getValue();
            fieldMap.put(field.getKey(), new MandU(((float) matrix.getFieldEqualPairMatch() / (matrix.getFieldEqualPairMatch() + matrix.getFieldNotEqualPairMatch())),
                                                    ((float) matrix.getFieldEqualPairNoMatch() / (matrix.getFieldEqualPairNoMatch() + matrix.getFieldNotEqualPairNoMatch()))));

        }
        Report report = new Report(reportMetaData, fieldMap);

        // Write the Report to a JSON file using Jackson
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        try {
            objectMapper.writeValue(new File(reportFilePath), report);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(final String[] args) {
        LibMPI libMPI = getLibMPI();
        try {
            deletePreviousMatrix();
            libMPI.startTransaction();
            libMPI.dropAll(); libMPI.createSchema();
            addInterationsFromFile(libMPI, args[0], args[1]);

            Float matchThreshold =  Float.parseFloat(args[1]);
            printSaved(args[2], matchThreshold, matchThreshold - 0.1F, matchThreshold + 0.1F);

            System.exit(0);

        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            e.printStackTrace();
        }
    }
}
