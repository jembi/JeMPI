package org.jembi.jempi.linker.threshold_range_processor.utls;

import org.jembi.jempi.linker.threshold_range_processor.lib.CategorisedCandidates;
import org.jembi.jempi.linker.threshold_range_processor.lib.range_type.RangeDetails;
import org.jembi.jempi.linker.threshold_range_processor.lib.range_type.RangeTypeFactory;
import org.jembi.jempi.linker.threshold_range_processor.mocks.dynamic.MockInteractionDicDefinition;
import org.jembi.jempi.linker.threshold_range_processor.mocks.dynamic.MockInteractionsDic;
import org.jembi.jempi.shared.models.CustomDemographicData;

import java.util.Random;

public class MockCategorisedCandidatesCreator {

    RangeDetails notificationRangeBelow = RangeTypeFactory.standardThresholdNotificationRangeBelow(0.4F, 0.5F);
    RangeDetails notificationRangeAbove= RangeTypeFactory.standardThresholdNotificationRangeBelow(0.5F, 0.6F);
    RangeDetails aboveThreshold= RangeTypeFactory.standardThresholdAboveThreshold(0.5F, 1.0F);

    MockInteractionDicDefinition.InteractionDic mockInteractionDic;
    public MockCategorisedCandidatesCreator(int mockInteractionIndex){
        mockInteractionDic = MockInteractionsDic.interactionsDic.get(mockInteractionIndex);
    }
    public CategorisedCandidates GetCategorisedCandidate(float score, int mockCandidateToUse){
        // BT - Below Threshold
        // AT - Above Threshold
        // BNW - Notification Window - Below Threshold
        // ANW - Notification Window - Above Threshold

        // ----------BT-----------|-----------AT----------
        // ---------------|--BNW--|--ANW--|---------------
        // 0--------------0.4----0.5-----0.6--------------1

        if (mockCandidateToUse == -1){
            mockCandidateToUse = new Random().ints(0, mockInteractionDic.mockCandidates().size()).findFirst().getAsInt();
        }

        CustomDemographicData mockCandidate = mockInteractionDic.mockCandidates().get(mockCandidateToUse).candidateDemographic();


        if (score > 0.6F){
            return new CategorisedCandidates(MockInteractionCreator.goldenRecordFromDemographicData(String.valueOf(String.format("ID_%s", mockCandidateToUse)), mockCandidate), score)
                        .addApplicableRange(aboveThreshold);
        } else if (score >= 0.5F && score < 0.6F) {
            return new CategorisedCandidates(MockInteractionCreator.goldenRecordFromDemographicData(String.valueOf(String.format("ID_%s", mockCandidateToUse)), mockCandidate), score)
                    .addApplicableRange(notificationRangeAbove)
                    .addApplicableRange(aboveThreshold);
        } else if (score >= 0.4F && score < 0.5F) {
            return new CategorisedCandidates(MockInteractionCreator.goldenRecordFromDemographicData(String.valueOf(String.format("ID_%s", mockCandidateToUse)), mockCandidate), score)
                    .addApplicableRange(notificationRangeBelow);
        } else {
            return new CategorisedCandidates(MockInteractionCreator.goldenRecordFromDemographicData(String.valueOf(String.format("ID_%s", mockCandidateToUse)), mockCandidate), score);
        }
    }
}
