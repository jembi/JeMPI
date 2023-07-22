package org.jembi.jempi.libmpi.dgraph;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jembi.jempi.shared.models.LibMPIInteractionCount;
import org.jembi.jempi.shared.models.LibMPIPagination;

import java.util.List;
@JsonInclude(JsonInclude.Include.NON_NULL)
public record DgraphPaginationUidListWithInteractionCount(@JsonProperty("all") List<DgraphUid> all,
                                                          @JsonProperty("pagination") List<LibMPIPagination> pagination,
                                                          @JsonProperty("interactionCount") List<LibMPIInteractionCount> interactionCount)  {
   DgraphPaginationUidListWithInteractionCount(@JsonProperty("all") final List<DgraphUid> all, @JsonProperty("interactionCount") final List<LibMPIInteractionCount> interactionCount) {
      this(all, List.of(new LibMPIPagination(all.size())), interactionCount);
   }
}
