package org.jembi.jempi.libmpi.dgraph;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

import org.jembi.jempi.shared.models.CustomPatient;
import org.jembi.jempi.libmpi.MpiExpandedPatient;

@JsonInclude(JsonInclude.Include.NON_NULL)
record CustomLibMPIExpandedPatient(@JsonProperty("uid") String uid,
                                  @JsonProperty("Patient.source_id") LibMPISourceId sourceId,
                                  @JsonProperty("Patient.aux_id") String auxId,
                                  @JsonProperty("Patient.given_name") String givenName,
                                  @JsonProperty("Patient.family_name") String familyName,
                                  @JsonProperty("Patient.gender") String gender,
                                  @JsonProperty("Patient.dob") String dob,
                                  @JsonProperty("Patient.city") String city,
                                  @JsonProperty("Patient.phone_number") String phoneNumber,
                                  @JsonProperty("Patient.national_id") String nationalId,
                                  @JsonProperty("~GoldenRecord.patients") List<CustomLibMPIDGraphGoldenRecord>
                                        dgraphGoldenRecordList) {


   CustomPatient toCustomPatient() {
      return new CustomPatient(this.uid(),
                              this.sourceId().toSourceId(),
                              this.auxId(),
                              this.givenName(),
                              this.familyName(),
                              this.gender(),
                              this.dob(),
                              this.city(),
                              this.phoneNumber(),
                              this.nationalId());
   }

   MpiExpandedPatient toMpiExpandedPatient() {
      return new MpiExpandedPatient(this.toCustomPatient(),
                                   this.dgraphGoldenRecordList()
                                       .stream()
                                       .map(CustomLibMPIDGraphGoldenRecord::toMpiGoldenRecord)
                                       .toList());
   }

}
