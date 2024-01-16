package org.jembi.jempi.shared.models.dashboard;

import org.jembi.jempi.shared.libs.interactionProcessor.processors.tptn.TPTNMatrix;

public record TPTNStats(TPTNMatrix tptnMatrix, TPTNFScore tptnfScore) {
}
