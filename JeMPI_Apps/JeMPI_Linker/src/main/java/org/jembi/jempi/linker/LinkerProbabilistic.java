package org.jembi.jempi.linker;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.apache.commons.text.similarity.SimilarityScore;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.shared.models.CustomMU;
import org.jembi.jempi.shared.utils.AppUtils;

import java.util.List;
import java.util.stream.IntStream;

import static java.lang.Math.abs;
import static java.lang.Math.log;

final class LinkerProbabilistic {

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
      final var score = field.similarityScore.apply(left, right);
      for (int i = 0; i < field.weights.size(); i++) {
         if (score >= field.comparisonLevels.get(i)) {
            return fieldScore(i <= field.comparisonLevels.size() / 2, field.m, field.u) * field.weights.get(i);
         }
      }
      return fieldScore(false, field.m, field.u);
   }

   static CustomMU.Probability getProbability(final Field field) {
      return new CustomMU.Probability(field.m(), field.u());
   }

   static void checkUpdatedMU() {
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
         List<Float> weights,
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
            final SimilarityScore<Double> func_,
            final List<Float> comparisonLevels_,
            final float m_,
            final float u_) {

         this(func_, comparisonLevels_, computeWeights(comparisonLevels_.size()), m_, u_, 0.0F, 0.0F);
      }

      private static List<Float> computeWeights(final int n) {
         if (n % 2 == 0) {
            final var k = n / 2;
            final var z = 1.0F / k;
            final var w = IntStream.range(0, n)
                                   .mapToDouble(i -> abs(1.0 - (z * i)))
                                   .boxed()
                                   .map(Double::floatValue)
                                   .toList();
            if (LOGGER.isDebugEnabled()) {
               try {
                  LOGGER.debug("{}", AppUtils.OBJECT_MAPPER.writeValueAsString(w));
               } catch (JsonProcessingException e) {
                  LOGGER.error(e.getLocalizedMessage(), e);
               }
            }
            return w;
         } else {
            final var k = (n + 1) / 2;
            final var z = 1.0F / k;
            final var w = IntStream.range(0, n)
                                   .mapToDouble(i -> abs(1.0 - (z * (i < k
                                                                           ? i
                                                                           : i + 1))))
                                   .boxed()
                                   .map(Double::floatValue)
                                   .toList();
            if (LOGGER.isDebugEnabled()) {
               try {
                  LOGGER.debug("{}", AppUtils.OBJECT_MAPPER.writeValueAsString(w));
               } catch (JsonProcessingException e) {
                  LOGGER.error(e.getLocalizedMessage(), e);
               }
            }
            return w;
         }
      }

   }

}
