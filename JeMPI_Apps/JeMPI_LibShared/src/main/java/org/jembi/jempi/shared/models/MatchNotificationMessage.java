package org.jembi.jempi.shared.models;

public record MatchNotificationMessage(
      Interaction interaction,
      GoldenRecordWithScore candidatesWithScores
) {
}
