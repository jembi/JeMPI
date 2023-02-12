package org.jembi.jempi.libmpi;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.jembi.jempi.shared.models.PatientRecord;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MpiPatientRecord(
      PatientRecord patientRecord,
      Float score) {}

