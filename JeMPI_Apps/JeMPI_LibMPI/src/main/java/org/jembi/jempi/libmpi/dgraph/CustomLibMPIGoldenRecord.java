package org.jembi.jempi.libmpi.dgraph;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jembi.jempi.shared.models.CustomDemographicData;
import org.jembi.jempi.shared.models.GoldenRecord;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
record CustomLibMPIGoldenRecord(
      @JsonProperty("uid") String uid,
      @JsonProperty("GoldenRecord.source_id") List<DgraphSourceId> sourceId,
      @JsonProperty("GoldenRecord.aux_id") String auxId,
      @JsonProperty("GoldenRecord.given_name") String givenName,
      @JsonProperty("GoldenRecord.family_name") String familyName,
      @JsonProperty("GoldenRecord.gender") String gender,
      @JsonProperty("GoldenRecord.dob") String dob,
      @JsonProperty("GoldenRecord.city") String city,
      @JsonProperty("GoldenRecord.phone_number") String phoneNumber,
      @JsonProperty("GoldenRecord.national_id") String nationalId) {

   CustomLibMPIGoldenRecord(final CustomLibMPIDGraphPatientRecord rec) {
      this(null,
           List.of(rec.sourceId()),
           rec.auxId(),
           rec.givenName(),
           rec.familyName(),
           rec.gender(),
           rec.dob(),
           rec.city(),
           rec.phoneNumber(),
           rec.nationalId());
   }

   GoldenRecord toGoldenRecord() {
      return new GoldenRecord(this.uid(),
                              this.sourceId() != null
                                    ? this.sourceId().stream().map(DgraphSourceId::toSourceId).toList()
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
