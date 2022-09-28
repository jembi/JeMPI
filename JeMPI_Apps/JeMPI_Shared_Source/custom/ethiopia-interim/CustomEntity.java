DEPRECATED 

package org.jembi.jempi.shared.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CustomEntity(@JsonProperty("mpiID") String mpiID,
                           @JsonProperty("auxID") String auxID,
                           @JsonProperty("givenName") String givenName,
                           @JsonProperty("fathersName") String fathersName,
                           @JsonProperty("fathersFatherName") String fathersFatherName,
                           @JsonProperty("mothersName") String mothersName,
                           @JsonProperty("mothersFatherName") String mothersFatherName,
                           @JsonProperty("genderAtBirth") String gender,
                           @JsonProperty("dateOfBirth") String dob,
                           @JsonProperty("city") String city,
                           @JsonProperty("phoneNumber") String phoneNumber) {

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
           null,
           null);
   }

}

