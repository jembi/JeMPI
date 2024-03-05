package org.jembi.jempi.shared.models;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.sql.Timestamp;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuditEvent(
      Timestamp createdAt,
      Timestamp insertedAt,
      String interactionID,
      String goldenID,
      String event
) {
}
