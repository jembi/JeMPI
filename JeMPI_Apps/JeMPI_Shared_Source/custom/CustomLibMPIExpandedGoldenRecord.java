package org.jembi.jempi.libmpi.dgraph;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

import org.jembi.jempi.shared.models.CustomGoldenRecord;
import org.jembi.jempi.libmpi.MpiExpandedGoldenRecord;

@JsonInclude(JsonInclude.Include.NON_NULL)
record CustomLibMPIExpandedGoldenRecord(@JsonProperty("uid") String uid,
                                        @JsonProperty("GoldenRecord.source_id") List<LibMPISourceId> sourceId,
                                        @JsonProperty("GoldenRecord.aux_id") String auxId,
                                        @JsonProperty("GoldenRecord.given_name") String givenName,
                                        @JsonProperty("GoldenRecord.family_name") String familyName,
                                        @JsonProperty("GoldenRecord.gender") String gender,
                                        @JsonProperty("GoldenRecord.dob") String dob,
                                        @JsonProperty("GoldenRecord.city") String city,
                                        @JsonProperty("GoldenRecord.phone_number") String phoneNumber,
                                        @JsonProperty("GoldenRecord.national_id") String nationalId,
                                        @JsonProperty("GoldenRecord.entity_list") List<CustomLibMPIDGraphEntity> dgraphEntityList) {


   CustomGoldenRecord toCustomGoldenRecord() {
      return new CustomGoldenRecord(this.uid(),
                                    this.sourceId() != null
                                       ? this.sourceId().stream().map(LibMPISourceId::toSourceId).toList()
                                       : List.of(),
                                    this.auxId(),
                                    this.givenName(),
                                    this.familyName(),
                                    this.gender(),
                                    this.dob(),
                                    this.city(),
                                    this.phoneNumber(),
                                    this.nationalId());
   }

   MpiExpandedGoldenRecord toMpiExpandedGoldenRecord() {
      return new MpiExpandedGoldenRecord(this.toCustomGoldenRecord(),
                                         this.dgraphEntityList()
                                             .stream()
                                             .map(CustomLibMPIDGraphEntity::toMpiEntity)
                                             .toList());
   }

}
