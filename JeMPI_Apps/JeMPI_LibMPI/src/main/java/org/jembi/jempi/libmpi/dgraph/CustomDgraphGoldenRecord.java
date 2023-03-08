package org.jembi.jempi.libmpi.dgraph;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jembi.jempi.shared.models.CustomDemographicData;
import org.jembi.jempi.shared.models.GoldenRecord;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
record CustomDgraphGoldenRecord(
      @JsonProperty("uid") String goldenId,
      @JsonProperty("GoldenRecord.source_id") List<DgraphSourceId> sourceId,
      @JsonProperty("GoldenRecord.aux_id") String auxId,
      @JsonProperty("GoldenRecord.fpid") String fpid,
      @JsonProperty("GoldenRecord.gender") String gender,
      @JsonProperty("GoldenRecord.dob") String dob) {

   CustomDgraphGoldenRecord(final CustomDgraphPatientRecord rec) {
      this(null,
           List.of(rec.sourceId()),
           rec.auxId(),
           rec.fpid(),
           rec.gender(),
           rec.dob());
   }

   GoldenRecord toGoldenRecord() {
      return new GoldenRecord(this.goldenId(),
                              this.sourceId() != null
                                    ? this.sourceId().stream().map(DgraphSourceId::toSourceId).toList()
                                    : List.of(),
                              new CustomDemographicData(this.auxId(),
                                                        this.fpid(),
                                                        this.gender(),
                                                        this.dob()));
   }

}
