package org.jembi.jempi.libmpi.dgraph;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jembi.jempi.shared.models.CustomDemographicData;
import org.jembi.jempi.shared.models.CustomGoldenRecord;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
record CustomLibMPIGoldenRecord(
      @JsonProperty("uid") String uid,
      @JsonProperty("GoldenRecord.source_id") List<LibMPISourceId> sourceId,
      @JsonProperty("GoldenRecord.aux_id") String auxId,
      @JsonProperty("GoldenRecord.given_name") String givenName,
      @JsonProperty("GoldenRecord.family_name") String familyName,
      @JsonProperty("GoldenRecord.gender") String gender,
      @JsonProperty("GoldenRecord.dob") String dob,
      @JsonProperty("GoldenRecord.city") String city,
      @JsonProperty("GoldenRecord.phone_number") String phoneNumber,
      @JsonProperty("GoldenRecord.national_id") String nationalId) {

   CustomLibMPIGoldenRecord(final CustomLibMPIDGraphPatient patient) {
      this(null,
           List.of(patient.sourceId()),
           patient.auxId(),
           patient.givenName(),
           patient.familyName(),
           patient.gender(),
           patient.dob(),
           patient.city(),
           patient.phoneNumber(),
           patient.nationalId());
   }

   CustomGoldenRecord toCustomGoldenRecord() {
      return new CustomGoldenRecord(this.uid(),
                                    this.sourceId() != null
                                          ? this.sourceId().stream().map(LibMPISourceId::toSourceId).toList()
                                          : List.of(),
                                    new CustomDemographicData(this.auxId(),
                                                              this.givenName(),
                                                              this.familyName(),
                                                              this.gender(),
                                                              this.dob(),
                                                              this.city(),
                                                              this.phoneNumber(),
                                                              this.nationalId()));
   }

}
