package org.jembi.jempi.libmpi.dgraph;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jembi.jempi.shared.utils.LibMPIPagination;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
record DgraphPatientRecords(
      @JsonProperty("all") List<CustomDgraphPatientRecord> all,
      @JsonProperty("pagination") List<LibMPIPagination> pagination) {
   DgraphPatientRecords(@JsonProperty("all") final List<CustomDgraphPatientRecord> all_) {
      this(all_, List.of(new LibMPIPagination(all_.size())));
   }
}
