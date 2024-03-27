package org.jembi.jempi.shared.models;


public record LinkingAuditEventData(
        String message,
        String interaction_id,
        String goldenID,
        float score,
        LinkingRule linkingRule
) {
}
