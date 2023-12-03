package org.jembi.jempi.linker.thresholdRangeProcessor.lib;

import org.jembi.jempi.linker.thresholdRangeProcessor.lib.rangeType.RangeDetails;
import org.jembi.jempi.linker.thresholdRangeProcessor.lib.rangeType.RangeTypeName;
import org.jembi.jempi.shared.models.GoldenRecord;

import java.util.ArrayList;
import java.util.List;

public class CategorisedCandidates {
    private GoldenRecord goldenRecord;
    private float score;
    private List<RangeDetails> rangeDetails = new ArrayList<>();
    private List<RangeTypeName> rangeDetailsNames = new ArrayList<>();

    public CategorisedCandidates(GoldenRecord goldenRecord, float score){
        this.goldenRecord = goldenRecord;
        this.score = score;
    }
    public CategorisedCandidates AddApplicableRange(RangeDetails range){
        rangeDetails.add(range);
        rangeDetailsNames.add(range.getRangeName());
        return this;
    }

    public Boolean IsRangeApplicable(RangeTypeName rangeName){
        return rangeDetailsNames.contains(rangeName);
    }

    public float getScore() {
        return score;
    }
    public GoldenRecord getGoldenRecord() {
        return goldenRecord;
    }
}
