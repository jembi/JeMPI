package org.jembi.jempi.linker.threshold_range_processor;

import org.jembi.jempi.linker.backend.LinkerUtils;
import org.jembi.jempi.linker.threshold_range_processor.lib.CategorisedCandidates;
import org.jembi.jempi.linker.threshold_range_processor.lib.range_type.RangeDetails;
import org.jembi.jempi.shared.models.GoldenRecord;
import org.jembi.jempi.shared.models.Interaction;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public abstract class BaseThresholdProcessor implements IThresholdRangeProcessor {

    protected String linkerId;
    protected Interaction originalInteraction;
    protected List<RangeDetails> rangeDetails = new ArrayList<>();
    protected List<IThresholdRangeSubProcessor> subProcessors = new ArrayList<>();

    protected BaseThresholdProcessor(final String linkerId, final Interaction originalInteraction){
        this.linkerId = linkerId;
        this.originalInteraction = originalInteraction;
        this.subProcessors = this.getSubProcessors();
    }

    protected List<CategorisedCandidates> getCategorisedCandidates(List<GoldenRecord> candidates){
        return candidates.parallelStream()
                .unordered()
                .map(candidate -> new CategorisedCandidates(candidate,
                        LinkerUtils.calcNormalizedScore(candidate.demographicData(),
                                this.originalInteraction.demographicData())))
                .peek(categorisedCandidates -> {
                    for (RangeDetails r: this.rangeDetails){
                        if (r.isApplicable(categorisedCandidates)){
                            categorisedCandidates.addApplicableRange(r);
                        }
                    }
                })
                .collect(Collectors.toCollection(ArrayList::new));

    }

    protected void runProcessors(List<CategorisedCandidates> categorisedCandidates) throws ExecutionException, InterruptedException {
        for (IThresholdRangeSubProcessor subProcessor: subProcessors){
            if (!subProcessor.processCandidates(categorisedCandidates)){
                break;
            }
        }
    }
    public IThresholdRangeProcessor setRanges(List<RangeDetails> rangeTypes){
        this.rangeDetails = rangeTypes;
        return this;
    }


    protected abstract List<IThresholdRangeSubProcessor> getSubProcessors();
}
