package org.jembi.jempi.libmpi.dgraph;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jembi.jempi.shared.models.CustomDemographicData;
import org.jembi.jempi.shared.models.ExpandedPatientRecord;
import org.jembi.jempi.shared.models.PatientRecord;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
record CustomDgraphExpandedPatientRecord(
      @JsonProperty("uid") String patientId,
      @JsonProperty("PatientRecord.source_id") DgraphSourceId sourceId,
      @JsonProperty("PatientRecord.aux_id") String auxId,
      @JsonProperty("PatientRecord.fpid") String fpid,
      @JsonProperty("PatientRecord.gender") String gender,
      @JsonProperty("PatientRecord.dob") String dob,
      @JsonProperty("~GoldenRecord.patients") List<CustomDgraphReverseGoldenRecord> dgraphGoldenRecordList) {

   PatientRecord toPatientRecord() {
      return new PatientRecord(this.patientId(),
                               this.sourceId().toSourceId(),
                               new CustomDemographicData(
                                     this.auxId(),
                                     this.fpid(),
                                     this.gender(),
                                     this.dob()));
   }

   ExpandedPatientRecord toExpandedPatientRecord() {
      return new ExpandedPatientRecord(this.toPatientRecord(),
                                       this.dgraphGoldenRecordList()
                                           .stream()
                                           .map(CustomDgraphReverseGoldenRecord::toGoldenRecordWithScore)
                                           .toList());
   }

}
