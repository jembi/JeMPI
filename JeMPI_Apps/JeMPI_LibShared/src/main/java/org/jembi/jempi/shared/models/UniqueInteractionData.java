package org.jembi.jempi.shared.models;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UniqueInteractionData(List<UniqueInteractionField> fields) {
//                                  java.time.LocalDateTime auxDateCreated,
//                                  String auxId,
//                                  String auxClinicalData) {

   public record UniqueInteractionField(
         String tag,
         Object value) {
   }

}
