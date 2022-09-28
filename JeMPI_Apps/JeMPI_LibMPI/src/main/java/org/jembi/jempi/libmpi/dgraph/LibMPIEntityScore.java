package org.jembi.jempi.libmpi.dgraph;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
record LibMPIEntityScore(String goldenUid,
                         String entityUid,
                         float score) {}

