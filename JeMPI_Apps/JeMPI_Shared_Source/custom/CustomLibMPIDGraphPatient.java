package org.jembi.jempi.libmpi.dgraph;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jembi.jempi.libmpi.MpiPatient;
import org.jembi.jempi.shared.models.CustomDemographicData;
import org.jembi.jempi.shared.models.CustomPatient;

@JsonInclude(JsonInclude.Include.NON_NULL)
record CustomLibMPIDGraphPatient(
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
   CustomLibMPIDGraphPatient(
         final CustomPatient patient,
         final Float score) {
      this(patient.uid(),
           new LibMPISourceId(patient.sourceId()),
           patient.demographicData().auxId(),
           patient.demographicData().givenName(),
           patient.demographicData().familyName(),
           patient.demographicData().gender(),
           patient.demographicData().dob(),
           patient.demographicData().city(),
           patient.demographicData().phoneNumber(),
           patient.demographicData().nationalId(),
           score);
   }

   CustomPatient toCustomPatient() {
      return new CustomPatient(this.uid(),
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

   MpiPatient toMpiPatient() {
      return new MpiPatient(toCustomPatient(), this.score());
   }

}
