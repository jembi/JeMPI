package org.jembi.jempi.shared.config.input;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
   public record DemographicField(
         String fieldName,
         String fieldType,
         Source source,
         String indexGoldenRecord,
         String indexInteraction,
         LinkMetaData linkMetaData,
         ValidateMetaData validateMetaData,
         MatchMetaData matchMetaData) {
   }
