package org.jembi.jempi.linker.thresholdRangeProcessor;

import org.jembi.jempi.linker.backend.LinkerDWH;
import org.jembi.jempi.linker.backend.LinkerUtils;
import org.jembi.jempi.linker.thresholdRangeProcessor.lib.CategorisedCandidates;
import org.jembi.jempi.linker.thresholdRangeProcessor.lib.rangeType.RangeDetails;
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

    public BaseThresholdProcessor(final String linkerId, final Interaction originalInteraction){
        this.linkerId = linkerId;
        this.originalInteraction = originalInteraction;
        this.subProcessors = this.getSubProcessors();
    }

    protected List<CategorisedCandidates> getCategorisedCandidates(List<GoldenRecord> candidates){
        // TODO Consider not repeating
        return candidates.parallelStream()
                .unordered()
                .map(candidate -> new CategorisedCandidates(candidate,
                        LinkerUtils.calcNormalizedScore(candidate.demographicData(),
                                this.originalInteraction.demographicData())))
                .peek(categorisedCandidates -> {
                    for (RangeDetails r: this.rangeDetails){
                        if (r.isApplicable(categorisedCandidates)){
                            categorisedCandidates.AddApplicableRange(r);
                        }
                    }
                })
                .collect(Collectors.toCollection(ArrayList::new));

    }

    protected void runProcessors(List<CategorisedCandidates> categorisedCandidates) throws ExecutionException, InterruptedException {
        for (IThresholdRangeSubProcessor subProcessor: subProcessors){
            if (!subProcessor.ProcessCandidates(categorisedCandidates)){
                break;
            }
        }
    }
    public IThresholdRangeProcessor SetRanges(List<RangeDetails> rangeTypes){
        this.rangeDetails = rangeTypes;
        return this;
    }


    abstract protected List<IThresholdRangeSubProcessor> getSubProcessors();
}
