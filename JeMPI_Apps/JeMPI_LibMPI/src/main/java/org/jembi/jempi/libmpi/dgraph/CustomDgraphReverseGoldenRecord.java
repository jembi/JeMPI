package org.jembi.jempi.libmpi.dgraph;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
record CustomDgraphReverseGoldenRecord(
      @JsonProperty("uid") String goldenId,
      @JsonProperty("GoldenRecord.source_id") List<DgraphSourceId> sourceId,
      @JsonProperty(CustomDgraphConstants.PREDICATE_GOLDEN_RECORD_AUX_DATE_CREATED) java.time.LocalDateTime auxDateCreated,
      @JsonProperty(CustomDgraphConstants.PREDICATE_GOLDEN_RECORD_AUX_AUTO_UPDATE_ENABLED) Boolean auxAutoUpdateEnabled,
      @JsonProperty(CustomDgraphConstants.PREDICATE_GOLDEN_RECORD_AUX_ID) String auxId,
      @JsonProperty(CustomDgraphConstants.PREDICATE_GOLDEN_RECORD_GIVEN_NAME) String givenName,
      @JsonProperty(CustomDgraphConstants.PREDICATE_GOLDEN_RECORD_FAMILY_NAME) String familyName,
      @JsonProperty(CustomDgraphConstants.PREDICATE_GOLDEN_RECORD_GENDER) String gender,
      @JsonProperty(CustomDgraphConstants.PREDICATE_GOLDEN_RECORD_DOB) String dob,
      @JsonProperty(CustomDgraphConstants.PREDICATE_GOLDEN_RECORD_CITY) String city,
      @JsonProperty(CustomDgraphConstants.PREDICATE_GOLDEN_RECORD_PHONE_NUMBER) String phoneNumber,
      @JsonProperty(CustomDgraphConstants.PREDICATE_GOLDEN_RECORD_NATIONAL_ID) String nationalId,
      @JsonProperty("~GoldenRecord.interactions|score") Float score) {

}

