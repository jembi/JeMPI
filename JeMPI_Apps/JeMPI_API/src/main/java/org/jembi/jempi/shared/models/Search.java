package org.jembi.jempi.shared.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record Search (
        List<SearchParameters> parameters) {
        public record SearchParameters(
            String value,
            String field,
            String distance){

    }

}
