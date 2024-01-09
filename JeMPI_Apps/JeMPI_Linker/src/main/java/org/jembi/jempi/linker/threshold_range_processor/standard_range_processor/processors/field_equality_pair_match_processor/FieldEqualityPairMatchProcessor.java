package org.jembi.jempi.linker.threshold_range_processor.standard_range_processor.processors.field_equality_pair_match_processor;


import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.AppConfig;
import org.jembi.jempi.libmpi.LibMPI;
import org.jembi.jempi.linker.CustomLinkerMU;
import org.jembi.jempi.linker.backend.LinkerProbabilistic;
import org.jembi.jempi.linker.threshold_range_processor.IDashboardDataProducer;
import org.jembi.jempi.linker.threshold_range_processor.IOnNotificationResolutionProcessor;
import org.jembi.jempi.linker.threshold_range_processor.IThresholdRangeSubProcessor;
import org.jembi.jempi.linker.threshold_range_processor.lib.CategorisedCandidates;
import org.jembi.jempi.shared.kafka.global_context.store_processor.StoreProcessorSinkManager;
import org.jembi.jempi.shared.kafka.global_context.store_processor.Utilities;
import org.jembi.jempi.shared.libs.m_and_u.FieldEqualityPairMatchMatrix;
import org.jembi.jempi.shared.libs.m_and_u.MuModel;
import org.jembi.jempi.linker.threshold_range_processor.lib.range_type.RangeTypeName;
import org.jembi.jempi.shared.models.*;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static org.jembi.jempi.shared.kafka.global_context.store_processor.Utilities.*;

public class FieldEqualityPairMatchProcessor implements IThresholdRangeSubProcessor, IOnNotificationResolutionProcessor, IDashboardDataProducer {

    private static final Logger LOGGER = LogManager.getLogger(FieldEqualityPairMatchProcessor.class);
    protected Interaction originalInteraction;
    protected MuModel muModel;

    public record PairMatchUnmatchedCandidates(CategorisedCandidates candidates, Boolean isPairMatch) { }

    protected String linkerId;

    public FieldEqualityPairMatchProcessor(final String linkerIdIn, final Interaction originalInteractionIn) {
        this.linkerId = linkerIdIn;
        this.originalInteraction = originalInteractionIn;
        this.muModel = MuModel.withLinkedFields(this.linkerId, CustomLinkerMU.LINKER_FIELDS.keySet(), AppConfig.KAFKA_BOOTSTRAP_SERVERS);
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

    void updateFieldEqualityPairMatchMatrixField(final CustomDemographicData goldenRecord, final CustomDemographicData interaction, final boolean isPairMatch) {
        Map<String, LinkerProbabilistic.FieldScoreInfo> fieldMatchInfo = new CustomLinkerMU.FieldMatchInfo(
                goldenRecord,
                interaction).toMap();

        for (Map.Entry<String, LinkerProbabilistic.Field> field: CustomLinkerMU.LINKER_FIELDS.entrySet()) {
            LinkerProbabilistic.FieldScoreInfo fieldScoreInfo = fieldMatchInfo.get(field.getKey());
            muModel.updateFieldEqualityPairMatchMatrix(field.getKey(), fieldScoreInfo.isMatch(), isPairMatch);
        }
    }
    void updateFieldEqualityPairMatchMatrix(final List<PairMatchUnmatchedCandidates> pairMatchUnmatchedCandidates) throws ExecutionException, InterruptedException {
        LOGGER.info(String.format("FieldEqualityPairMatchProcessor: Processing %d candidates", pairMatchUnmatchedCandidates.size()));

        for (PairMatchUnmatchedCandidates pairMatchCandidate : pairMatchUnmatchedCandidates) {

            updateFieldEqualityPairMatchMatrixField(pairMatchCandidate.candidates.getGoldenRecord().demographicData(),
                                                    originalInteraction.demographicData(), pairMatchCandidate.isPairMatch);

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

    @Override
    public boolean processOnNotificationResolution(final NotificationResolutionProcessorData data, final LibMPI libMPI) {
        LOGGER.info(String.format("Updating the m and u values based on a notification resolution. Notification Id: %s", data.notificationResolution().notificationId()));
        ExpandedGoldenRecord linkedGoldenRecord = libMPI.findExpandedGoldenRecord(data.linkInfo().goldenUID());

        if (linkedGoldenRecord == null) {
            LOGGER.error(String.format("Failed to update the m and u values based on a notification resolution. Could not find new golden record %s", data.linkInfo().goldenUID()));
            return false;
        }

        Optional<InteractionWithScore> originalLinkedInteraction = linkedGoldenRecord.interactionsWithScore().stream().filter(i -> Objects.equals(i.interaction().interactionId(), data.notificationResolution().interactionId())).findFirst();

        if (originalLinkedInteraction.isPresent()) {
            updateFieldEqualityPairMatchMatrixField(linkedGoldenRecord.goldenRecord().demographicData(), originalLinkedInteraction.get().interaction().demographicData(), true);

            ArrayList<String> candidates = data.notificationResolution().currentCandidates().stream().filter(c -> !Objects.equals(c, linkedGoldenRecord.goldenRecord().goldenId())).collect(Collectors.toCollection(ArrayList::new));

            for (ExpandedGoldenRecord candidateGoldenRecord : libMPI.findExpandedGoldenRecords(candidates)) {
                updateFieldEqualityPairMatchMatrixField(candidateGoldenRecord.goldenRecord().demographicData(), originalLinkedInteraction.get().interaction().demographicData(), false);
            }

            return true;
        } else {
            return false;
        }
    }

    protected FieldEqualityPairMatchMatrix convertToFieldEqualityPairMatchMatrix(final Object value) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper.convertValue(value, FieldEqualityPairMatchMatrix.class);
    }
    @Override
    public Object getDashboardData(final LibMPI libMPI) throws ExecutionException {
        try {
            // todo: Rework maybe
            HashMap<String, FieldEqualityPairMatchMatrix.MandU> dashboardData = new HashMap<>();

            TopicStoreNames topicName = Utilities.getStoreNames(String.format("linker_mu_tally_%s", this.linkerId));
            StoreProcessorSinkManager sm = new StoreProcessorSinkManager<>(topicName.topicName(), topicName.topicSinkName(), AppConfig.KAFKA_BOOTSTRAP_SERVERS, Object.class);

            Object storedValue = sm.readSink();
            if (storedValue == null) {
                return dashboardData;
            }

            for (Map.Entry<String, Object> value: ((LinkedHashMap<String, Object>) storedValue).entrySet()) {
                dashboardData.put(value.getKey(), convertToFieldEqualityPairMatchMatrix(value.getValue()).getMandUValues());
            }

            return dashboardData;
        } catch (Exception e) {
            throw new ExecutionException(e);
        }

    }

    @Override
    public String getDashboardDataName() {
        return "m_and_u";
    }

}
