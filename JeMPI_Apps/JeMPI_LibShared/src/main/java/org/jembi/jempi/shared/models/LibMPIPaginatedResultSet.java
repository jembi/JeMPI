package org.jembi.jempi.shared.models;

import java.util.List;

public record LibMPIPaginatedResultSet<T>(
      List<T> data,
      LibMPIPagination pagination) {
}
