package org.jembi.jempi.linker.linker_processor.processors;

import org.jembi.jempi.shared.models.Interaction;

public interface IOnNewInteractionProcessor extends ISubProcessor {

    void onNewInteraction(Interaction interaction);
}
