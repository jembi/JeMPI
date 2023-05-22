package org.jembi.jempi.shared.models;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record BatchInteraction(
      BatchType batchType,
      BatchMetaData batchMetaData,
      // System Trace Audit Number
      String stan,
      Interaction interaction) {

   public BatchInteraction(final BatchType type) {
      this(type, null, null, null);
   }

   public enum BatchType {
      BATCH_START(BatchType.BATCH_START_VALUE),
      BATCH_END(BatchType.BATCH_END_VALUE),
      BATCH_PATIENT(BatchType.BATCH_PATIENT_VALUE);
      public static final int BATCH_START_VALUE = 1;
      public static final int BATCH_END_VALUE = 2;
      public static final int BATCH_PATIENT_VALUE = 3;

      public final int type;

      BatchType(final int type_) {
         this.type = type_;
      }
   }

}

