package org.jembi.jempi.linker.thresholdRangeProcessor.standardRangeProcessor;

import org.jembi.jempi.linker.thresholdRangeProcessor.IThresholdRangeSubProcessor;
import org.jembi.jempi.linker.thresholdRangeProcessor.BaseThresholdProcessor;
import org.jembi.jempi.linker.thresholdRangeProcessor.lib.CategorisedCandidates;
import org.jembi.jempi.shared.models.GoldenRecord;
import org.jembi.jempi.shared.models.Interaction;
import java.util.List;

public class StandardThresholdRangeProcessor extends BaseThresholdProcessor {
    public StandardThresholdRangeProcessor(Interaction originalInteraction) {
        super(originalInteraction);
    }
    @Override
    protected List<IThresholdRangeSubProcessor> getSubProcessors() {
        return null;
    }

    public void ProcessCandidates(List<GoldenRecord> candidates){
        List<CategorisedCandidates> categorisedCandidates = this.getCategorisedCandidates(candidates);
        this.runProcessors(categorisedCandidates);
    }

}
