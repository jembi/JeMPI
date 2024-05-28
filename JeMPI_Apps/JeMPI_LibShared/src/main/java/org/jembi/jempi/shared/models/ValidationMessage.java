package org.jembi.jempi.shared.models;

public record ValidationMessage(
      Interaction interaction,
      GoldenRecord goldenRecord,
      float deterministicScore,
      float probabilisticScore
) {
}
