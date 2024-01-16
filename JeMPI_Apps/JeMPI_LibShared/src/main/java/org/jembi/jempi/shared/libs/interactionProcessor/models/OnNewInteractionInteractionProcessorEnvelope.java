package org.jembi.jempi.shared.libs.interactionProcessor.models;

import org.jembi.jempi.shared.models.Interaction;

public record OnNewInteractionInteractionProcessorEnvelope(Interaction interaction,  String envelopeStan) {
}
