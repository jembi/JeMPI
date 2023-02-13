package org.jembi.jempi.shared.models;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record RatedPatientRecord(
      PatientRecord patientRecord,
      Float score) {
}

