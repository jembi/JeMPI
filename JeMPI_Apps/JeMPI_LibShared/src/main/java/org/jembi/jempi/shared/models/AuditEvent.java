package org.jembi.jempi.shared.models;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuditEvent(
      Long createdAt,
      Long insertedAt,
      String interactionID,
      String goldenID,
      String event) {
}
