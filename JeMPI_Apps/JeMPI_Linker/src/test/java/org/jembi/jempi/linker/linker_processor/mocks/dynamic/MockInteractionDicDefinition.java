package org.jembi.jempi.linker.linker_processor.mocks.dynamic;

import org.jembi.jempi.linker.backend.LinkerProbabilistic;
import org.jembi.jempi.shared.models.CustomDemographicData;

import java.util.List;
import java.util.Map;

public class MockInteractionDicDefinition
{
    public record MockInteractionCandidates(Map<String, LinkerProbabilistic.FieldScoreInfo>  jaroWrinkleScores,
                                            CustomDemographicData candidateDemographic){}
    public record InteractionDic(CustomDemographicData originalInteractionDemographics,
                                     List<MockInteractionCandidates> mockCandidates){}
}
