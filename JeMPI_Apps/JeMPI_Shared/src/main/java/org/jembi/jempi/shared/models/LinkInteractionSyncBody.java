package org.jembi.jempi.shared.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record LinkInteractionSyncBody(
      @JsonProperty("stan") String stan,
      @JsonProperty("externalLinkRange") ExternalLinkRange externalLinkRange,
      @JsonProperty("matchThreshold") Float matchThreshold,
      @JsonProperty("patientRecord") Interaction interaction) {
}
