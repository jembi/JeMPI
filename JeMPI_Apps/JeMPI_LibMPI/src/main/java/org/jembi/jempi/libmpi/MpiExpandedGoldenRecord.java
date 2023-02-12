package org.jembi.jempi.libmpi;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.jembi.jempi.shared.models.GoldenRecord;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MpiExpandedGoldenRecord(
      GoldenRecord goldenRecord,
      List<MpiPatientRecord> mpiPatientRecords) {
}
