DEPRECATED

package org.jembi.jempi.shared.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CustomMU(@JsonProperty("givenName") Probability givenName,
                       @JsonProperty("familyName") Probability familyName,
                       @JsonProperty("gender") Probability gender,
                       @JsonProperty("dob") Probability dob,
                       @JsonProperty("city") Probability city,
                       @JsonProperty("phoneNumber") Probability phoneNumber,
                       @JsonProperty("nationalID") Probability nationalID) {

   public CustomMU(final double[] mHat, final double[] uHat) {
      this(new CustomMU.Probability((float) mHat[0], (float) uHat[0]),
           new CustomMU.Probability((float) mHat[1], (float) uHat[1]),
           new CustomMU.Probability((float) mHat[2], (float) uHat[2]),
           new CustomMU.Probability((float) mHat[3], (float) uHat[3]),
           new CustomMU.Probability((float) mHat[4], (float) uHat[4]),
           new CustomMU.Probability((float) mHat[5], (float) uHat[5]),
           new CustomMU.Probability((float) mHat[6], (float) uHat[6]));
   }

   public record Probability(float m, float u) {
   }

}



