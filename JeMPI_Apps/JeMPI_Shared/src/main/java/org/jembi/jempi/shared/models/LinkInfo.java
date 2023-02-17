package org.jembi.jempi.shared.models;

public record LinkInfo(
      String goldenUID,
      String patientUID,
      float score) {
}
