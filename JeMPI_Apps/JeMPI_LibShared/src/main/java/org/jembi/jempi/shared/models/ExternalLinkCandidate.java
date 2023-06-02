package org.jembi.jempi.shared.models;

public record ExternalLinkCandidate(
      GoldenRecord goldenRecord,
      float score) {
}
