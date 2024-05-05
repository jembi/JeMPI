package org.jembi.jempi.shared.config.input;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
   public record AuxInteractionField(
         String fieldName,
         String fieldType,
         Integer csvCol) {
   }
