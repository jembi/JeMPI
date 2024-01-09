package org.jembi.jempi.linker.linker_processor.standard_range_processor.processors.field_equality_pair_match_processor;

import org.jembi.jempi.AppConfig;
import org.jembi.jempi.libmpi.LibMPI;
import org.jembi.jempi.libmpi.LibMPIClientInterface;
import org.jembi.jempi.linker.backend.LinkerDWH;
import org.jembi.jempi.linker.backend.LinkerUtils;
import org.jembi.jempi.linker.backend.CustomLinkerDeterministic;
import org.jembi.jempi.linker.backend.CustomLinkerProbabilistic;
import org.jembi.jempi.shared.libs.m_and_u.FieldEqualityPairMatchMatrix;
import org.jembi.jempi.shared.libs.m_and_u.MuAccesor;
import org.jembi.jempi.linker.linker_processor.lib.range_type.RangeTypeFactory;
import org.jembi.jempi.linker.linker_processor.StandardLinkerProcessor;
import org.jembi.jempi.linker.linker_processor.utls.MockInteractionCreator;
import org.jembi.jempi.linker.linker_processor.utls.MockLibMPI;
import org.jembi.jempi.shared.models.CustomDemographicData;
import org.jembi.jempi.shared.models.Interaction;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static java.lang.Thread.sleep;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class VerticalFieldEqualityPairMatchTests {
    LibMPI libMPI = null;
    @BeforeAll
    void setLibMPI(){
        libMPI = MockLibMPI.getLibMPI();
    }


    public void printSaved() throws ExecutionException, InterruptedException {
        sleep(2000);
        HashMap<String, FieldEqualityPairMatchMatrix> resultMatrix = MuAccesor.getKafkaMUUpdater("linker_new",  AppConfig.KAFKA_BOOTSTRAP_SERVERS).getValue();

        StringBuilder matrixData = new StringBuilder("----- Current Values ----");

        for (Map.Entry<String, FieldEqualityPairMatchMatrix> field: resultMatrix.entrySet()){
            matrixData.append(String.format("\n-> %s", field.getKey()));
            FieldEqualityPairMatchMatrix matrix = field.getValue();
            matrixData.append(matrix.toString());
            matrixData.append(String.format("M: %f\n", (float) matrix.getFieldEqualPairMatch() / (matrix.getFieldEqualPairMatch() + matrix.getFieldNotEqualPairMatch())));
            matrixData.append(String.format("U: %f\n", (float) matrix.getFieldEqualPairNoMatch() / (matrix.getFieldEqualPairNoMatch() + matrix.getFieldNotEqualPairNoMatch())));
            matrixData.append("\n\n");
            matrixData.append("-----------------------------------------");
        }

        System.out.println(matrixData);
    }

    // Extract of org.jembi.jempi.linker.backend.LinkerDWH.linkInteraction
    public void mockLinkInteraction(final Interaction interaction) throws ExecutionException, InterruptedException {
        StandardLinkerProcessor thresholdProcessor =
                (StandardLinkerProcessor) new StandardLinkerProcessor("linker_new", interaction).setRanges(List.of(RangeTypeFactory.standardThresholdAboveThreshold(AppConfig.LINKER_MATCH_THRESHOLD, 1.0F)));

        libMPI.startTransaction();
        final var candidateGoldenRecords = libMPI.findLinkCandidates(interaction.demographicData());

        if (candidateGoldenRecords.isEmpty()) {
             libMPI.createInteractionAndLinkToClonedGoldenRecord(interaction, 1.0F);
        } else {
            thresholdProcessor.processCandidates(candidateGoldenRecords);

            final var allCandidateScores =
                    candidateGoldenRecords.parallelStream()
                            .unordered()
                            .map(candidate -> new LinkerDWH.WorkCandidate(candidate,
                                    LinkerUtils.calcNormalizedScore(candidate.demographicData(),
                                            interaction.demographicData())))
                            .sorted((o1, o2) -> Float.compare(o2.score(), o1.score()))
                            .collect(Collectors.toCollection(ArrayList::new));

            final var candidatesAboveMatchThreshold =
                    allCandidateScores
                            .stream()
                            .filter(v -> v.score() >= AppConfig.LINKER_MATCH_THRESHOLD)
                            .collect(Collectors.toCollection(ArrayList::new));


            if (candidatesAboveMatchThreshold.isEmpty()) {
                libMPI.createInteractionAndLinkToClonedGoldenRecord(interaction, 1.0F);
            }
            else{
                final var firstCandidate = candidatesAboveMatchThreshold.get(0);
                final var linkToGoldenId =
                        new LibMPIClientInterface.GoldenIdScore(firstCandidate.goldenRecord().goldenId(), firstCandidate.score());
                final var validated1 =
                        CustomLinkerDeterministic.validateDeterministicMatch(firstCandidate.goldenRecord().demographicData(),
                                interaction.demographicData());
                final var validated2 =
                        CustomLinkerProbabilistic.validateProbabilisticScore(firstCandidate.goldenRecord().demographicData(),
                                interaction.demographicData());

                        libMPI.createInteractionAndLinkToExistingGoldenRecord(interaction,
                                linkToGoldenId,
                                validated1,
                                validated2);
            }
        }
    }

    public void useCSVFile(String csvFile) throws ExecutionException, InterruptedException {
            try {
                InputStream inputStream = VerticalFieldEqualityPairMatchTests.class.getResourceAsStream(csvFile);
                CSVParser csvParser = CSVFormat.DEFAULT.withHeader().parse(new InputStreamReader(inputStream));

                for (CSVRecord record : csvParser) {
                    this.mockLinkInteraction(MockInteractionCreator.interactionFromDemographicData(null,
                            new CustomDemographicData(
                                    record.get("givenName"),
                                    record.get("familyName"),
                                    record.get("gender"),
                                    record.get("dob"),
                                    record.get("city"),
                                    record.get("phoneNumber"),
                                    record.get("nationalId")
                            )
                    ));
                }

                csvParser.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    @Test
    void testGetMandUFor10Records() {
        try{
            libMPI.startTransaction();
            libMPI.dropAll(); libMPI.createSchema();
            useCSVFile("/csv/basic.csv");
            printSaved();
        } catch (Exception e){
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

    }
}
