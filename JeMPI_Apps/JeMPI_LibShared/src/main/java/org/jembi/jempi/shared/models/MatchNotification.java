package org.jembi.jempi.shared.models;

public record MatchNotification(
      Interaction interaction,
      GoldenRecordWithScore candidatesWithScores
) {
}
