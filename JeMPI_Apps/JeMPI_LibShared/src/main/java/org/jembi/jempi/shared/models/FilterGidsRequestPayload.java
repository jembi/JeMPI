package org.jembi.jempi.shared.models;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ObjectUtils;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record FilterGidsRequestPayload(
        List<SearchParameter> parameters,
        LocalDate createdAt,
        Integer offset,
        Integer limit,
        String sortBy,
        Boolean sortAsc) {

    public FilterGidsRequestPayload(
            final List<SearchParameter> parameters,
            final LocalDate createdAt,
            final Integer offset,
            final Integer limit,
            final String sortBy,
            final Boolean sortAsc) {
        this.parameters = ObjectUtils.defaultIfNull(parameters, new ArrayList<>());
        this.createdAt = ObjectUtils.defaultIfNull(createdAt, LocalDate.now());
        this.offset = ObjectUtils.defaultIfNull(offset, 0);
        this.limit = ObjectUtils.defaultIfNull(limit, 10);
        this.sortBy = ObjectUtils.defaultIfNull(sortBy, "uid");
        this.sortAsc = ObjectUtils.defaultIfNull(sortAsc, false);
    }
}