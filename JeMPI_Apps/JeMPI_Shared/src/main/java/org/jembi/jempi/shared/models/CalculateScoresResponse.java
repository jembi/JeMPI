package org.jembi.jempi.shared.models;

import java.util.List;

public record CalculateScoresResponse(
      String patientUid,
      List<Score> scores) {

   public record Score(
         String goldenUid,
         float score) {
   }

}
