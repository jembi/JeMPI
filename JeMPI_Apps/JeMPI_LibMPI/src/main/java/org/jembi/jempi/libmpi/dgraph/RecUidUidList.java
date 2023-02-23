package org.jembi.jempi.libmpi.dgraph;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
record RecUidUidList(@JsonProperty("list") List<RecUidList> list) {
   record RecUidList(
         @JsonProperty("uid") String uid,
         @JsonProperty("list") List<LibMPIUid> list) {}
}
