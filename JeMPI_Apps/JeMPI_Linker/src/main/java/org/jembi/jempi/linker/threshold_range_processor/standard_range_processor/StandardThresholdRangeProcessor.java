package org.jembi.jempi.linker.threshold_range_processor.standard_range_processor;

import org.jembi.jempi.linker.threshold_range_processor.IThresholdRangeSubProcessor;
import org.jembi.jempi.linker.threshold_range_processor.BaseThresholdProcessor;
import org.jembi.jempi.linker.threshold_range_processor.lib.CategorisedCandidates;
import org.jembi.jempi.linker.threshold_range_processor.standard_range_processor.processors.field_equality_pair_match_processor.FieldEqualityPairMatchProcessor;
import org.jembi.jempi.shared.models.GoldenRecord;
import org.jembi.jempi.shared.models.Interaction;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class StandardThresholdRangeProcessor extends BaseThresholdProcessor {
    public StandardThresholdRangeProcessor(final String linkerId, final Interaction originalInteractionIn) {
        super(linkerId, originalInteractionIn);
    }
    @Override
    protected List<IThresholdRangeSubProcessor> getSubProcessors() {
        return List.of(new FieldEqualityPairMatchProcessor(this.linkerId, this.originalInteraction));
    }

    public void processCandidates(final List<GoldenRecord> candidates) throws ExecutionException, InterruptedException {
        List<CategorisedCandidates> categorisedCandidates = this.getCategorisedCandidates(candidates);
        this.runProcessors(categorisedCandidates);
    }

}
