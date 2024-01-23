package org.jembi.jempi.shared.libs.interactionProcessor.models;

public record InteractionProcessorEnvelop(String processorToUse,
                                          OnNewInteractionInteractionProcessorEnvelope newInteractionEnvelope,
                                          OnProcessCandidatesInteractionProcessorEnvelope newProcessCandidatesEnvelope) {
}
