package org.jembi.jempi.libmpi.dgraph;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jembi.jempi.shared.models.CustomDemographicData;
import org.jembi.jempi.shared.models.ExpandedInteraction;
import org.jembi.jempi.shared.models.Interaction;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
record CustomDgraphExpandedInteraction(
      @JsonProperty("uid") String patientId,
      @JsonProperty("Interaction.source_id") DgraphSourceId sourceId,
      @JsonProperty(CustomDgraphConstants.PREDICATE_INTERACTION_AUX_ID) String auxId,
      @JsonProperty(CustomDgraphConstants.PREDICATE_INTERACTION_GIVEN_NAME) String givenName,
      @JsonProperty(CustomDgraphConstants.PREDICATE_INTERACTION_FAMILY_NAME) String familyName,
      @JsonProperty(CustomDgraphConstants.PREDICATE_INTERACTION_GENDER) String gender,
      @JsonProperty(CustomDgraphConstants.PREDICATE_INTERACTION_DOB) String dob,
      @JsonProperty(CustomDgraphConstants.PREDICATE_INTERACTION_CITY) String city,
      @JsonProperty(CustomDgraphConstants.PREDICATE_INTERACTION_PHONE_NUMBER) String phoneNumber,
      @JsonProperty(CustomDgraphConstants.PREDICATE_INTERACTION_NATIONAL_ID) String nationalId,
      @JsonProperty("~GoldenRecord.interactions") List<CustomDgraphReverseGoldenRecord> dgraphGoldenRecordList) {

   Interaction toInteraction() {
      return new Interaction(this.patientId(),
                             this.sourceId().toSourceId(),
                             new CustomDemographicData(
                                     this.auxId(),
                                     this.givenName(),
                                     this.familyName(),
                                     this.gender(),
                                     this.dob(),
                                     this.city(),
                                     this.phoneNumber(),
                                     this.nationalId()));
   }

   ExpandedInteraction toExpandedInteraction() {
      return new ExpandedInteraction(this.toInteraction(),
                                     this.dgraphGoldenRecordList()
                                         .stream()
                                         .map(CustomDgraphReverseGoldenRecord::toGoldenRecordWithScore)
                                         .toList());
   }

}
