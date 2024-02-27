package org.jembi.jempi.libmpi.utils;

import org.jembi.jempi.libmpi.AppConfig;
import org.jembi.jempi.libmpi.LibMPI;
import org.jembi.jempi.shared.models.CustomDemographicData;
import org.jembi.jempi.shared.models.CustomUniqueInteractionData;
import org.jembi.jempi.shared.models.Interaction;
import org.jembi.jempi.shared.models.*;

import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.UUID;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;



public class Utilities {

    private static LibMPI libMpi = null;
    public static LibMPI getLibMPI(){
        if (libMpi == null){
            // TODO: Load env variable  correctly. currently using intellj library
            final var host = AppConfig.getDGraphHosts();
            final var port = AppConfig.getDGraphPorts();
            libMpi = new LibMPI(AppConfig.GET_LOG_LEVEL,
                    host,
                    port,
                    AppConfig.KAFKA_BOOTSTRAP_SERVERS,
                    "CLIENT_ID_LINKER-" + UUID.randomUUID());

            libMpi.startTransaction();
        }

        return libMpi;
    }

    public static void SetRules(String ruleId) {

    }

    public static Interaction interactionFromDemographicData(final String interactionId, final CustomDemographicData demographicData){
        return new Interaction(interactionId == null ?  UUID.randomUUID().toString() : interactionId,
                new CustomSourceId(UUID.randomUUID().toString(), null, null),
                new CustomUniqueInteractionData( LocalDateTime.now(), null, null),
                demographicData);
    }

    public static void ResetData () {
        LibMPI libMPI = getLibMPI();
        libMPI.dropAll();
        libMPI.createSchema();
    }
    public static void AddData(CustomDemographicData customDemographicData){
        LibMPI libMPI = getLibMPI();
        libMPI.createInteractionAndLinkToClonedGoldenRecord(interactionFromDemographicData(null, customDemographicData), -1.0F);
    }

    public static void AddDataFromCSV(String dataId, Boolean cleanPrevious) {

        LibMPI libMPI = getLibMPI();
        if (cleanPrevious){
            libMPI.dropAll();
            libMPI.createSchema();
        }

        // Get the class loader
        ClassLoader classLoader = Utilities.class.getClassLoader();

        try {
            CSVParser csvParser = CSVFormat.DEFAULT.withHeader().parse(new InputStreamReader(classLoader.getResourceAsStream(String.format("csv/%s.csv", dataId))));

            for (CSVRecord record : csvParser) {
                CustomDemographicData data = new CustomDemographicData(
                        record.get("givenName"),
                        record.get("familyName"),
                        record.get("gender"),
                        record.get("dob"),
                        record.get("city"),
                        record.get("phoneNumber"),
                        record.get("nationalId")
                );

                libMPI.createInteractionAndLinkToClonedGoldenRecord(interactionFromDemographicData(null, data), -1.0F);

            }

            csvParser.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
