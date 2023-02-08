package org.jembi.jempi.shared.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.commons.lang3.ObjectUtils;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SimpleSearchRequestPayload(List<SearchParameter> parameters,
                                         Integer offset,
                                         Integer limit,
                                         String sortBy,
                                         Boolean sortAsc) {

    public SimpleSearchRequestPayload(List<SearchParameter> parameters, Integer offset, Integer limit, String sortBy, Boolean sortAsc) {
        this.parameters = ObjectUtils.defaultIfNull(parameters, new ArrayList<>());
        this.offset = ObjectUtils.defaultIfNull(offset, 0);
        this.limit = ObjectUtils.defaultIfNull(limit, 10);
        this.sortBy = ObjectUtils.defaultIfNull(sortBy, "uid");
        this.sortAsc = ObjectUtils.defaultIfNull(sortAsc, false);
    }

    public record SearchParameter(
            String value,
            String fieldName,
            Integer distance) {

    }
}
