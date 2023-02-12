package org.jembi.jempi.libmpi;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.jembi.jempi.shared.models.GoldenRecord;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MpiGoldenRecord(
      GoldenRecord goldenRecord,
      Float score) {}

