package org.jembi.jempi.shared.models;

import java.util.List;

public record PaginatedGIDsWithInteractionCount(
      List<String> data,
      LibMPIPagination pagination,
      LibMPIInteractionCount interactionCount) {
}
