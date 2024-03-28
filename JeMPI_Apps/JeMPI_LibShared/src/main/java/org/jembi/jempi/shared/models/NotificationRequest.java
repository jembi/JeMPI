package org.jembi.jempi.shared.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record NotificationRequest(
      @JsonProperty("notificationId") String notificationId,
      @JsonProperty("state") String state,
      @JsonProperty("oldGoldenId") String oldGoldenId,
      @JsonProperty("currentGoldenId") String currentGoldenId) {

}
