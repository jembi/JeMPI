package org.jembi.jempi.shared.utils;

import java.util.List;

public record LibMPIPaginatedResultSet<T extends Object>(List<T> data, LibMPIPagination pagination) {
}
