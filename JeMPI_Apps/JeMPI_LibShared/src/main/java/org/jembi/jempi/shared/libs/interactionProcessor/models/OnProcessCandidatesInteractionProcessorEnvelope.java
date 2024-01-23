package org.jembi.jempi.shared.libs.interactionProcessor.models;

import org.jembi.jempi.shared.models.Interaction;

public record OnProcessCandidatesInteractionProcessorEnvelope(Interaction interaction, String envelopeStan, Float matchThreshold) {
}
