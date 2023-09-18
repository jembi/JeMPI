package org.jembi.jempi.shared.models;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CustomMU(Probability dummy) {

   public CustomMU(final double[] mHat, final double[] uHat) {
      this(new CustomMU.Probability(0.0F, 0.0F));
   }

   public record Probability(float m, float u) {
   }

}
