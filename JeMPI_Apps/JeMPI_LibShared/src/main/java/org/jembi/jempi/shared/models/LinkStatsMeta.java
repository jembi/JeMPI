package org.jembi.jempi.shared.models;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record LinkStatsMeta(
      ConfusionMatrix confusionMatrix,
      CustomFieldTallies customFieldTallies) {

   public static final ConfusionMatrix CONFUSION_MATRIX_IDENTITY = new ConfusionMatrix(0.0, 0.0, 0.0, 0.0);

   @JsonInclude(JsonInclude.Include.NON_NULL)
   public record ConfusionMatrix(
         Double TP,
         Double FP,
         Double TN,
         Double FN) {

      public ConfusionMatrix sum(final ConfusionMatrix right) {
         return new ConfusionMatrix(this.TP + right.TP,
                                    this.FP + right.FP,
                                    this.TN + right.TN,
                                    this.FN + right.FN);
      }

   }

}
