package org.jembi.jempi.libmpi.dgraph;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jembi.jempi.shared.models.InteractionWithScore;
import org.jembi.jempi.shared.models.CustomUniqueInteractionData;
import org.jembi.jempi.shared.models.CustomDemographicData;
import org.jembi.jempi.shared.models.Interaction;

@JsonInclude(JsonInclude.Include.NON_NULL)
record CustomDgraphInteraction(
      @JsonProperty("uid") String interactionId,
      @JsonProperty("Interaction.source_id") DgraphSourceId sourceId,
      @JsonProperty(CustomDgraphConstants.PREDICATE_INTERACTION_AUX_DATE_CREATED) java.time.LocalDateTime auxDateCreated,
      @JsonProperty(CustomDgraphConstants.PREDICATE_INTERACTION_AUX_ID) String auxId,
      @JsonProperty(CustomDgraphConstants.PREDICATE_INTERACTION_AUX_CLINICAL_DATA) String auxClinicalData,
      @JsonProperty(CustomDgraphConstants.PREDICATE_INTERACTION_GIVEN_NAME) String givenName,
      @JsonProperty(CustomDgraphConstants.PREDICATE_INTERACTION_FAMILY_NAME) String familyName,
      @JsonProperty(CustomDgraphConstants.PREDICATE_INTERACTION_GENDER) String gender,
      @JsonProperty(CustomDgraphConstants.PREDICATE_INTERACTION_DOB) String dob,
      @JsonProperty(CustomDgraphConstants.PREDICATE_INTERACTION_CITY) String city,
      @JsonProperty(CustomDgraphConstants.PREDICATE_INTERACTION_PHONE_NUMBER_HOME) String phoneNumberHome,
      @JsonProperty(CustomDgraphConstants.PREDICATE_INTERACTION_PHONE_NUMBER_MOBILE) String phoneNumberMobile,
      @JsonProperty(CustomDgraphConstants.PREDICATE_INTERACTION_PHN) String phn,
      @JsonProperty(CustomDgraphConstants.PREDICATE_INTERACTION_NIC) String nic,
      @JsonProperty(CustomDgraphConstants.PREDICATE_INTERACTION_PPN) String ppn,
      @JsonProperty(CustomDgraphConstants.PREDICATE_INTERACTION_SCN) String scn,
      @JsonProperty(CustomDgraphConstants.PREDICATE_INTERACTION_DL) String dl,
      @JsonProperty("GoldenRecord.interactions|score") Float score) {

   CustomDgraphInteraction(
         final Interaction interaction,
         final Float score) {
      this(interaction.interactionId(),
           new DgraphSourceId(interaction.sourceId()),
           interaction.uniqueInteractionData().auxDateCreated(),
           interaction.uniqueInteractionData().auxId(),
           interaction.uniqueInteractionData().auxClinicalData(),
           interaction.demographicData().givenName,
           interaction.demographicData().familyName,
           interaction.demographicData().gender,
           interaction.demographicData().dob,
           interaction.demographicData().city,
           interaction.demographicData().phoneNumberHome,
           interaction.demographicData().phoneNumberMobile,
           interaction.demographicData().phn,
           interaction.demographicData().nic,
           interaction.demographicData().ppn,
           interaction.demographicData().scn,
           interaction.demographicData().dl,
           score);
   }

   Interaction toInteraction() {
      return new Interaction(this.interactionId(),
                             this.sourceId() != null
                                   ? this.sourceId().toSourceId()
                                   : null,
                             new CustomUniqueInteractionData(this.auxDateCreated,
                                                               this.auxId,
                                                               this.auxClinicalData),
                             new CustomDemographicData(this.givenName,
                                                       this.familyName,
                                                       this.gender,
                                                       this.dob,
                                                       this.city,
                                                       this.phoneNumberHome,
                                                       this.phoneNumberMobile,
                                                       this.phn,
                                                       this.nic,
                                                       this.ppn,
                                                       this.scn,
                                                       this.dl));
   }

   InteractionWithScore toInteractionWithScore() {
      return new InteractionWithScore(toInteraction(), this.score());
   }

}

