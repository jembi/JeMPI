package org.jembi.jempi.controller.interactions_processor;

import org.jembi.jempi.controller.interactions_processor.processors.ProcessorsRegistry;
import org.jembi.jempi.libmpi.LibMPI;
import org.jembi.jempi.controller.interactions_processor.lib.CategorisedCandidates;
import org.jembi.jempi.controller.interactions_processor.lib.range_type.RangeDetails;
import org.jembi.jempi.controller.interactions_processor.processors.IOnNewInteractionProcessor;
import org.jembi.jempi.controller.interactions_processor.processors.IThresholdRangeSubProcessor;
import org.jembi.jempi.shared.libs.linker.LinkerUtils;
import org.jembi.jempi.shared.models.GoldenRecord;
import org.jembi.jempi.shared.models.Interaction;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class BaseInteractionProcessor implements IInteractionProcessor {

    protected String linkerId;
    protected Interaction originalInteraction;
    protected List<RangeDetails> rangeDetails = new ArrayList<>();
    protected List<IThresholdRangeSubProcessor> thresholdProcessors = new ArrayList<>();
    protected List<IOnNewInteractionProcessor> onNewInteractionProcessors = new ArrayList<>();
    protected ProcessorsRegistry processorRegistry;
    protected  LibMPI libMPI;

    protected BaseInteractionProcessor(final String linkerIdIn, final Interaction originalInteractionIn, final LibMPI libMPIIn) {
        this.linkerId = linkerIdIn;
        this.originalInteraction = originalInteractionIn;
        this.processorRegistry = new ProcessorsRegistry();
        this.thresholdProcessors = this.getThresholdProcessors();
        this.onNewInteractionProcessors = this.getOnNewInteractionProcessors();
        this.libMPI = libMPIIn;
    }

    protected List<CategorisedCandidates> getCategorisedCandidates(final List<GoldenRecord> candidates) {
        return candidates.parallelStream()
                .unordered()
                .map(candidate -> new CategorisedCandidates(candidate,
                        LinkerUtils.calcNormalizedScore(candidate.demographicData(),
                                this.originalInteraction.demographicData())))
                .peek(categorisedCandidates -> {
                    for (RangeDetails r: this.rangeDetails) {
                        if (r.isApplicable(categorisedCandidates)) {
                            categorisedCandidates.addApplicableRange(r);
                        }
                    }
                })
                .collect(Collectors.toCollection(ArrayList::new));

    }

    public IInteractionProcessor setRanges(final List<RangeDetails> rangeTypes) {
        this.rangeDetails = rangeTypes;
        return this;
    }

    public void onNewInteraction(final Interaction interaction, final String envelopeStan) {
        for (IOnNewInteractionProcessor subProcessor: onNewInteractionProcessors) {
            try {
                subProcessor.onNewInteraction(interaction, envelopeStan);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    protected abstract List<IThresholdRangeSubProcessor> getThresholdProcessors();
    protected abstract List<IOnNewInteractionProcessor> getOnNewInteractionProcessors();

}
