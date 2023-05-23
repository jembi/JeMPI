package org.jembi.jempi.libmpi.dgraph;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jembi.jempi.shared.models.InteractionWithScore;
import org.jembi.jempi.shared.models.CustomDemographicData;
import org.jembi.jempi.shared.models.Interaction;

@JsonInclude(JsonInclude.Include.NON_NULL)
record CustomDgraphInteraction(
      @JsonProperty("uid") String patientId,
      @JsonProperty("Interaction.source_id") DgraphSourceId sourceId,
      @JsonProperty("Interaction.aux_id") String auxId,
      @JsonProperty("Interaction.given_name") String givenName,
      @JsonProperty("Interaction.family_name") String familyName,
      @JsonProperty("Interaction.gender") String gender,
      @JsonProperty("Interaction.dob") String dob,
      @JsonProperty("Interaction.city") String city,
      @JsonProperty("Interaction.phone_number") String phoneNumber,
      @JsonProperty("Interaction.national_id") String nationalId,
      @JsonProperty("GoldenRecord.patients|score") Float score) {
   CustomDgraphInteraction(
         final Interaction interaction,
         final Float score) {
      this(interaction.patientId(),
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
      return new Interaction(this.patientId(),
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
