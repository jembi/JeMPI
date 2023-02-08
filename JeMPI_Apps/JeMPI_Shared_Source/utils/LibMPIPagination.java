package org.jembi.jempi.shared.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record LibMPIPagination(@JsonProperty("total") Integer total) {}
