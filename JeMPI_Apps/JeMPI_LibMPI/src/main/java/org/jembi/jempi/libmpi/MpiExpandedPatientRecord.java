package org.jembi.jempi.libmpi;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.jembi.jempi.shared.models.PatientRecord;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MpiExpandedPatientRecord(
      PatientRecord patientRecord,
      List<MpiGoldenRecord> mpiGoldenRecords) {}
