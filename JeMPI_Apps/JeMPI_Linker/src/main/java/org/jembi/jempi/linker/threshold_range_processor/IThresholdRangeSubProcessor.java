package org.jembi.jempi.linker.threshold_range_processor;

import org.jembi.jempi.linker.threshold_range_processor.lib.CategorisedCandidates;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface IThresholdRangeSubProcessor {
    Boolean processCandidates(List<CategorisedCandidates> candidate) throws ExecutionException, InterruptedException;
}
