package org.jembi.jempi.shared.models;

public record AuditEvent(String UID,
                         Long timestamp,
                         String event) {
}
