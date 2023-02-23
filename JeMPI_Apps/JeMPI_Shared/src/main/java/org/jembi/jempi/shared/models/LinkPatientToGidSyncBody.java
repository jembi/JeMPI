package org.jembi.jempi.shared.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record LinkPatientToGidSyncBody(
      @JsonProperty("stan") String stan,
      @JsonProperty("patientRecord") PatientRecord patientRecord,
      @JsonProperty("gid") String gid) {
}
