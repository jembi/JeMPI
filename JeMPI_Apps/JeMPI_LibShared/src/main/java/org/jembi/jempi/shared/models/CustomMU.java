package org.jembi.jempi.shared.models;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CustomMU(Probability nationalId) {

   public CustomMU(final double[] mHat, final double[] uHat) {
      this(new CustomMU.Probability((float) mHat[0], (float) uHat[0]));
   }

   public record Probability(float m, float u) {
   }

}
