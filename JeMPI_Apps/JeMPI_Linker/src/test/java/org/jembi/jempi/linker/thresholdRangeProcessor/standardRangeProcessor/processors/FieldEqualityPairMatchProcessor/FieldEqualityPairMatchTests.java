package org.jembi.jempi.linker.thresholdRangeProcessor.standardRangeProcessor.processors.FieldEqualityPairMatchProcessor;

import org.jembi.jempi.linker.backend.LinkerProbabilistic;
import org.jembi.jempi.linker.thresholdRangeProcessor.lib.CategorisedCandidates;
import org.jembi.jempi.linker.thresholdRangeProcessor.lib.muLib.FieldEqualityPairMatchMatrix;
import org.jembi.jempi.linker.thresholdRangeProcessor.lib.muLib.MuModel;
import org.jembi.jempi.linker.thresholdRangeProcessor.utls.MockCategorisedCandidatesCreator;
import org.jembi.jempi.linker.thresholdRangeProcessor.utls.MockInteractionCreator;
import org.jembi.jempi.shared.models.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FieldEqualityPairMatchTests {

    // TODO: Compare json and warn
    @Test
    void testCanLoadFieldEqualityPairMatchProcessorProperties(){
        CustomDemographicData demographicData = MockInteractionCreator.getMockDemographicData();
        Interaction mockInteraction = MockInteractionCreator.interactionFromDemographicData("testInteraction", demographicData);
        MockFieldEqualityPairMatchProcessor processor = MockFieldEqualityPairMatchProcessor.getMockInstance("testLinker", mockInteraction);

        assertEquals(processor.getLinkerId(), "testLinker");
        // TODO: Update
    }

    @Test
    void testCanGetUpdateFieldEqualityPairMatchMatrix(){

        MockCategorisedCandidatesCreator mockCategorisedCandidatesCreator = new MockCategorisedCandidatesCreator(0);

        CategorisedCandidates aCategorisedCandidates = mockCategorisedCandidatesCreator.GetCategorisedCandidate(0.3F, -1);
        CategorisedCandidates bCategorisedCandidates = mockCategorisedCandidatesCreator.GetCategorisedCandidate(0.45F, -1);
        CategorisedCandidates cCategorisedCandidates = mockCategorisedCandidatesCreator.GetCategorisedCandidate(0.55F, -1);
        CategorisedCandidates dCategorisedCandidates = mockCategorisedCandidatesCreator.GetCategorisedCandidate(0.7F, -1);

        List<CategorisedCandidates> categorisedCandidates = List.of(
                aCategorisedCandidates,
                bCategorisedCandidates,
                cCategorisedCandidates,
                dCategorisedCandidates
        );

        MockFieldEqualityPairMatchProcessor processor = MockFieldEqualityPairMatchProcessor.getMockInstance("testLinker",  MockInteractionCreator.interactionFromDemographicData(null, null));
        List<FieldEqualityPairMatchProcessor.PairMatchUnmatchedCandidates> pmCandidates = processor.getPairMatchUnMatchedCandidates(categorisedCandidates);

        assertEquals(2, pmCandidates.size());
        assertEquals(pmCandidates.get(0).isPairMatch(), true);
        assertEquals(pmCandidates.get(0).candidates().getGoldenRecord(), dCategorisedCandidates.getGoldenRecord() );

        assertEquals(pmCandidates.get(1).isPairMatch(), false);
        assertEquals(pmCandidates.get(1).candidates().getGoldenRecord(), aCategorisedCandidates.getGoldenRecord() );
    }

    public void assertMatrixMatch(HashMap<String, FieldEqualityPairMatchMatrix> resultMatrix, Map<String, List<Integer>> compareMatrix){

        for (Map.Entry<String, List<Integer>> field: compareMatrix.entrySet()){
            assertTrue(resultMatrix.containsKey(field.getKey()));

            FieldEqualityPairMatchMatrix fieldResultMatrix = resultMatrix.get(field.getKey());
            List<Integer> expectedResults = field.getValue();

            assertEquals(expectedResults.get(0), fieldResultMatrix.getFieldEqualPairMatch());
            assertEquals(expectedResults.get(1), fieldResultMatrix.getFieldEqualPairNoMatch());
            assertEquals(expectedResults.get(2), fieldResultMatrix.getFieldNotEqualPairMatch());
            assertEquals(expectedResults.get(3), fieldResultMatrix.getFieldNotEqualPairNoMatch());
        }

    }
    @Test
    void testCanUpdatedKafkaMatrixSimple() throws ExecutionException, InterruptedException {
        int mockBaseInteraction = 0;
        MockFieldEqualityPairMatchProcessor processor = MockFieldEqualityPairMatchProcessor.getMockInstance("testLinker",  MockInteractionCreator.interactionFromIdRef(mockBaseInteraction));
        MockCategorisedCandidatesCreator mockCategorisedCandidatesCreator = new MockCategorisedCandidatesCreator(mockBaseInteraction);

        CategorisedCandidates dCategorisedCandidates = mockCategorisedCandidatesCreator.GetCategorisedCandidate(0.7F, 0);
        List<CategorisedCandidates> categorisedCandidates = List.of(
                dCategorisedCandidates
        );

        processor = spy(processor);
        doNothing().when(processor).saveToKafka();

        Boolean pmCandidates = processor.ProcessCandidates(categorisedCandidates);
        assertMatrixMatch(processor.getFieldEqualityPairMatchMatrix(),
                            Map.ofEntries(
                                    Map.entry("givenName", List.of(1, 0, 0, 0)),
                                    Map.entry("familyName", List.of(1, 0, 0, 0)),
                                    Map.entry("gender", List.of(1, 0, 0, 0)),
                                    Map.entry("dob", List.of(1, 0, 0, 0)),
                                    Map.entry("city", List.of(1, 0, 0, 0)),
                                    Map.entry("phoneNumber", List.of(1, 0, 0, 0)),
                                    Map.entry("nationalId", List.of(1, 0, 0, 0))

                            ));

    }

    @Test
    void testCanUpdatedKafkaMatrixComplex() throws ExecutionException, InterruptedException {
        int mockBaseInteraction = 0;
        MockFieldEqualityPairMatchProcessor processor = MockFieldEqualityPairMatchProcessor.getMockInstance("testLinker",  MockInteractionCreator.interactionFromIdRef(mockBaseInteraction));
        MockCategorisedCandidatesCreator mockCategorisedCandidatesCreator = new MockCategorisedCandidatesCreator(mockBaseInteraction);

        CategorisedCandidates aCategorisedCandidates = mockCategorisedCandidatesCreator.GetCategorisedCandidate(0.8F, 0);
        CategorisedCandidates bCategorisedCandidates = mockCategorisedCandidatesCreator.GetCategorisedCandidate(0.7F, 1);
        CategorisedCandidates cCategorisedCandidates = mockCategorisedCandidatesCreator.GetCategorisedCandidate(0.3F, 2);
        CategorisedCandidates dCategorisedCandidates = mockCategorisedCandidatesCreator.GetCategorisedCandidate(0.2F, 3);

        CategorisedCandidates eCategorisedCandidates = mockCategorisedCandidatesCreator.GetCategorisedCandidate(0.45F, 1);
        CategorisedCandidates fCategorisedCandidates = mockCategorisedCandidatesCreator.GetCategorisedCandidate(0.55F, 2);

        List<CategorisedCandidates> categorisedCandidates = List.of(
                aCategorisedCandidates,
                bCategorisedCandidates,
                cCategorisedCandidates,
                dCategorisedCandidates,
                eCategorisedCandidates,
                fCategorisedCandidates
        );

        processor = spy(processor);
        doNothing().when(processor).saveToKafka();

        Boolean pmCandidates = processor.ProcessCandidates(categorisedCandidates);
        assertMatrixMatch(processor.getFieldEqualityPairMatchMatrix(),
                Map.ofEntries(
                        Map.entry("givenName", List.of(1, 1, 0, 2)),
                        Map.entry("familyName", List.of(1, 2, 0, 1)),
                        Map.entry("gender", List.of(1, 2, 0, 1)),
                        Map.entry("dob", List.of(1, 1, 0, 2)),
                        Map.entry("city", List.of(0, 2, 1, 1)),
                        Map.entry("phoneNumber", List.of(1, 0, 0, 3)),
                        Map.entry("nationalId", List.of(1, 1, 0, 2))

                ));

    }

}
