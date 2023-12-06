package org.jembi.jempi.linker.thresholdRangeProcessor.standardRangeProcessor;

import org.jembi.jempi.linker.thresholdRangeProcessor.IThresholdRangeSubProcessor;
import org.jembi.jempi.linker.thresholdRangeProcessor.BaseThresholdProcessor;
import org.jembi.jempi.linker.thresholdRangeProcessor.lib.CategorisedCandidates;
import org.jembi.jempi.linker.thresholdRangeProcessor.standardRangeProcessor.processors.FieldEqualityPairMatchProcessor.FieldEqualityPairMatchProcessor;
import org.jembi.jempi.shared.models.GoldenRecord;
import org.jembi.jempi.shared.models.Interaction;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class StandardThresholdRangeProcessor extends BaseThresholdProcessor {
    public StandardThresholdRangeProcessor(final String linkerId, Interaction originalInteraction) {
        super(linkerId, originalInteraction);
    }
    @Override
    protected List<IThresholdRangeSubProcessor> getSubProcessors() {
        return List.of(new FieldEqualityPairMatchProcessor(this.linkerId, this.originalInteraction));
    }

    public void ProcessCandidates(List<GoldenRecord> candidates) throws ExecutionException, InterruptedException {
        List<CategorisedCandidates> categorisedCandidates = this.getCategorisedCandidates(candidates);
        this.runProcessors(categorisedCandidates);
    }

}
