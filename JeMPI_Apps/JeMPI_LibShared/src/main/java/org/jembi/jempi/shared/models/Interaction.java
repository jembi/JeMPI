package org.jembi.jempi.shared.models;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record Interaction(
      String interactionId,
      SourceId sourceId,
      CustomUniqueInteractionData uniqueInteractionData,
      CustomDemographicData demographicData) {
}

