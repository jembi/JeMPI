package org.jembi.jempi.shared.config.input;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
   public record DemographicField(
         @JsonProperty("fieldName") String scFieldName,
         @JsonProperty("fieldType") String fieldType,
         @JsonProperty("source") Source source,
         @JsonProperty("indexGoldenRecord") String indexGoldenRecord,
         @JsonProperty("indexInteraction") String indexInteraction,
         @JsonProperty("linkMetaData") MetaDataLink linkMetaData,
         @JsonProperty("validateMetaData") MetaDataValidate validateMetaData,
         @JsonProperty("matchMetaData") MetaDataMatch matchMetaData) {
   }
