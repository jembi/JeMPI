package org.jembi.jempi.linker.threshold_range_processor;

import org.jembi.jempi.linker.threshold_range_processor.lib.range_type.RangeDetails;
import org.jembi.jempi.shared.models.GoldenRecord;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface IThresholdRangeProcessor {

    void processCandidates(List<GoldenRecord> candidate) throws ExecutionException, InterruptedException;
    IThresholdRangeProcessor setRanges(List<RangeDetails> rangeType);
}
