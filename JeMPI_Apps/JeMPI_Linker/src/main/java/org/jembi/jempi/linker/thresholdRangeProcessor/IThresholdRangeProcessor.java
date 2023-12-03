package org.jembi.jempi.linker.thresholdRangeProcessor;

import org.jembi.jempi.linker.thresholdRangeProcessor.lib.rangeType.RangeDetails;
import org.jembi.jempi.shared.models.GoldenRecord;

import java.util.ArrayList;
import java.util.List;

public interface IThresholdRangeProcessor {

    void ProcessCandidates(List<GoldenRecord> candidate);
    IThresholdRangeProcessor SetRanges(List<RangeDetails> rangeType);
}
