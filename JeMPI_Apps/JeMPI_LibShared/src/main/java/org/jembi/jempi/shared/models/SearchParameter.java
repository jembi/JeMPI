package org.jembi.jempi.shared.models;

public record SearchParameter(
      String value,
      String fieldName,
      Integer distance) {

}
