
package org.jembi.jempi.shared.models;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CustomSourceId(
      String uid,
      String facility,
      String patient,
      String aux_clinical_data) {
}

