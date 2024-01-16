package org.jembi.jempi.shared.models;

public record LinkInfo(
      String goldenUID,
      String interactionUID,
      String sourceUID,
      float score) {
}
