package org.jembi.jempi.shared.models;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record GoldenRecordUpdateRequestPayload(List<Field> fields) {

   public record Field(
         String name,
         String oldValue,
         String newValue) {
   }

}
