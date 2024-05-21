package org.jembi.jempi.shared.models;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ExpandedSourceId(
      CustomSourceId sourceId,
      List<GoldenRecord> goldenRecords) {
}
