package org.jembi.jempi.linker.thresholdRangeProcessor;

import org.jembi.jempi.linker.thresholdRangeProcessor.lib.rangeType.RangeDetails;
import org.jembi.jempi.shared.models.GoldenRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public interface IThresholdRangeProcessor {

    void ProcessCandidates(List<GoldenRecord> candidate) throws ExecutionException, InterruptedException;
    IThresholdRangeProcessor SetRanges(List<RangeDetails> rangeType);
}
