package org.jembi.jempi.shared.config.input;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuxInteractionField(
      @JsonProperty("fieldName") String scFieldName,
      @JsonProperty("fieldType") String fieldType,
      @JsonProperty("source") Source source) {
}
