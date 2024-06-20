package org.jembi.jempi.shared.models;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record FieldsConfiguration(
      List<Field> fields,
      List<Field> systemFields) {

   public record Field(
         String fieldName,
         String fieldType,
         String fieldLabel,
         List<String> groups,
         List<String> scope,
         boolean readOnly,
         Validation validation,
         List<String> accessLevel) {
   }

   public record Validation(
         boolean required,
         String regex,
         String onErrorMessage) {
   }
}
