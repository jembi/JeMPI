package org.jembi.jempi.shared.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record NotificationResolution(
        @JsonProperty(value = "notificationId", required = true) String notificationId,
        @JsonProperty(value = "interactionId", required = true) String interactionId,
        @JsonProperty(value = "resolutionState", required = true) String resolutionState,
        @JsonProperty(value = "currentGoldenId", required = true) String currentGoldenId,
        @JsonProperty(value = "currentCandidates", required = true) ArrayList<String> currentCandidates,
        @JsonProperty(value = "newGoldenId", required = true) String newGoldenId,
        @JsonProperty("score") Float score
) {

}





