package org.jembi.jempi.linker.linker_processor.processors.linker_stats;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.AppConfig;
import org.jembi.jempi.libmpi.LibMPI;
import org.jembi.jempi.linker.linker_processor.processors.IDashboardDataProducer;
import org.jembi.jempi.linker.linker_processor.processors.IOnNewInteractionProcessor;
import org.jembi.jempi.linker.linker_processor.processors.SubProcessor;
import org.jembi.jempi.shared.libs.linkerProgress.LinkerProgressAccessor;
import org.jembi.jempi.shared.libs.linkerProgress.LinkerProgressData;
import org.jembi.jempi.shared.libs.linkerProgress.LinkerProgressKGlobalStoreInstance;
import org.jembi.jempi.shared.models.Interaction;
import org.jembi.jempi.shared.models.dashboard.LinkerProgressStats;
import org.jembi.jempi.shared.models.dashboard.LinkerStats;

import java.util.concurrent.ExecutionException;


public final class LinkerStatsProcessor extends SubProcessor implements IOnNewInteractionProcessor, IDashboardDataProducer<LinkerStats> {
    private static final Logger LOGGER = LogManager.getLogger(LinkerStatsProcessor.class);

    private LinkerProgressKGlobalStoreInstance getStore() throws ExecutionException, InterruptedException {
        return LinkerProgressAccessor.getKafkaLinkerProgressUpdater(this.linkerId, AppConfig.KAFKA_BOOTSTRAP_SERVERS);
    }
    @Override
    public LinkerStats getDashboardData(final LibMPI libMPI) throws ExecutionException, InterruptedException {
        LinkerProgressData linkerProgressData = getStore().getValue();

        return new LinkerStats(libMPI.countGoldenRecords(), libMPI.countInteractions(),
                new LinkerProgressStats(linkerProgressData.interactionCount(), linkerProgressData.interactionSize(), linkerProgressData.fileSize(), linkerProgressData.fileName()));
    }

    @Override
    public String getDashboardDataName() {
        return "linker_stats";
    }

    @Override
    public void onNewInteraction(final Interaction interaction, final String envelopeStan) throws Exception {

        try {
            String[] stanDetails = envelopeStan.split(":");
            String[] sizes = stanDetails[2].split("_");
            getStore().updateValue(new LinkerProgressData(1, Long.parseLong(sizes[0]), Long.parseLong(sizes[1]), stanDetails[3]));
        } catch (ExecutionException | InterruptedException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.warn(String.format("The stan for the interaction %s. Seem to be in the wrong format. Will not process", interaction.interactionId()));
        }

    }
}
