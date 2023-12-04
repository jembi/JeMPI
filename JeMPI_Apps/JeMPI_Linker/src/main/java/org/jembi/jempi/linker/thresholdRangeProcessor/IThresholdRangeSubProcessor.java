package org.jembi.jempi.linker.thresholdRangeProcessor;

import org.jembi.jempi.linker.thresholdRangeProcessor.lib.CategorisedCandidates;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface IThresholdRangeSubProcessor {
    Boolean ProcessCandidates(List<CategorisedCandidates> candidate) throws ExecutionException, InterruptedException;
}
