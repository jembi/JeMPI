package org.jembi.jempi.controller.interactions_processor.processors;

import org.jembi.jempi.controller.interactions_processor.lib.CategorisedCandidates;
import org.jembi.jempi.shared.models.Interaction;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface IThresholdRangeSubProcessor extends ISubProcessor {
    Boolean processCandidates(List<CategorisedCandidates> candidate) throws ExecutionException, InterruptedException;
    IThresholdRangeSubProcessor setOriginalInteraction(Interaction originalInteractionIn);

}
