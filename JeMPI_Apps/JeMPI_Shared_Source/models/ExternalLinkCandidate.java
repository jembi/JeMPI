package org.jembi.jempi.shared.models;

public record ExternalLinkCandidate(
      CustomGoldenRecord goldenRecord,
      float score) {
}