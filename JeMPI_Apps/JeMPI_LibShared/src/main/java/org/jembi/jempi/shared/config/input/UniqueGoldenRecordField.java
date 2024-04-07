package org.jembi.jempi.shared.config.input;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
   public record UniqueGoldenRecordField(
         String fieldName,
         String fieldType,
         @JsonProperty("default") String defaultValue,
         String source) {
   }
