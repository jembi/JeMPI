package org.jembi.jempi.linker;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.apache.commons.text.similarity.SimilarityScore;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.shared.models.CustomMU;

import java.util.List;

import static java.lang.Math.log;

public final class LinkerProbabilistic {

   static final JaroWinklerSimilarity JARO_WINKLER_SIMILARITY = new JaroWinklerSimilarity();
   static final ExactSimilarity EXACT_SIMILARITY = new ExactSimilarity();
   private static final Logger LOGGER = LogManager.getLogger(LinkerProbabilistic.class);
   private static final double LOG2 = java.lang.Math.log(2.0);
   private static final float MISSING_PENALTY = 0.925F;

   private LinkerProbabilistic() {
   }

   private static float limitProbability(final float p) {
      if (p > 1.0F - 1E-5F) {
         return 1.0F - 1E-5F;
      } else if (p < 1E-5F) {
         return 1E-5F;
      }
      return p;
   }

   private static float fieldScore(
         final boolean match,
         final float m,
         final float u) {
      if (match) {
         return (float) (log(m / u) / LOG2);
      }
      return (float) (log((1.0 - m) / (1.0 - u)) / LOG2);
   }

   private static float fieldScore(
         final String left,
         final String right,
         final Field field) {
      return fieldScore(field.similarityScore.apply(left, right) > field.comparisonLevels.get(0), field.m, field.u);
   }

   static CustomMU.Probability getProbability(final Field field) {
      return new CustomMU.Probability(field.m(), field.u());
   }

   public static void checkUpdatedMU() {
      if (CustomLinkerProbabilistic.updatedFields != null) {
         LOGGER.info("Using updated MU values: {}", CustomLinkerProbabilistic.updatedFields);
         CustomLinkerProbabilistic.currentFields = CustomLinkerProbabilistic.updatedFields;
         CustomLinkerProbabilistic.updatedFields = null;
      }
   }

   static void updateMetricsForStringField(
         final float[] metrics,
         final String left,
         final String right,
         final Field field) {
      if (StringUtils.isNotBlank(left) && StringUtils.isNotBlank(right)) {
         metrics[0] += field.min;
         metrics[1] += field.max;
         metrics[2] += fieldScore(left, right, field);
      } else {
         metrics[3] *= MISSING_PENALTY;
      }
   }

   static class ExactSimilarity implements SimilarityScore<Double> {

      @Override
      public Double apply(
            final CharSequence left,
            final CharSequence right) {
         if (left == null || right == null) {
            return 0.5;
         }
         return left.equals(right)
               ? 1.0
               : 0.0;
      }

   }

   record Field(
         SimilarityScore<Double> similarityScore,
         List<Float> comparisonLevels,
         float m,
         float u,
         float min,
         float max) {
      Field {
         m = limitProbability(m);
         u = limitProbability(u);
         min = fieldScore(false, m, u);
         max = fieldScore(true, m, u);
      }

      Field(
            final SimilarityScore<Double> fn,
            final List<Float> comparisonLevels,
            final float m_,
            final float u_) {
         this(fn, comparisonLevels, m_, u_, 0.0F, 0.0F);
      }

   }

}
