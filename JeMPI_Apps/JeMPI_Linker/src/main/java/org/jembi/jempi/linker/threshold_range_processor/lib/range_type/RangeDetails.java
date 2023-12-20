package org.jembi.jempi.linker.threshold_range_processor.lib.range_type;

import org.jembi.jempi.linker.threshold_range_processor.lib.CategorisedCandidates;

public class RangeDetails {

    private float lowerValue;
    private float upperValue;
    private RangeTypeName rangeName;

    public RangeDetails(float lowerValue, float upperValue, RangeTypeName rangeName){
        this.lowerValue = lowerValue;
        this.upperValue = upperValue;
        this.rangeName = rangeName;
    }
    public Boolean isApplicable(CategorisedCandidates candidate){
        return lowerValue <= candidate.getScore() && candidate.getScore() < upperValue;
    }

    public float getLowerValue() {
        return lowerValue;
    }

    public RangeTypeName getRangeName() {
        return rangeName;
    }

    public float getUpperValue() {
        return upperValue;
    }
}
