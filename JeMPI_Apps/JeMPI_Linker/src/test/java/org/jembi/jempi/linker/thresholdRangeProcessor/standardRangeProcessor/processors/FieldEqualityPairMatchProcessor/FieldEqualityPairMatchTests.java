package org.jembi.jempi.linker.thresholdRangeProcessor.standardRangeProcessor.processors.FieldEqualityPairMatchProcessor;

import org.jembi.jempi.linker.thresholdRangeProcessor.lib.CategorisedCandidates;
import org.jembi.jempi.linker.thresholdRangeProcessor.utls.MockCategorisedCandidatesCreator;
import org.jembi.jempi.linker.thresholdRangeProcessor.utls.MockInteractionCreator;
import org.jembi.jempi.shared.models.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    void testCanUpdatedKafkaMatrix(){

    }

}
