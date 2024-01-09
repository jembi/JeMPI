package org.jembi.jempi.linker.linker_processor;

import org.jembi.jempi.linker.linker_processor.lib.range_type.RangeDetails;
import org.jembi.jempi.shared.models.GoldenRecord;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface ILinkerProcessor {
    void processCandidates(List<GoldenRecord> candidate) throws ExecutionException, InterruptedException;
    ILinkerProcessor setRanges(List<RangeDetails> rangeType);
}


