package org.jembi.jempi.linker.threshold_range_processor.standard_range_processor.processors.field_equality_pair_match_processor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.AppConfig;
import org.jembi.jempi.linker.backend.LinkerProbabilistic;
import org.jembi.jempi.linker.threshold_range_processor.IThresholdRangeSubProcessor;
import org.jembi.jempi.linker.threshold_range_processor.lib.CategorisedCandidates;
import org.jembi.jempi.shared.libs.m_and_u.FieldEqualityPairMatchMatrix;
import org.jembi.jempi.shared.libs.m_and_u.MuModel;
import org.jembi.jempi.linker.threshold_range_processor.lib.range_type.RangeTypeName;
import org.jembi.jempi.shared.models.Interaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static org.jembi.jempi.linker.backend.CustomLinkerProbabilistic.currentLinkFieldsMap;

public class FieldEqualityPairMatchProcessor implements IThresholdRangeSubProcessor {

    private static final Logger LOGGER = LogManager.getLogger(FieldEqualityPairMatchProcessor.class);
    protected Interaction originalInteraction;
    protected Map<String, String> originalInteractionDemographicDataMap;

    protected MuModel muModel;
    public record PairMatchUnmatchedCandidates(CategorisedCandidates candidates, Boolean isPairMatch) { }

    protected String linkerId;

    public FieldEqualityPairMatchProcessor(final String linkerIdIn, final Interaction originalInteractionIn) {
        this.linkerId = linkerIdIn;
        this.originalInteraction = originalInteractionIn;
        this.originalInteractionDemographicDataMap = this.originalInteraction.demographicData().toMap();
        this.muModel = MuModel.fromDemographicData(this.linkerId, originalInteractionDemographicDataMap, AppConfig.KAFKA_BOOTSTRAP_SERVERS);
    }

    List<PairMatchUnmatchedCandidates> getPairMatchUnMatchedCandidates(final List<CategorisedCandidates> candidates) {
         Boolean[] firstMatch = {true};

        return candidates.stream()
                        .filter(candidate -> !(candidate.isRangeApplicable(RangeTypeName.NOTIFICATION_RANGE_BELOW_THRESHOLD) || candidate.isRangeApplicable(RangeTypeName.NOTIFICATION_RANGE_ABOVE_THRESHOLD)))
                        .sorted((o1, o2) -> Float.compare(o2.getScore(), o1.getScore()))
                        .map(orderCandidates -> {
                            if (orderCandidates.isRangeApplicable(RangeTypeName.ABOVE_THRESHOLD) && firstMatch[0]) {
                                firstMatch[0] = false;
                                return new PairMatchUnmatchedCandidates(orderCandidates, true);
                            } else {
                                return new PairMatchUnmatchedCandidates(orderCandidates, false);
                            }
                        })
                        .collect(Collectors.toCollection(ArrayList::new));

    }

    void updateFieldEqualityPairMatchMatrix(final List<PairMatchUnmatchedCandidates> pairMatchUnmatchedCandidates) throws ExecutionException, InterruptedException {
        LOGGER.info(String.format("FieldEqualityPairMatchProcessor: Processing %d candidates", pairMatchUnmatchedCandidates.size()));
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
        LOGGER.debug("Saving candidates m and u values to kafka");
        LOGGER.debug(this.muModel.toString());
        this.muModel.saveToKafka();
    }

    public HashMap<String, FieldEqualityPairMatchMatrix> getFieldEqualityPairMatchMatrix() {
        return (HashMap<String, FieldEqualityPairMatchMatrix>) this.muModel.getFieldEqualityPairMatchMatrix();
    }

    @Override
    public Boolean processCandidates(final List<CategorisedCandidates> candidate) throws ExecutionException, InterruptedException {
        List<PairMatchUnmatchedCandidates> pairMatchUnmatchedCandidates = this.getPairMatchUnMatchedCandidates(candidate);
        this.updateFieldEqualityPairMatchMatrix(pairMatchUnmatchedCandidates);
        return true;
    }
}
