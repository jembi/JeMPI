package org.jembi.jempi.shared.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record LinkInteractionToGidSyncBody(
      @JsonProperty("stan") String stan,
      @JsonProperty("patientRecord") Interaction interaction,
      @JsonProperty("gid") String gid) {
}
