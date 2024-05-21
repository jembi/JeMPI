package org.jembi.jempi.shared.config.input;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
   public record Rules(
         LinkRules link,
         LinkRules validate,
         LinkRules matchNotification) {
   }
