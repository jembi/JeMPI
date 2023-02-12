package org.jembi.jempi.libmpi.dgraph;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jembi.jempi.shared.models.RatedPatientRecord;
import org.jembi.jempi.shared.models.CustomDemographicData;
import org.jembi.jempi.shared.models.PatientRecord;

@JsonInclude(JsonInclude.Include.NON_NULL)
record CustomLibMPIDGraphPatientRecord(
      @JsonProperty("uid") String uid,
      @JsonProperty("Patient.source_id") LibMPISourceId sourceId,
      @JsonProperty("Patient.aux_id") String auxId,
      @JsonProperty("Patient.given_name") String givenName,
      @JsonProperty("Patient.family_name") String familyName,
      @JsonProperty("Patient.gender") String gender,
      @JsonProperty("Patient.dob") String dob,
      @JsonProperty("Patient.city") String city,
      @JsonProperty("Patient.phone_number") String phoneNumber,
      @JsonProperty("Patient.national_id") String nationalId,
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

   RatedPatientRecord toRatedPatientRecord() {
      return new RatedPatientRecord(toPatientRecord(), this.score());
   }

}
