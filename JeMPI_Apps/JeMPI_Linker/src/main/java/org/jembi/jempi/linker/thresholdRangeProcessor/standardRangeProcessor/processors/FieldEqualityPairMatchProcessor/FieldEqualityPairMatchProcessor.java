package org.jembi.jempi.linker.thresholdRangeProcessor.standardRangeProcessor.processors.FieldEqualityPairMatchProcessor;

import org.jembi.jempi.linker.backend.LinkerProbabilistic;
import org.jembi.jempi.linker.thresholdRangeProcessor.IThresholdRangeSubProcessor;
import org.jembi.jempi.linker.thresholdRangeProcessor.lib.CategorisedCandidates;
import org.jembi.jempi.linker.thresholdRangeProcessor.lib.muLib.FieldEqualityPairMatchMatrix;
import org.jembi.jempi.linker.thresholdRangeProcessor.lib.muLib.MuModel;
import org.jembi.jempi.linker.thresholdRangeProcessor.lib.rangeType.RangeTypeName;
import org.jembi.jempi.shared.models.Interaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static org.jembi.jempi.linker.backend.CustomLinkerProbabilistic.currentLinkFieldsMap;

public class FieldEqualityPairMatchProcessor implements IThresholdRangeSubProcessor {

    protected Interaction originalInteraction;
    protected Map<String, String> originalInteractionDemographicDataMap;

    protected MuModel muModel;
    public record PairMatchUnmatchedCandidates(CategorisedCandidates candidates, Boolean isPairMatch) {}

    protected String linkerId;

    public FieldEqualityPairMatchProcessor(final String linkerId, final Interaction originalInteraction){
        this.linkerId = linkerId;
        this.originalInteraction = originalInteraction;
        this.originalInteractionDemographicDataMap = this.originalInteraction.demographicData().toMap();
        this.muModel = MuModel.fromDemographicData(this.linkerId, originalInteractionDemographicDataMap);
    }

    List<PairMatchUnmatchedCandidates> getPairMatchUnMatchedCandidates(List<CategorisedCandidates> candidates){
         Boolean[] firstMatch = {true};

        return candidates.stream()
                        .filter(candidate -> !(candidate.IsRangeApplicable(RangeTypeName.NOTIFICATION_RANGE_BELOW_THRESHOLD) || candidate.IsRangeApplicable(RangeTypeName.NOTIFICATION_RANGE_ABOVE_THRESHOLD)))
                        .sorted((o1, o2) -> Float.compare(o2.getScore(), o1.getScore()))
                        .map(orderCandidates-> {
                            if (orderCandidates.IsRangeApplicable(RangeTypeName.ABOVE_THRESHOLD) && firstMatch[0]){
                                firstMatch[0] = false;
                                return new PairMatchUnmatchedCandidates(orderCandidates, true);
                            }
                            else{
                                return new PairMatchUnmatchedCandidates(orderCandidates, false);
                            }
                        })
                        .collect(Collectors.toCollection(ArrayList::new));

    }

    void updateFieldEqualityPairMatchMatrix(List<PairMatchUnmatchedCandidates> pairMatchUnmatchedCandidates) throws ExecutionException, InterruptedException {
        for (Map.Entry<String, LinkerProbabilistic.Field> field: currentLinkFieldsMap.entrySet()) {

            for (PairMatchUnmatchedCandidates pairMatchCandidate : pairMatchUnmatchedCandidates) {
                var candidateDemographicData = pairMatchCandidate.candidates.getGoldenRecord().demographicData().toMap();

                var fieldScoreInfo = LinkerProbabilistic.fieldScoreInfo(originalInteractionDemographicDataMap.get(field.getKey()), candidateDemographicData.get(field.getKey()), field.getValue());

                this.muModel.updateFieldEqualityPairMatchMatrix(field.getKey(), fieldScoreInfo.isMatch(), pairMatchCandidate.isPairMatch);
            }
        }
        this.saveToKafka();
    }

    public void saveToKafka() throws ExecutionException, InterruptedException {
        this.muModel.saveToKafka();
    }

    public HashMap<String, FieldEqualityPairMatchMatrix> getFieldEqualityPairMatchMatrix(){
        return this.muModel.getFieldEqualityPairMatchMatrix();
    }

    @Override
    public Boolean ProcessCandidates(List<CategorisedCandidates> candidate) throws ExecutionException, InterruptedException {
        List<PairMatchUnmatchedCandidates> pairMatchUnmatchedCandidates = this.getPairMatchUnMatchedCandidates(candidate);
        this.updateFieldEqualityPairMatchMatrix(pairMatchUnmatchedCandidates);
        return true;
    }
}
