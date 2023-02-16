package org.jembi.jempi.libmpi.dgraph;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jembi.jempi.shared.utils.LibMPIPagination;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
record LibMPIExpandedGoldenRecords(
      @JsonProperty("all") List<CustomLibMPIExpandedGoldenRecord> all,
      @JsonProperty("pagination") List<LibMPIPagination> pagination) {

   LibMPIExpandedGoldenRecords(@JsonProperty("all") final List<CustomLibMPIExpandedGoldenRecord> all) {
      this(all, List.of(new LibMPIPagination(all.size())));
   }

}
