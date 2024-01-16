package org.jembi.jempi.shared.libs.interactionProcessor.models;

import org.jembi.jempi.shared.models.Interaction;

import java.util.Map;

public record OnProcessCandidatesInteractionProcessorEnvelope(Interaction interaction, String envelopeStan, Float matchThreshold, Map<String, Float> candidatesWithScores) {
}
