package org.jembi.jempi.linker.linker_processor.processors.linker_stats;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.libmpi.LibMPI;
import org.jembi.jempi.linker.linker_processor.processors.IDashboardDataProducer;
import org.jembi.jempi.linker.linker_processor.processors.IOnNewInteractionProcessor;
import org.jembi.jempi.linker.linker_processor.processors.SubProcessor;
import org.jembi.jempi.shared.models.Interaction;

import java.util.concurrent.ExecutionException;

public class LinkerStatsProcessor extends SubProcessor implements IOnNewInteractionProcessor, IDashboardDataProducer {
    private static final Logger LOGGER = LogManager.getLogger(LinkerStatsProcessor.class);

    @Override
    public Object getDashboardData(final LibMPI libMPI) throws ExecutionException {
        return null;
    }

    @Override
    public String getDashboardDataName() {
        return "linker_stats";
    }

    @Override
    public void onNewInteraction(final Interaction interaction) {

    }
}
