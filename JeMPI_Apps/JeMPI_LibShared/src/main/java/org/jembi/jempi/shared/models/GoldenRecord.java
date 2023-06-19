package org.jembi.jempi.shared.models;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record GoldenRecord(
      String goldenId,
      List<CustomSourceId> sourceId,
      CustomUniqueGoldenRecordData customUniqueGoldenRecordData,
      CustomDemographicData demographicData) {

   public GoldenRecord(final Interaction interaction) {
      this(null,
           List.of(interaction.sourceId()),
           new CustomUniqueGoldenRecordData(LocalDateTime.now(),
                                            true,
                                            interaction.uniqueInteractionData().auxId()),
           interaction.demographicData());
   }

}
