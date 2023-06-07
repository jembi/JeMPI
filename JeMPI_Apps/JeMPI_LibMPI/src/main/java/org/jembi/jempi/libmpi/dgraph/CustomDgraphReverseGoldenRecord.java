package org.jembi.jempi.libmpi.dgraph;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jembi.jempi.shared.models.CustomUniqueGoldenRecordData;
import org.jembi.jempi.shared.models.GoldenRecordWithScore;
import org.jembi.jempi.shared.models.CustomDemographicData;
import org.jembi.jempi.shared.models.GoldenRecord;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
record CustomDgraphReverseGoldenRecord(
      @JsonProperty("uid") String goldenId,
      @JsonProperty("GoldenRecord.source_id") List<DgraphSourceId> sourceId,
      @JsonProperty("GoldenRecord.aux_id") String auxId,
      @JsonProperty("GoldenRecord.aux_auto_update_enabled") String auxAutoUpdateEnabled,
      @JsonProperty("GoldenRecord.given_name") String givenName,
      @JsonProperty("GoldenRecord.family_name") String familyName,
      @JsonProperty("GoldenRecord.gender") String gender,
      @JsonProperty("GoldenRecord.dob") String dob,
      @JsonProperty("GoldenRecord.city") String city,
      @JsonProperty("GoldenRecord.phone_number") String phoneNumber,
      @JsonProperty("GoldenRecord.national_id") String nationalId,
      @JsonProperty("~GoldenRecord.interactions|score") Float score) {

   GoldenRecord toGoldenRecord() {
      return new GoldenRecord(this.goldenId(),
                              this.sourceId() != null
                                    ? this.sourceId().stream().map(DgraphSourceId::toSourceId).toList()
                                    : List.of(),
                              new CustomDemographicData(this.auxId(),
                                                        this.givenName(),
                                                        this.familyName(),
                                                        this.gender(),
                                                        this.dob(),
                                                        this.city(),
                                                        this.phoneNumber(),
                                                        this.nationalId()), new CustomUniqueGoldenRecordData(true));
   }

   GoldenRecordWithScore toGoldenRecordWithScore() {
      return new GoldenRecordWithScore(toGoldenRecord(), score);
   }

}
