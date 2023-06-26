package org.jembi.jempi.libmpi.dgraph;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jembi.jempi.shared.models.LibMPIPagination;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record DgraphPaginatedUidList(@JsonProperty("all") List<DgraphUid> all,
                                     @JsonProperty("pagination") List<LibMPIPagination> pagination) {
   DgraphPaginatedUidList(@JsonProperty("all") final List<DgraphUid> all) {
      this(all, List.of(new LibMPIPagination(all.size())));
   }
}
