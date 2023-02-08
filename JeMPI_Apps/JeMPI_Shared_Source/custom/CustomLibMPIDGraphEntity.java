package org.jembi.jempi.libmpi.dgraph;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jembi.jempi.shared.models.CustomEntity;
import org.jembi.jempi.libmpi.MpiEntity;

@JsonInclude(JsonInclude.Include.NON_NULL)
record CustomLibMPIDGraphEntity(@JsonProperty("uid") String uid,
                                @JsonProperty("Entity.source_id") LibMPISourceId sourceId,
                                @JsonProperty("Entity.aux_id") String auxId,
                                @JsonProperty("Entity.given_name") String givenName,
                                @JsonProperty("Entity.family_name") String familyName,
                                @JsonProperty("Entity.gender") String gender,
                                @JsonProperty("Entity.dob") String dob,
                                @JsonProperty("Entity.city") String city,
                                @JsonProperty("Entity.phone_number") String phoneNumber,
                                @JsonProperty("Entity.national_id") String nationalId,
                                @JsonProperty("GoldenRecord.entity_list|score") Float score) {
   CustomLibMPIDGraphEntity(final CustomEntity entity, final Float score) {
      this(entity.uid(),
           new LibMPISourceId(entity.sourceId()),
           entity.auxId(),
           entity.givenName(),
           entity.familyName(),
           entity.gender(),
           entity.dob(),
           entity.city(),
           entity.phoneNumber(),
           entity.nationalId(),
           score);
   }

   CustomEntity toCustomEntity() {
      return new CustomEntity(this.uid(),
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

   MpiEntity toMpiEntity() {
      return new MpiEntity(toCustomEntity(), this.score());
   }

}
