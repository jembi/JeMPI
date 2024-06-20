package org.jembi.jempi.shared.models;

public record Validation(
      String interactionId,
      String goldenRecordId,
      boolean deterministicScore,
      float probabilisticScore
) {
}
