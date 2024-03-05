package org.jembi.jempi.shared.models;

public record ExpandedAuditEvent(
        AuditEvent event,
        AuditEventType eventType,
        String eventData
) {

}
