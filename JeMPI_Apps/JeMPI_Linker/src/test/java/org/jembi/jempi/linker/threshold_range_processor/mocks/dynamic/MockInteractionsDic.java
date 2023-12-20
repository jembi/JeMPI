package org.jembi.jempi.linker.threshold_range_processor.mocks.dynamic;

import org.jembi.jempi.linker.backend.LinkerProbabilistic;
import org.jembi.jempi.shared.models.CustomDemographicData;

import java.util.List;
import java.util.Map;

public class MockInteractionsDic {

    public static final List<MockInteractionDicDefinition.InteractionDic> interactionsDic =
            List.of(
                    new MockInteractionDicDefinition.InteractionDic(
                            new CustomDemographicData("Jabu",
                                    "Khutuma",
                                    "Male",
                                    "01/01/89",
                                    "Joburg",
                                    "793847286",
                                    "123"),
                            List.of(
                                    // Candidate 1
                                    new MockInteractionDicDefinition.MockInteractionCandidates(
                                            Map.ofEntries(
                                                    Map.entry("firstName", new LinkerProbabilistic.FieldScoreInfo(true, 1.0F)),
                                                    Map.entry("familyName", new LinkerProbabilistic.FieldScoreInfo(true, 1.0F)),
                                                    Map.entry("gender", new LinkerProbabilistic.FieldScoreInfo(true, 1.0F)),
                                                    Map.entry("dob", new LinkerProbabilistic.FieldScoreInfo(true, 1.0F)),
                                                    Map.entry("city", new LinkerProbabilistic.FieldScoreInfo(true, 1.0F)),
                                                    Map.entry("phoneNumber", new LinkerProbabilistic.FieldScoreInfo(true, 1.0F)),
                                                    Map.entry("nationalId", new LinkerProbabilistic.FieldScoreInfo(true, 1.0F))
                                            ),
                                            new CustomDemographicData("Jabu",
                                                    "Khutuma",
                                                    "Male",
                                                    "01/01/89",
                                                    "Durban",
                                                    "793847286",
                                                    "123")
                                    ),
                                    // Candidate 2
                                    new MockInteractionDicDefinition.MockInteractionCandidates(
                                            Map.ofEntries(
                                                    Map.entry("firstName", new LinkerProbabilistic.FieldScoreInfo(true, 0.96F)),
                                                    Map.entry("familyName", new LinkerProbabilistic.FieldScoreInfo(true, 0.93F)),
                                                    Map.entry("gender", new LinkerProbabilistic.FieldScoreInfo(true, 1.0F)),
                                                    Map.entry("dob", new LinkerProbabilistic.FieldScoreInfo(true, 0.79F)),
                                                    Map.entry("city", new LinkerProbabilistic.FieldScoreInfo(true, 1.0F)),
                                                    Map.entry("phoneNumber", new LinkerProbabilistic.FieldScoreInfo(true, 0.0F)),
                                                    Map.entry("nationalId", new LinkerProbabilistic.FieldScoreInfo(true, 1.0F))
                                            ),
                                            new CustomDemographicData("Jabu2",
                                                    "Khuma",
                                                    "Male",
                                                    "03/01/89",
                                                    "Joburg",
                                                    "83564215",
                                                    "123")
                                    ),
                                    // Candidate 3
                                    new MockInteractionDicDefinition.MockInteractionCandidates(
                                            Map.ofEntries(
                                                    Map.entry("firstName", new LinkerProbabilistic.FieldScoreInfo(true, 0.0F)),
                                                    Map.entry("familyName", new LinkerProbabilistic.FieldScoreInfo(true, 0.93F)),
                                                    Map.entry("gender", new LinkerProbabilistic.FieldScoreInfo(true, 1.0F)),
                                                    Map.entry("dob", new LinkerProbabilistic.FieldScoreInfo(true, 1.0F)),
                                                    Map.entry("city", new LinkerProbabilistic.FieldScoreInfo(true, 1.0F)),
                                                    Map.entry("phoneNumber", new LinkerProbabilistic.FieldScoreInfo(true, 0.0F)),
                                                    Map.entry("nationalId", new LinkerProbabilistic.FieldScoreInfo(true, 0.0F))
                                            ),
                                            new CustomDemographicData("Smith",
                                                    "Khuma",
                                                    "Male",
                                                    "01/01/89",
                                                    "Joburg",
                                                    "83564215",
                                                    "56493")
                                    ),
                                    // Candidate 4
                                    new MockInteractionDicDefinition.MockInteractionCandidates(
                                            Map.ofEntries(
                                                    Map.entry("firstName", new LinkerProbabilistic.FieldScoreInfo(true, 0.0F)),
                                                    Map.entry("familyName", new LinkerProbabilistic.FieldScoreInfo(true, 0.46F)),
                                                    Map.entry("gender", new LinkerProbabilistic.FieldScoreInfo(true, 0.41F)),
                                                    Map.entry("dob", new LinkerProbabilistic.FieldScoreInfo(true, 0.85F)),
                                                    Map.entry("city", new LinkerProbabilistic.FieldScoreInfo(true, 0.0F)),
                                                    Map.entry("phoneNumber", new LinkerProbabilistic.FieldScoreInfo(true, 0.0F)),
                                                    Map.entry("nationalId", new LinkerProbabilistic.FieldScoreInfo(true, 0.0F))
                                            ),
                                            new CustomDemographicData("Smith",
                                                    "John",
                                                    "Female",
                                                    "02/02/89",
                                                    "CapeTown",
                                                    "89654",
                                                    "111")
                                    )
                            )
                    )
            );
}
