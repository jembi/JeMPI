package org.jembi.jempi.libmpi.dgraph;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jembi.jempi.shared.models.SourceId;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record DgraphSourceId(
      @JsonProperty("uid") String uid,
      @JsonProperty("SourceId.facility") String facility,
      @JsonProperty("SourceId.patient") String patient) {
   DgraphSourceId(final SourceId sourceId) {
      this(sourceId.uid(), sourceId.facility(), sourceId.patient());
   }

   SourceId toSourceId() {
      return new SourceId(uid, facility, patient);
   }
}
