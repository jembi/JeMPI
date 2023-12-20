package org.jembi.jempi.linker.threshold_range_processor.lib;

import org.jembi.jempi.linker.threshold_range_processor.lib.range_type.RangeDetails;
import org.jembi.jempi.linker.threshold_range_processor.lib.range_type.RangeTypeName;
import org.jembi.jempi.shared.models.GoldenRecord;

import java.util.ArrayList;
import java.util.List;

public class CategorisedCandidates {
    private final GoldenRecord goldenRecord;
    private final float score;
    private final List<RangeDetails> rangeDetails = new ArrayList<>();
    private final List<RangeTypeName> rangeDetailsNames = new ArrayList<>();

    public CategorisedCandidates(GoldenRecord goldenRecord, float score){
        this.goldenRecord = goldenRecord;
        this.score = score;
    }
    public CategorisedCandidates addApplicableRange(RangeDetails range){
        rangeDetails.add(range);
        rangeDetailsNames.add(range.getRangeName());
        return this;
    }

    public Boolean isRangeApplicable(RangeTypeName rangeName){
        return rangeDetailsNames.contains(rangeName);
    }

    public float getScore() {
        return score;
    }
    public GoldenRecord getGoldenRecord() {
        return goldenRecord;
    }
}
