package org.jembi.jempi.libmpi.dgraph;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jembi.jempi.shared.models.InteractionWithScore;
import org.jembi.jempi.shared.models.CustomDemographicData;
import org.jembi.jempi.shared.models.Interaction;

@JsonInclude(JsonInclude.Include.NON_NULL)
record CustomDgraphInteraction(
      @JsonProperty("uid") String patientId,
      @JsonProperty("PatientRecord.source_id") DgraphSourceId sourceId,
      @JsonProperty("PatientRecord.aux_id") String auxId,
      @JsonProperty("PatientRecord.given_name") String givenName,
      @JsonProperty("PatientRecord.family_name") String familyName,
      @JsonProperty("PatientRecord.gender") String gender,
      @JsonProperty("PatientRecord.dob") String dob,
      @JsonProperty("PatientRecord.city") String city,
      @JsonProperty("PatientRecord.phone_number") String phoneNumber,
      @JsonProperty("PatientRecord.national_id") String nationalId,
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

   InteractionWithScore toPatientRecordWithScore() {
      return new InteractionWithScore(toInteraction(), this.score());
   }

}
