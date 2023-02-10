package org.jembi.jempi.libmpi.dgraph;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jembi.jempi.shared.utils.LibMPIPagination;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
record LibMPIExpandedPatients(@JsonProperty("all") List<CustomLibMPIExpandedPatient> all,
                              @JsonProperty("pagination") List<LibMPIPagination> pagination) {

   public LibMPIExpandedPatients(@JsonProperty("all") List<CustomLibMPIExpandedPatient> all) {
      this(all, List.of(new LibMPIPagination(all.size())));
   }

}
