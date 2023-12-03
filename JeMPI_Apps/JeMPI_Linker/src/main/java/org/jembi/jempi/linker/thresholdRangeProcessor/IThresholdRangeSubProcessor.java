package org.jembi.jempi.linker.thresholdRangeProcessor;

import org.jembi.jempi.linker.thresholdRangeProcessor.lib.CategorisedCandidates;

import java.util.List;

public interface IThresholdRangeSubProcessor {
    Boolean ProcessCandidates(List<CategorisedCandidates> candidate);
}
