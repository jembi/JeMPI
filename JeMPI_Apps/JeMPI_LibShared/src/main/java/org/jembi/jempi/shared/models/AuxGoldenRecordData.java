package org.jembi.jempi.shared.models;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuxGoldenRecordData(
      java.time.LocalDateTime auxDateCreated,
      Boolean auxAutoUpdateEnabled,
      String auxId) {

   public AuxGoldenRecordData(final AuxInteractionData uniqueInteractionData) {
      this(LocalDateTime.now(),
           true,
           uniqueInteractionData.auxId()
          );
   }

}
