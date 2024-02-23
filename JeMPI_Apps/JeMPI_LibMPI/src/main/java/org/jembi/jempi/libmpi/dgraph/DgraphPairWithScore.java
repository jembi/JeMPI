package org.jembi.jempi.libmpi.dgraph;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
record DgraphPairWithScore(
      String goldenUID,
      String interactionUID,
      float score) {
}

