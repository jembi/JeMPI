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
      @JsonProperty("PatientRecord.source_id") DgraphSourceId sourceId,
      @JsonProperty("PatientRecord.aux_id") String auxId,
      @JsonProperty("PatientRecord.given_name") String givenName,
      @JsonProperty("PatientRecord.family_name") String familyName,
      @JsonProperty("PatientRecord.gender") String gender,
      @JsonProperty("PatientRecord.dob") String dob,
      @JsonProperty("PatientRecord.city") String city,
      @JsonProperty("PatientRecord.phone_number") String phoneNumber,
      @JsonProperty("PatientRecord.national_id") String nationalId,
      @JsonProperty("~GoldenRecord.patients") List<CustomDgraphReverseGoldenRecord> dgraphGoldenRecordList) {

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
