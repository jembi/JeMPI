package org.jembi.jempi.shared.config.input;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
   public record ValidateMetaData(
         String comparison,
         List<Double> comparisonLevels,
         Double m,
         Double u) {
   }
