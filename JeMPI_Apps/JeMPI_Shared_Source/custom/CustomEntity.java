package org.jembi.jempi.shared.models;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CustomEntity(String uid,
                           SourceId sourceId,
                           String auxId,
                           String givenName,
                           String familyName,
                           String gender,
                           String dob,
                           String city,
                           String phoneNumber,
                           String nationalId) {
   public CustomEntity() {
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

}
