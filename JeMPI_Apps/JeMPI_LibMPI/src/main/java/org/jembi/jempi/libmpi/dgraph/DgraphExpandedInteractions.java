package org.jembi.jempi.libmpi.dgraph;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jembi.jempi.shared.models.LibMPIPagination;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
record DgraphExpandedInteractions(
      @JsonProperty("all") List<CustomDgraphExpandedInteraction> all,
      @JsonProperty("pagination") List<LibMPIPagination> pagination) {

   DgraphExpandedInteractions(@JsonProperty("all") final List<CustomDgraphExpandedInteraction> all) {
      this(all, List.of(new LibMPIPagination(all.size())));
   }

}
