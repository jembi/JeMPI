package org.jembi.jempi.shared.models;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UploadConfig(
    Boolean reporting,
    Integer computing,
    Double leftMargin,
    Double threshold,
    Double rightMargin,
    Double windowSize) {
}
