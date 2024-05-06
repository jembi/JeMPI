package org.jembi.jempi.libmpi.dgraph;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jembi.jempi.shared.config.DGraphConfig;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
record CustomDgraphExpandedGoldenRecord(
      @JsonProperty("uid") String goldenId,
      @JsonProperty("GoldenRecord.source_id") List<DgraphSourceId> sourceId,
      @JsonProperty(DGraphConfig.PREDICATE_GOLDEN_RECORD_AUX_DATE_CREATED) java.time.LocalDateTime auxDateCreated,
      @JsonProperty(DGraphConfig.PREDICATE_GOLDEN_RECORD_AUX_AUTO_UPDATE_ENABLED) Boolean auxAutoUpdateEnabled,
      @JsonProperty(DGraphConfig.PREDICATE_GOLDEN_RECORD_AUX_ID) String auxId,
      @JsonProperty("GoldenRecord.demographic_field_00") String givenName,
      @JsonProperty("GoldenRecord.demographic_field_01") String familyName,
      @JsonProperty("GoldenRecord.demographic_field_02") String gender,
      @JsonProperty("GoldenRecord.demographic_field_03") String dob,
      @JsonProperty("GoldenRecord.demographic_field_04") String city,
      @JsonProperty("GoldenRecord.demographic_field_05") String phoneNumber,
      @JsonProperty("GoldenRecord.demographic_field_06") String nationalId,
      @JsonProperty("GoldenRecord.interactions") List<CustomDgraphInteraction> interactions) {

}
