package org.jembi.jempi.linker.linker_processor;

import org.jembi.jempi.linker.linker_processor.processors.IOnNewInteractionProcessor;
import org.jembi.jempi.linker.linker_processor.processors.IThresholdRangeSubProcessor;
import org.jembi.jempi.linker.linker_processor.lib.CategorisedCandidates;
import org.jembi.jempi.shared.models.GoldenRecord;
import org.jembi.jempi.shared.models.Interaction;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class StandardLinkerProcessor extends BaseLinkerProcessor {
    public StandardLinkerProcessor(final String linkerId, final Interaction originalInteractionIn) {
        super(linkerId, originalInteractionIn);
    }
    @Override
    protected List<IThresholdRangeSubProcessor> getThresholdProcessors() {
        return this.processorRegistry.getThresholdProcessors(this.originalInteraction, null);
    }

    @Override
    protected List<IOnNewInteractionProcessor> getOnNewInteractionProcessors() {
        return this.processorRegistry.getOnNewInteractionProcessors(null);
    }

    public void processCandidates(final List<GoldenRecord> candidates) throws ExecutionException, InterruptedException {
        List<CategorisedCandidates> categorisedCandidates = this.getCategorisedCandidates(candidates);
        for (IThresholdRangeSubProcessor subProcessor: thresholdProcessors) {
            subProcessor.processCandidates(categorisedCandidates);
        }
    }

}
