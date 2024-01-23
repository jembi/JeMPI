package org.jembi.jempi.controller.interactions_processor;

import org.jembi.jempi.controller.interactions_processor.lib.range_type.RangeTypeFactory;
import org.jembi.jempi.libmpi.LibMPI;
import org.jembi.jempi.shared.libs.interactionProcessor.models.OnNewInteractionInteractionProcessorEnvelope;
import org.jembi.jempi.shared.libs.interactionProcessor.models.OnProcessCandidatesInteractionProcessorEnvelope;
import org.jembi.jempi.shared.models.GlobalConstants;
import org.jembi.jempi.shared.models.Interaction;
import org.jembi.jempi.shared.libs.interactionProcessor.models.InteractionProcessorEnvelop;
import org.jembi.jempi.shared.libs.interactionProcessor.InteractionProcessorEvents;
import org.jembi.jempi.controller.interactions_processor.lib.InteractionProcessorNotFoundException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

public class InteractionProcessorRunner {

    protected InteractionProcessorRunner() { }
    private static StandardInteractionProcessor getInteractionProcessor(final Interaction interaction, final LibMPI libMPI) {
        return new StandardInteractionProcessor(GlobalConstants.DEFAULT_LINKER_GLOBAL_STORE_NAME, interaction, libMPI);
    }

    public static void run(final InteractionProcessorEnvelop interactionProcessorEnvelop, final LibMPI libMPI) throws ExecutionException, InterruptedException, InteractionProcessorNotFoundException {

        String processorToUse = interactionProcessorEnvelop.processorToUse();
        switch (processorToUse) {
            case InteractionProcessorEvents.ON_NEW_INTERACTION:
                OnNewInteractionInteractionProcessorEnvelope interactionEnvNn = interactionProcessorEnvelop.newInteractionEnvelope();
                Interaction interactionNn = interactionEnvNn.interaction();
                // todo: Rethink the envelope stan. The correct way would be to update the envelope to contain the correct data. This change should be a part of a bigger one
                getInteractionProcessor(interactionNn, libMPI).onNewInteraction(interactionNn, interactionEnvNn.envelopeStan());
                return;
            case InteractionProcessorEvents.ON_PROCESS_CANDIDATES:
                OnProcessCandidatesInteractionProcessorEnvelope interactionOPCEnv = interactionProcessorEnvelop.newProcessCandidatesEnvelope();
                Interaction interactionOPC = interactionOPCEnv.interaction();
                Float matchThreshold = interactionOPCEnv.matchThreshold();

                StandardInteractionProcessor processor = (StandardInteractionProcessor) getInteractionProcessor(interactionOPC, libMPI).setRanges(
                        new ArrayList<>(Arrays.asList(
                                RangeTypeFactory.standardThresholdNotificationRangeBelow(matchThreshold - 0.1F, matchThreshold),
                                RangeTypeFactory.standardThresholdNotificationRangeAbove(matchThreshold, matchThreshold + 0.1F),
                                RangeTypeFactory.standardThresholdAboveThreshold(matchThreshold, 1.0F))));

                processor.onProcessCandidates(interactionOPC);
                return;
            default:
                throw new InteractionProcessorNotFoundException(String.format("The interaction processor '%s' has not been found", processorToUse));

        }
    }
}
