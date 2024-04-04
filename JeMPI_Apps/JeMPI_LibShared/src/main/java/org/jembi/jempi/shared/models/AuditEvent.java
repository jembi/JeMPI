package org.jembi.jempi.shared.models;

import java.sql.Timestamp;

public record AuditEvent(
      Timestamp createdAt,
      Timestamp insertedAt,
      GlobalConstants.AuditEventType eventType,
      String eventData
) {
}
