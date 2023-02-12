package org.jembi.jempi.shared.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.jembi.jempi.shared.models.PatientRecord;
import org.jembi.jempi.shared.models.RatedGoldenRecord;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ExpandedPatientRecord(
      PatientRecord patientRecord,
      List<RatedGoldenRecord> goldenRecordScoreList) {}
