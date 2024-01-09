package org.jembi.jempi.linker.threshold_range_processor;

import org.jembi.jempi.linker.threshold_range_processor.standard_range_processor.processors.field_equality_pair_match_processor.FieldEqualityPairMatchProcessor;
import org.jembi.jempi.shared.models.GlobalConstants;
import org.jembi.jempi.shared.models.Interaction;

import java.util.List;

public class ProcessorsRegistry {
    protected ProcessorsRegistry() { }
    public static List<IDashboardDataProducer<?>> getDashboardDataProducerProcessors() {
        return List.of((new FieldEqualityPairMatchProcessor(GlobalConstants.DEFAULT_LINKER_M_AND_U_GLOBAL_STORE_NAME, null)));
    }

    public static List<IThresholdRangeSubProcessor> getThresholdProcessors(final Interaction originalInteractionIn) {
        return List.of((new FieldEqualityPairMatchProcessor(GlobalConstants.DEFAULT_LINKER_M_AND_U_GLOBAL_STORE_NAME, originalInteractionIn)));
    }

    public static List<IOnNotificationResolutionProcessor> getOnNotificationResolutionProcessors() {
        return List.of((new FieldEqualityPairMatchProcessor(GlobalConstants.DEFAULT_LINKER_M_AND_U_GLOBAL_STORE_NAME, null)));
    }

}
