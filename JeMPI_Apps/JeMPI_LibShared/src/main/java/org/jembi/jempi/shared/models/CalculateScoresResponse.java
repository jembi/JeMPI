package org.jembi.jempi.shared.models;

import java.util.List;

public record CalculateScoresResponse(
      String interactionId,
      List<Score> scores) {

   public record Score(
         String goldenId,
         float score) {
   }

}
