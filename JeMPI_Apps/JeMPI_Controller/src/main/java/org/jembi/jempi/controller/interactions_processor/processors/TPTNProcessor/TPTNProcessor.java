package org.jembi.jempi.controller.interactions_processor.processors.TPTNProcessor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.AppConfig;
import org.jembi.jempi.controller.interactions_processor.processors.IDashboardDataProducer;
import org.jembi.jempi.controller.interactions_processor.processors.IOnNotificationResolutionProcessor;
import org.jembi.jempi.controller.interactions_processor.processors.IThresholdRangeSubProcessor;
import org.jembi.jempi.controller.interactions_processor.processors.SubProcessor;
import org.jembi.jempi.libmpi.LibMPI;
import org.jembi.jempi.controller.interactions_processor.lib.CategorisedCandidates;
import org.jembi.jempi.controller.interactions_processor.lib.range_type.RangeTypeName;
import org.jembi.jempi.shared.libs.tptn.TPTNAccessor;
import org.jembi.jempi.shared.libs.tptn.TPTNKGlobalStoreInstance;
import org.jembi.jempi.shared.libs.tptn.TPTNMatrix;
import org.jembi.jempi.shared.models.Interaction;
import org.jembi.jempi.shared.models.NotificationResolution;
import org.jembi.jempi.shared.models.NotificationResolutionProcessorData;
import org.jembi.jempi.shared.models.dashboard.TPTNFScore;
import org.jembi.jempi.shared.models.dashboard.TPTNStats;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public final class TPTNProcessor extends SubProcessor implements IThresholdRangeSubProcessor, IOnNotificationResolutionProcessor, IDashboardDataProducer<TPTNStats> {
    protected Interaction originalInteraction;
    private static final Logger LOGGER = LogManager.getLogger(TPTNProcessor.class);
    private float getFScoreType(final String type, final TPTNMatrix tptnMatrix) {
        float beta;

        if (Objects.equals(type, "recall")) {
            beta = 2.0F;
        } else if (Objects.equals(type, "recall_precision")) {
            beta = 1.0F;
        } else if (Objects.equals(type, "precision")) {
            beta = 0.5F;
        } else {
            throw new RuntimeException("Unknown f-score type");
        }

        float divider = (((1F + beta) * tptnMatrix.getTruePositive()) + (beta * tptnMatrix.getFalseNegative()) + tptnMatrix.getFalsePositive());
        if (divider == 0) {
            return -1;
        }

        return  (1F + beta) * tptnMatrix.getTruePositive() / divider;
    }
    public TPTNStats getDashboardData(final LibMPI libMPI) throws Exception {
        TPTNKGlobalStoreInstance store = getStore();
        TPTNMatrix currentMatrix = store.getValue();

        return new TPTNStats(currentMatrix, new TPTNFScore(getFScoreType("recall", currentMatrix),
                                                           getFScoreType("recall_precision", currentMatrix),
                                                           getFScoreType("precision", currentMatrix)));
    }

    @Override
    public String getDashboardDataName() {
        return "tptn";
    }

    @Override
    public boolean processOnNotificationResolution(final NotificationResolutionProcessorData notificationResolutionProcessorData, final LibMPI libMPI) throws Exception {
        LOGGER.info(String.format("Updating the tptn values based on a notification resolution. Notification Id: %s", notificationResolutionProcessorData.notificationResolution().notificationId()));

        TPTNMatrix tptnMatrix = new TPTNMatrix();
        TPTNKGlobalStoreInstance store = getStore();
        NotificationResolution resolution = notificationResolutionProcessorData.notificationResolution();

        LOGGER.info(resolution.resolutionState());
        LOGGER.info(resolution.notificationType());
        if (Objects.equals(resolution.resolutionState(), "APPROVED")) {
            if (Objects.equals(resolution.notificationType(), "ABOVE THRESHOLD")) {
                tptnMatrix.updateTruePositive(1);
            } else if (Objects.equals(resolution.notificationType(), "BELOW THRESHOLD")) {
                tptnMatrix.updateTrueNegative(1);
            }
        } else {
            if (Objects.equals(resolution.notificationType(), "ABOVE THRESHOLD")) {
                tptnMatrix.updateFalsePositive(1);
            } else if (Objects.equals(resolution.notificationType(), "BELOW THRESHOLD")) {
                tptnMatrix.updateFalseNegative(1);
            }
        }

        store.updateValue(tptnMatrix);
        return true;
    }

    private TPTNKGlobalStoreInstance getStore() throws ExecutionException, InterruptedException {
        return TPTNAccessor.getKafkaTPTNUpdater(this.linkerId, AppConfig.KAFKA_BOOTSTRAP_SERVERS);
    }
    @Override
    public Boolean processCandidates(final List<CategorisedCandidates> candidates) throws ExecutionException, InterruptedException {
        TPTNMatrix tptnMatrix = new TPTNMatrix();
        TPTNKGlobalStoreInstance store = getStore();

        List<CategorisedCandidates> candidateOutsideNotiWindow = candidates.stream()
                .filter(candidate -> !(candidate.isRangeApplicable(RangeTypeName.NOTIFICATION_RANGE_BELOW_THRESHOLD)
                        || candidate.isRangeApplicable(RangeTypeName.NOTIFICATION_RANGE_ABOVE_THRESHOLD))).toList();

        tptnMatrix.updateTruePositive(candidateOutsideNotiWindow.stream().filter(c -> c.isRangeApplicable(RangeTypeName.ABOVE_THRESHOLD)).count());
        tptnMatrix.updateTrueNegative(candidateOutsideNotiWindow.stream().filter(c -> !c.isRangeApplicable(RangeTypeName.ABOVE_THRESHOLD)).count());

        LOGGER.debug(String.format("Updating the TPTN Matrix with the following: \n %s", tptnMatrix));
        store.updateValue(tptnMatrix);
        return true;
    }

    @Override
    public IThresholdRangeSubProcessor setOriginalInteraction(final Interaction originalInteractionIn) {
        this.originalInteraction = originalInteractionIn;
        return this;
    }
}
