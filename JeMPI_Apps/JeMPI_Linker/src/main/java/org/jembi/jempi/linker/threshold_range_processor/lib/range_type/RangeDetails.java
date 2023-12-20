package org.jembi.jempi.linker.threshold_range_processor.lib.range_type;

import org.jembi.jempi.linker.threshold_range_processor.lib.CategorisedCandidates;

public final class RangeDetails {

    private final float lowerValue;
    private final float upperValue;
    private final RangeTypeName rangeName;

    public RangeDetails(final float lowerValueIn, final float upperValueIn, final RangeTypeName rangeNameIn) {
        this.lowerValue = lowerValueIn;
        this.upperValue = upperValueIn;
        this.rangeName = rangeNameIn;
    }
    public Boolean isApplicable(final CategorisedCandidates candidate) {
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
