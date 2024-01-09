package org.jembi.jempi.linker.linker_processor.lib;

import org.jembi.jempi.linker.linker_processor.lib.range_type.RangeDetails;
import org.jembi.jempi.linker.linker_processor.lib.range_type.RangeTypeName;
import org.jembi.jempi.shared.models.GoldenRecord;

import java.util.ArrayList;
import java.util.List;

public final class CategorisedCandidates {
    private final GoldenRecord goldenRecord;
    private final float score;
    private final List<RangeDetails> rangeDetails = new ArrayList<>();
    private final List<RangeTypeName> rangeDetailsNames = new ArrayList<>();

    public CategorisedCandidates(final GoldenRecord goldenRecordIn, final float scoreIn) {
        this.goldenRecord = goldenRecordIn;
        this.score = scoreIn;
    }
    public CategorisedCandidates addApplicableRange(final RangeDetails range) {
        rangeDetails.add(range);
        rangeDetailsNames.add(range.getRangeName());
        return this;
    }

    public Boolean isRangeApplicable(final RangeTypeName rangeName) {
        return rangeDetailsNames.contains(rangeName);
    }

    public float getScore() {
        return score;
    }
    public GoldenRecord getGoldenRecord() {
        return goldenRecord;
    }
}
