package org.jembi.jempi.libmpi.dgraph;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jembi.jempi.shared.models.CustomPatient;
import org.jembi.jempi.libmpi.MpiPatient;

@JsonInclude(JsonInclude.Include.NON_NULL)
record CustomLibMPIDGraphPatient(@JsonProperty("uid") String uid,
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
   CustomLibMPIDGraphPatient(final CustomPatient patient, final Float score) {
      this(patient.uid(),
           new LibMPISourceId(patient.sourceId()),
           patient.auxId(),
           patient.givenName(),
           patient.familyName(),
           patient.gender(),
           patient.dob(),
           patient.city(),
           patient.phoneNumber(),
           patient.nationalId(),
           score);
   }

   CustomPatient toCustomPatient() {
      return new CustomPatient(this.uid(),
                              this.sourceId() != null
                                 ? this.sourceId().toSourceId()
                                 : null,
                              this.auxId(),
                              this.givenName(),
                              this.familyName(),
                              this.gender(),
                              this.dob(),
                              this.city(),
                              this.phoneNumber(),
                              this.nationalId());
   }

   MpiPatient toMpiPatient() {
      return new MpiPatient(toCustomPatient(), this.score());
   }

}
