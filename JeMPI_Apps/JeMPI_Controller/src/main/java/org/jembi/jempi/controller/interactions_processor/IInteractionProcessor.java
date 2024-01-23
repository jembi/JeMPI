package org.jembi.jempi.controller.interactions_processor;

import org.jembi.jempi.controller.interactions_processor.lib.range_type.RangeDetails;
import java.util.List;


public interface IInteractionProcessor {
    IInteractionProcessor setRanges(List<RangeDetails> rangeType);
}


