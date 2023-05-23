package org.jembi.jempi.libmpi.dgraph;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jembi.jempi.shared.models.InteractionWithScore;
import org.jembi.jempi.shared.models.CustomDemographicData;
import org.jembi.jempi.shared.models.Interaction;

@JsonInclude(JsonInclude.Include.NON_NULL)
record CustomDgraphInteraction(
      @JsonProperty("uid") String interactionId,
      @JsonProperty("Interaction.source_id") DgraphSourceId sourceId,
      @JsonProperty(CustomDgraphConstants.PREDICATE_INTERACTION_AUX_ID) String auxId,
      @JsonProperty(CustomDgraphConstants.PREDICATE_INTERACTION_GIVEN_NAME) String givenName,
      @JsonProperty(CustomDgraphConstants.PREDICATE_INTERACTION_FAMILY_NAME) String familyName,
      @JsonProperty(CustomDgraphConstants.PREDICATE_INTERACTION_GENDER) String gender,
      @JsonProperty(CustomDgraphConstants.PREDICATE_INTERACTION_DOB) String dob,
      @JsonProperty(CustomDgraphConstants.PREDICATE_INTERACTION_CITY) String city,
      @JsonProperty(CustomDgraphConstants.PREDICATE_INTERACTION_PHONE_NUMBER) String phoneNumber,
      @JsonProperty(CustomDgraphConstants.PREDICATE_INTERACTION_NATIONAL_ID) String nationalId,
      @JsonProperty("GoldenRecord.patients|score") Float score) {
   CustomDgraphInteraction(
         final Interaction interaction,
         final Float score) {
      this(interaction.interactionId(),
           new DgraphSourceId(interaction.sourceId()),
           interaction.demographicData().auxId,
           interaction.demographicData().givenName,
           interaction.demographicData().familyName,
           interaction.demographicData().gender,
           interaction.demographicData().dob,
           interaction.demographicData().city,
           interaction.demographicData().phoneNumber,
           interaction.demographicData().nationalId,
           score);
   }

   Interaction toInteraction() {
      return new Interaction(this.interactionId(),
                             this.sourceId() != null
                                   ? this.sourceId().toSourceId()
                                   : null,
                             new CustomDemographicData(this.auxId,
                                                         this.givenName,
                                                         this.familyName,
                                                         this.gender,
                                                         this.dob,
                                                         this.city,
                                                         this.phoneNumber,
                                                         this.nationalId));
   }

   InteractionWithScore toInteractionWithScore() {
      return new InteractionWithScore(toInteraction(), this.score());
   }

}
