package org.jembi.jempi.libmpi.common;

import org.jembi.jempi.shared.models.LibMPIPagination;

import java.util.List;

public record PaginatedResultSet<T>(
      List<T> data,
      List<LibMPIPagination> pagination) {
}
