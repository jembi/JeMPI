package org.jembi.jempi.libmpi.dgraph;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jembi.jempi.shared.models.CustomSourceId;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record DgraphSourceId(
      @JsonProperty("uid") String uid,
      @JsonProperty("SourceId.facility") String facility,
      @JsonProperty("SourceId.patient") String patient) {
   DgraphSourceId(final CustomSourceId sourceId) {
      this(sourceId.uid(), sourceId.facility(), sourceId.patient());
   }

   public CustomSourceId toSourceId() {
      return new CustomSourceId(uid, facility, patient);
   }
}
