package org.jembi.jempi.shared.models;

import java.util.List;

public record CalculateScoresRequest(String interactionId,
                                     List<String> goldenIds) {
}
