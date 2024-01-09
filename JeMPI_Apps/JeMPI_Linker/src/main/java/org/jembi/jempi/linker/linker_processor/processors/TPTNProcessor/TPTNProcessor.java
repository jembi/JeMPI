package org.jembi.jempi.linker.linker_processor.processors.TPTNProcessor;

import org.jembi.jempi.libmpi.LibMPI;
import org.jembi.jempi.linker.linker_processor.lib.CategorisedCandidates;
import org.jembi.jempi.linker.linker_processor.processors.IDashboardDataProducer;
import org.jembi.jempi.linker.linker_processor.processors.IOnNotificationResolutionProcessor;
import org.jembi.jempi.linker.linker_processor.processors.IThresholdRangeSubProcessor;
import org.jembi.jempi.linker.linker_processor.processors.SubProcessor;
import org.jembi.jempi.shared.models.Interaction;
import org.jembi.jempi.shared.models.NotificationResolutionProcessorData;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class TPTNProcessor extends SubProcessor implements IThresholdRangeSubProcessor, IOnNotificationResolutionProcessor, IDashboardDataProducer {
    @Override
    public Object getDashboardData(LibMPI libMPI) throws ExecutionException {
        return null;
    }

    @Override
    public String getDashboardDataName() {
        return null;
    }

    @Override
    public boolean processOnNotificationResolution(NotificationResolutionProcessorData notificationResolutionProcessorData, LibMPI libMPI) {
        return false;
    }

    @Override
    public Boolean processCandidates(List<CategorisedCandidates> candidate) throws ExecutionException, InterruptedException {
        return null;
    }

    @Override
    public IThresholdRangeSubProcessor setOriginalInteraction(Interaction originalInteractionIn) {
        return null;
    }
}
