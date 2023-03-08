package org.jembi.jempi.libmpi.dgraph;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jembi.jempi.shared.models.PatientRecordWithScore;
import org.jembi.jempi.shared.models.CustomDemographicData;
import org.jembi.jempi.shared.models.PatientRecord;

@JsonInclude(JsonInclude.Include.NON_NULL)
record CustomDgraphPatientRecord(
      @JsonProperty("uid") String patientId,
      @JsonProperty("PatientRecord.source_id") DgraphSourceId sourceId,
      @JsonProperty("PatientRecord.aux_id") String auxId,
      @JsonProperty("PatientRecord.fpid") String fpid,
      @JsonProperty("PatientRecord.gender") String gender,
      @JsonProperty("PatientRecord.dob") String dob,
      @JsonProperty("GoldenRecord.patients|score") Float score) {
   CustomDgraphPatientRecord(
         final PatientRecord patientRecord,
         final Float score) {
      this(patientRecord.patientId(),
           new DgraphSourceId(patientRecord.sourceId()),
           patientRecord.demographicData().auxId(),
           patientRecord.demographicData().fpid(),
           patientRecord.demographicData().gender(),
           patientRecord.demographicData().dob(),
           score);
   }

   PatientRecord toPatientRecord() {
      return new PatientRecord(this.patientId(),
                               this.sourceId() != null
                                     ? this.sourceId().toSourceId()
                                     : null,
                               new CustomDemographicData(this.auxId(),
                                                         this.fpid(),
                                                         this.gender(),
                                                         this.dob()));
   }

   PatientRecordWithScore toPatientRecordWithScore() {
      return new PatientRecordWithScore(toPatientRecord(), this.score());
   }

}
