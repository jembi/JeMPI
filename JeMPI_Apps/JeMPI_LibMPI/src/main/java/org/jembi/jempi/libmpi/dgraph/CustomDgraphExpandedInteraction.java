package org.jembi.jempi.libmpi.dgraph;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jembi.jempi.shared.config.DGraphConfig;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
record CustomDgraphExpandedInteraction(
      @JsonProperty("uid") String interactionId,
      @JsonProperty("Interaction.source_id") DgraphSourceId sourceId,
      @JsonProperty(DGraphConfig.PREDICATE_INTERACTION_AUX_DATE_CREATED) java.time.LocalDateTime auxDateCreated,
      @JsonProperty(DGraphConfig.PREDICATE_INTERACTION_AUX_ID) String auxId,
      @JsonProperty(DGraphConfig.PREDICATE_INTERACTION_AUX_CLINICAL_DATA) String auxClinicalData,
      @JsonProperty("Interaction.demographic_field_00") String givenName,
      @JsonProperty("Interaction.demographic_field_01") String familyName,
      @JsonProperty("Interaction.demographic_field_02") String gender,
      @JsonProperty("Interaction.demographic_field_03") String dob,
      @JsonProperty("Interaction.demographic_field_04") String city,
      @JsonProperty("Interaction.demographic_field_05") String phoneNumber,
      @JsonProperty("Interaction.demographic_field_06") String nationalId,
      @JsonProperty("~GoldenRecord.interactions") List<CustomDgraphReverseGoldenRecord> dgraphGoldenRecordList) {

}

