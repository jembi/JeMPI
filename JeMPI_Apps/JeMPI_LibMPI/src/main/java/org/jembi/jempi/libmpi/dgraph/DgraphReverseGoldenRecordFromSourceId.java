package org.jembi.jempi.libmpi.dgraph;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jembi.jempi.shared.models.CustomSourceId;
import org.jembi.jempi.shared.models.ExpandedSourceId;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record DgraphReverseGoldenRecordFromSourceId(
      @JsonProperty("uid") String uid,
      @JsonProperty("SourceId.facility") String facility,
      @JsonProperty("SourceId.patient") String patient,
      @JsonProperty("~GoldenRecord.source_id") List<CustomDgraphGoldenRecord> goldenRecordList) {

   public ExpandedSourceId toExpandedSourceId() {
      return new ExpandedSourceId(new CustomSourceId(uid, facility, patient), goldenRecordList.stream().map(
            CustomDgraphGoldenRecord::toGoldenRecord).toList());
   }

}
