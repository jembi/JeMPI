package org.jembi.jempi.controller.interactions_processor.processors;

import org.jembi.jempi.shared.models.Interaction;

public interface IOnNewInteractionProcessor extends ISubProcessor {

    void onNewInteraction(Interaction interaction, String envelopeStan) throws Exception;
}
