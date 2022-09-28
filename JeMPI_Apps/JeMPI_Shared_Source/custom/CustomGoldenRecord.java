package org.jembi.jempi.shared.models;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CustomGoldenRecord(String uid,
                                 List<SourceId> sourceId,
                                 String auxId,
                                 String givenName,
                                 String familyName,
                                 String gender,
                                 String dob,
                                 String city,
                                 String phoneNumber,
                                 String nationalId) {
   public CustomGoldenRecord() {
      this(null,
           null,
           null,
           null,
           null,
           null,
           null,
           null,
           null,
           null);
   }

   public CustomGoldenRecord(final CustomEntity entity) {
      this(null,
           List.of(entity.sourceId()),
           entity.auxId(),
           entity.givenName(),
           entity.familyName(),
           entity.gender(),
           entity.dob(),
           entity.city(),
           entity.phoneNumber(),
           entity.nationalId());
   }

}
