package org.jembi.jempi.shared.models;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CustomGoldenRecord(
      String uid,
      List<SourceId> sourceId,
      CustomDemographicData demographicData) {

   public CustomGoldenRecord(final CustomPatient patient) {
      this(null, List.of(patient.sourceId()), patient.demographicData());
   }

}
