package org.jembi.jempi.libmpi.dgraph;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jembi.jempi.shared.models.PatientRecordWithScore;
import org.jembi.jempi.shared.models.CustomDemographicData;
import org.jembi.jempi.shared.models.PatientRecord;

@JsonInclude(JsonInclude.Include.NON_NULL)
record CustomLibMPIDGraphPatientRecord(
      @JsonProperty("uid") String uid,
      @JsonProperty("PatientRecord.source_id") LibMPISourceId sourceId,
      @JsonProperty("PatientRecord.aux_id") String auxId,
      @JsonProperty("PatientRecord.given_name") String givenName,
      @JsonProperty("PatientRecord.family_name") String familyName,
      @JsonProperty("PatientRecord.gender") String gender,
      @JsonProperty("PatientRecord.dob") String dob,
      @JsonProperty("PatientRecord.city") String city,
      @JsonProperty("PatientRecord.phone_number") String phoneNumber,
      @JsonProperty("PatientRecord.national_id") String nationalId,
      @JsonProperty("GoldenRecord.patients|score") Float score) {
   CustomLibMPIDGraphPatientRecord(
         final PatientRecord patientRecord,
         final Float score) {
      this(patientRecord.uid(),
           new LibMPISourceId(patientRecord.sourceId()),
           patientRecord.demographicData().auxId(),
           patientRecord.demographicData().givenName(),
           patientRecord.demographicData().familyName(),
           patientRecord.demographicData().gender(),
           patientRecord.demographicData().dob(),
           patientRecord.demographicData().city(),
           patientRecord.demographicData().phoneNumber(),
           patientRecord.demographicData().nationalId(),
           score);
   }

   PatientRecord toPatientRecord() {
      return new PatientRecord(this.uid(),
                               this.sourceId() != null
                                     ? this.sourceId().toSourceId()
                                     : null,
                               new CustomDemographicData(this.auxId(),
                                                         this.givenName(),
                                                         this.familyName(),
                                                         this.gender(),
                                                         this.dob(),
                                                         this.city(),
                                                         this.phoneNumber(),
                                                         this.nationalId()));
   }

   PatientRecordWithScore toPatientRecordWithScore() {
      return new PatientRecordWithScore(toPatientRecord(), this.score());
   }

}
