package org.jembi.jempi.shared.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record Interaction(
      @JsonProperty("interactionId") String interactionId,
      @JsonProperty("sourceId") SourceId sourceId,
      @JsonProperty("uniqueInteractionData") AuxInteractionData auxInteractionData,
      @JsonProperty("demographicData") DemographicData demographicData) {
}
