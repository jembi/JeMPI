package org.jembi.jempi.shared.models;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CustomUniqueInteractionData(java.time.LocalDateTime auxDateCreated,
                                          String auxId,
                                          String auxClinicalData) {
}
