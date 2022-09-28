package org.jembi.jempi.shared.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record BatchEntity(@JsonProperty("entityType") EntityType entityType,
                          @JsonProperty("stan") String stan, // System Trace Audit Number
                          @JsonProperty("entity") CustomEntity entity) {

   public BatchEntity(EntityType entityType) {
      this(entityType, null, null);
   }

   public enum EntityType {
      BATCH_START(EntityType.BATCH_START_VALUE),
      BATCH_END(EntityType.BATCH_END_VALUE),
      BATCH_RECORD(EntityType.BATCH_RECORD_VALUE);
      public static final int BATCH_START_VALUE = 1;
      public static final int BATCH_END_VALUE = 2;
      public static final int BATCH_RECORD_VALUE = 3;

      public final int type;

      EntityType(final int type) {
         this.type = type;
      }
   }

}

