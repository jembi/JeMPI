package org.jembi.jempi.shared.config.input;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record Generate(
    String func,
    String interactionField) {

    public Generate(final String func) {
        this(func, null);
    }
}
