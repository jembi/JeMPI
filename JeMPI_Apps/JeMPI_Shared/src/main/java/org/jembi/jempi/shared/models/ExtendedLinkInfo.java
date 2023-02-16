package org.jembi.jempi.shared.models;

import java.util.List;

public record ExtendedLinkInfo(
      String stan,
      LinkInfo linkInfo,
      List<ExternalLinkCandidate> externalLinkCandidateList) {
}
