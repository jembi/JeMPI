package org.jembi.jempi.libmpi;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.jembi.jempi.shared.models.CustomGoldenRecord;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MpiGoldenRecord(
      CustomGoldenRecord goldenRecord,
      Float score) {}

