package org.jembi.jempi.libmpi.dgraph;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
record LibMPIPatientScore(
      String goldenUID,
      String patientUID,
      float score) {}

