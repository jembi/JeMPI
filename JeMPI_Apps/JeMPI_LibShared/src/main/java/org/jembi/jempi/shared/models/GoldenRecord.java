package org.jembi.jempi.shared.models;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record GoldenRecord(
      String goldenId,
      List<SourceId> sourceId,
      CustomDemographicData demographicData,
       CustomUniqueGoldenRecordData customUniqueGoldenRecordData
) {

   public GoldenRecord(final Interaction patient) {
      this(null, List.of(patient.sourceId()), patient.demographicData(), new CustomUniqueGoldenRecordData(true));
   }

}
