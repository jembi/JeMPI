package org.jembi.jempi.libmpi.dgraph;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jembi.jempi.shared.models.LibMPIPagination;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
record DgraphExpandedGoldenRecords(
      @JsonProperty("all") List<CustomDgraphExpandedGoldenRecord> all,
      @JsonProperty("pagination") List<LibMPIPagination> pagination) {

   DgraphExpandedGoldenRecords(@JsonProperty("all") final List<CustomDgraphExpandedGoldenRecord> all) {
      this(all, List.of(new LibMPIPagination(all.size())));
   }

}
