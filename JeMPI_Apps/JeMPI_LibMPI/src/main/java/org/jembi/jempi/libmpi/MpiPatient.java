package org.jembi.jempi.libmpi;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.jembi.jempi.shared.models.CustomPatient;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MpiPatient(
      CustomPatient patient,
      Float score) {}

