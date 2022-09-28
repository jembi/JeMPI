DEPRECATED

package org.jembi.jempi.shared.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CustomEntity(@JsonProperty("mpiID") String mpiID,
                           @JsonProperty("auxID") String auxID,
                           @JsonProperty("givenName") String givenName,
                           @JsonProperty("familyName") String familyName,
                           @JsonProperty("genderAtBirth") String gender,
                           @JsonProperty("dateOfBirth") String dob,
                           @JsonProperty("city") String city,
                           @JsonProperty("phoneNumber") String phoneNumber,
                           @JsonProperty("nationalID") String nationalID) {

   public CustomEntity() {
      this(null,
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

