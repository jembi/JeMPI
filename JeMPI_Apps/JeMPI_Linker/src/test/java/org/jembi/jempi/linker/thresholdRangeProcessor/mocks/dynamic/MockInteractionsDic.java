package org.jembi.jempi.linker.thresholdRangeProcessor.mocks.dynamic;

import org.jembi.jempi.linker.backend.LinkerProbabilistic;
import org.jembi.jempi.shared.models.CustomDemographicData;

import java.util.List;
import java.util.Map;

public class MockInteractionsDic {

    public static final List<MockInteractionDicDefinition.InteractionDic> interactionsDic =
            List.of(
                    new MockInteractionDicDefinition.InteractionDic(
                            new CustomDemographicData("givenName",
                                    "familyName",
                                    "gender",
                                    "dob",
                                    "city",
                                    "phoneNumber",
                                    "nationalId"),
                            List.of(
                                    new MockInteractionDicDefinition.MockInteractionCandidates(
                                            Map.ofEntries(
                                                    Map.entry("firstName", new LinkerProbabilistic.FieldScoreInfo(true, 0.9F))
                                            ),
                                            new CustomDemographicData("givenName",
                                                    "familyName",
                                                    "gender",
                                                    "dob",
                                                    "city",
                                                    "phoneNumber",
                                                    "nationalId")
                                    )
                            )
                    )
            );
}
