package org.jembi.jempi.linker.linker_processor.standard_range_processor.processors.field_equality_pair_match_processor;

import org.jembi.jempi.linker.linker_processor.lib.CategorisedCandidates;
import org.jembi.jempi.linker.linker_processor.processors.field_equality_pair_match_processor.FieldEqualityPairMatchProcessor;
import org.jembi.jempi.linker.linker_processor.utls.MockInteractionCreator;
import org.jembi.jempi.shared.models.Interaction;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class MockFieldEqualityPairMatchProcessor extends FieldEqualityPairMatchProcessor {
    public MockFieldEqualityPairMatchProcessor(String linkerId, Interaction originalInteraction) {
        super(linkerId, originalInteraction);
    }

    public String getLinkerId(){
        return this.linkerId;
    }

    public List<FieldEqualityPairMatchProcessor.PairMatchUnmatchedCandidates> getPairMatchUnMatchedCandidates(List<CategorisedCandidates> candidates){
        return super.getPairMatchUnMatchedCandidates(candidates);
    }

    public void updateFieldEqualityPairMatchMatrix(List<FieldEqualityPairMatchProcessor.PairMatchUnmatchedCandidates> pairMatchUnmatchedCandidates) throws ExecutionException, InterruptedException {
        super.updateFieldEqualityPairMatchMatrix(pairMatchUnmatchedCandidates);
    }

    public static  MockFieldEqualityPairMatchProcessor getMockInstance(final String linkerId, final Interaction mockInteraction){
        return new MockFieldEqualityPairMatchProcessor(linkerId == null ?  UUID.randomUUID().toString() : linkerId,
                                                        mockInteraction == null ? MockInteractionCreator.interactionFromDemographicData(null, null): mockInteraction);
    }
}
