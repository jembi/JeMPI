package org.jembi.jempi.libmpi.dgraph;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

import org.jembi.jempi.shared.models.CustomEntity;
import org.jembi.jempi.libmpi.MpiExpandedEntity;

@JsonInclude(JsonInclude.Include.NON_NULL)
record CustomLibMPIExpandedEntity(@JsonProperty("uid") String uid,
                                  @JsonProperty("Entity.source_id") LibMPISourceId sourceId,
                                  @JsonProperty("Entity.aux_id") String auxId,
                                  @JsonProperty("Entity.given_name") String givenName,
                                  @JsonProperty("Entity.family_name") String familyName,
                                  @JsonProperty("Entity.gender") String gender,
                                  @JsonProperty("Entity.dob") String dob,
                                  @JsonProperty("Entity.city") String city,
                                  @JsonProperty("Entity.phone_number") String phoneNumber,
                                  @JsonProperty("Entity.national_id") String nationalId,
                                  @JsonProperty("~GoldenRecord.entity_list") List<CustomLibMPIDGraphGoldenRecord>
                                        dgraphGoldenRecordList) {


   CustomEntity toCustomEntity() {
      return new CustomEntity(this.uid(),
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

   MpiExpandedEntity toMpiExpandedEntity() {
      return new MpiExpandedEntity(this.toCustomEntity(),
                                   this.dgraphGoldenRecordList()
                                       .stream()
                                       .map(CustomLibMPIDGraphGoldenRecord::toMpiGoldenRecord)
                                       .toList());
   }

}
