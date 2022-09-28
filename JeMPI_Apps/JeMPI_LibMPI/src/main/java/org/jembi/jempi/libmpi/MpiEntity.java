package org.jembi.jempi.libmpi;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.jembi.jempi.shared.models.CustomEntity;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MpiEntity(CustomEntity entity,
                        Float score) {}

