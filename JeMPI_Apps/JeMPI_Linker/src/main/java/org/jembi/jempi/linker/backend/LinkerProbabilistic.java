package org.jembi.jempi.linker.backend;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.codec.language.Soundex;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.similarity.JaccardSimilarity;
import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.apache.commons.text.similarity.SimilarityScore;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.shared.config.LinkerConfig;
import org.jembi.jempi.shared.models.DemographicData;
import org.jembi.jempi.shared.models.MUPacket;
import org.jembi.jempi.shared.utils.AppUtils;
import org.apache.commons.codec.language.DoubleMetaphone;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import static java.lang.Math.abs;
import static java.lang.Math.log;
import static org.jembi.jempi.shared.config.Config.LINKER_CONFIG;
import static org.jembi.jempi.shared.utils.AppUtils.OBJECT_MAPPER;

public final class LinkerProbabilistic {
   static final JaroWinklerSimilarity JARO_WINKLER_SIMILARITY = new JaroWinklerSimilarity();
   static final JaccardSimilarity JACCARD_SIMILARITY = new JaccardSimilarity();
   static final JaroSimilarity JARO_SIMILARITY = new JaroSimilarity();
   static final ExactSimilarity EXACT_SIMILARITY = new ExactSimilarity();
   static final SoundexSimilarity SOUNDEX_SIMILARITY = new SoundexSimilarity();
   static final DoubleMetaphoneSimilarity DOUBLE_METAPHONE_SIMILARITY = new DoubleMetaphoneSimilarity();
   private static final int METRIC_MIN = 0;
   private static final int METRIC_MAX = 1;
   private static final int METRIC_SCORE = 2;
   private static final int METRIC_MISSING_PENALTY = 3;
   private static final Logger LOGGER = LogManager.getLogger(LinkerProbabilistic.class);
   private static final double LOG2 = java.lang.Math.log(2.0);
   private static final float MISSING_PENALTY = 0.925F;
   static List<ProbabilisticField> currentProbabilisticLinkFields = LINKER_CONFIG.probabilisticLinkFields
         .stream()
         .map(f -> new ProbabilisticField(getSimilarityFunction(SimilarityFunctionName.valueOf(f.similarityScore())), f.comparisonLevels(), f.m(), f.u()))
         .toList();
   static List<ProbabilisticField> currentProbabilisticValidateFields = LINKER_CONFIG.probabilisticValidateFields
         .stream()
         .map(f -> new ProbabilisticField(getSimilarityFunction(SimilarityFunctionName.valueOf(f.similarityScore())), f.comparisonLevels(), f.m(), f.u()))
         .toList();
   static List<ProbabilisticField> currentProbabilisticMatchFields = LINKER_CONFIG.probabilisticMatchNotificationFields
         .stream()
         .map(f -> new ProbabilisticField(getSimilarityFunction(SimilarityFunctionName.valueOf(f.similarityScore())), f.comparisonLevels(), f.m(), f.u()))
         .toList();

   static List<ProbabilisticField> updatedProbabilisticLinkFields = null;
   static List<ProbabilisticField> updatedProbabilisticValidateFields = null;
   static List<ProbabilisticField> updatedProbabilisticMatchFields = null;

   private LinkerProbabilistic() {
   }

   static List<ProbabilisticField> toLinkProbabilisticFieldList(
         final List<LinkerConfig.FieldProbabilisticMetaData> probabilisticMetaData,
         final List<MUPacket.Probability> mu) {
      for (MUPacket.Probability probability : mu) {
         if (probability.u() >= probability.m()) {
            return Collections.emptyList();
         }
      }
      final var list = new ArrayList<ProbabilisticField>();
      for (int i = 0; i < mu.size(); i++) {
         list.add(new ProbabilisticField(
               getSimilarityFunction(SimilarityFunctionName.valueOf(probabilisticMetaData.get(i).similarityScore())),
               probabilisticMetaData.get(i).comparisonLevels(),
               mu.get(i).m(), mu.get(i).u()));
      }
      return list;
   }

   public enum SimilarityFunctionName {
      JARO_WINKLER_SIMILARITY,
      JARO_SIMILARITY,
      JACCARD_SIMILARITY,
      SOUNDEX_SIMILARITY,
      EXACT_SIMILARITY,
      DOUBLE_METAPHONE_SIMILARITY
   }

   static SimilarityScore<Double> getSimilarityFunction(final SimilarityFunctionName func) {
      switch (func) {
         case JARO_WINKLER_SIMILARITY:
            return JARO_WINKLER_SIMILARITY;
         case JARO_SIMILARITY:
            return JARO_SIMILARITY;
         case JACCARD_SIMILARITY:
            return JACCARD_SIMILARITY;
         case SOUNDEX_SIMILARITY:
            return SOUNDEX_SIMILARITY;
         case DOUBLE_METAPHONE_SIMILARITY:
            return DOUBLE_METAPHONE_SIMILARITY;
         default:
            return EXACT_SIMILARITY;
      }
   }

   public static void updateMU(final MUPacket mu) {

      try {
         final var jsonL = OBJECT_MAPPER.writeValueAsString(mu.linkMuPacket());
         final var jsonV = OBJECT_MAPPER.writeValueAsString(mu.validateMuPacket());
         final var jsonM = OBJECT_MAPPER.writeValueAsString(mu.matchMuPacket());
         LOGGER.info("update link MU: {}", jsonL);
         LOGGER.info("update validate MU: {}", jsonV);
         LOGGER.info("update match MU: {}", jsonM);
      } catch (JsonProcessingException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
      }

      try {
         final List<ProbabilisticField> linkProbabilisticFieldList =
               (mu.linkMuPacket() != null && !AppUtils.isNullOrEmpty(mu.linkMuPacket()))
                     ? toLinkProbabilisticFieldList(LINKER_CONFIG.probabilisticLinkFields, mu.linkMuPacket())
                     : List.of();

         final List<ProbabilisticField> validateProbabilisticFieldList =
               (mu.validateMuPacket() != null && !AppUtils.isNullOrEmpty(mu.validateMuPacket()))
                     ? toLinkProbabilisticFieldList(LINKER_CONFIG.probabilisticValidateFields, mu.validateMuPacket())
                     : List.of();

         final List<ProbabilisticField> matchProbabilisticFieldList =
               (mu.matchMuPacket() != null && !AppUtils.isNullOrEmpty(mu.matchMuPacket()))
                     ? toLinkProbabilisticFieldList(LINKER_CONFIG.probabilisticMatchNotificationFields, mu.matchMuPacket())
                     : List.of();

         if (!AppUtils.isNullOrEmpty(linkProbabilisticFieldList)) {
            updatedProbabilisticLinkFields = linkProbabilisticFieldList;
         }
         if (!AppUtils.isNullOrEmpty(validateProbabilisticFieldList)) {
            updatedProbabilisticValidateFields = validateProbabilisticFieldList;
         }
         if (!AppUtils.isNullOrEmpty(matchProbabilisticFieldList)) {
            updatedProbabilisticMatchFields = matchProbabilisticFieldList;
         }
      } catch (Exception e) {
         LOGGER.error(e.getLocalizedMessage(), e);
      }
   }

   public static void checkUpdatedLinkMU() {
      if (updatedProbabilisticLinkFields != null) {
         currentProbabilisticLinkFields = updatedProbabilisticLinkFields;
         updatedProbabilisticLinkFields = null;
      }
      if (updatedProbabilisticValidateFields != null) {
         currentProbabilisticValidateFields = updatedProbabilisticValidateFields;
         updatedProbabilisticValidateFields = null;
      }
      if (updatedProbabilisticMatchFields != null) {
         currentProbabilisticMatchFields = updatedProbabilisticMatchFields;
         updatedProbabilisticMatchFields = null;
      }
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

   public static float fieldScore(
         final String left,
         final String right,
         final ProbabilisticField field) {
      final var score = field.similarityScore.apply(left, right);
      for (int i = 0; i < field.weights.size(); i++) {
         if (score >= field.comparisonLevels.get(i)) {
            return fieldScore(i <= field.comparisonLevels.size() / 2, field.m, field.u) * field.weights.get(i);
         }
      }
      return fieldScore(false, field.m, field.u);
   }

   public static FieldScoreInfo fieldScoreInfo(
         final String left,
         final String right,
         final ProbabilisticField field) {
      final var score = field.similarityScore.apply(left, right);
      for (int i = 0; i < field.weights.size(); i++) {
         if (score >= field.comparisonLevels.get(i)) {
            return new FieldScoreInfo(i <= field.comparisonLevels.size() / 2,
                                      fieldScore(i <= field.comparisonLevels.size() / 2,
                                                 field.m, field.u) * field.weights.get(i));
         }
      }
      return new FieldScoreInfo(false, fieldScore(false, field.m, field.u));

   }

   static float matchProbabilisticScore(
         final DemographicData goldenRecord,
         final DemographicData interaction) {
      // min, max, score, missingPenalty
      final float[] metrics = {0, 0, 0, 1.0F};
      for (int i = 0; i < LINKER_CONFIG.probabilisticMatchNotificationFields.size(); i++) {
         updateMetricsForStringField(metrics,
                                     goldenRecord.fields.get(LINKER_CONFIG.probabilisticMatchNotificationFields.get(i)
                                                                                                               .demographicDataIndex())
                                                        .value(),
                                     interaction.fields.get(LINKER_CONFIG.probabilisticMatchNotificationFields.get(i)
                                                                                                              .demographicDataIndex())
                                                       .value(),
                                     currentProbabilisticMatchFields.get(i));
      }
      return ((metrics[METRIC_SCORE] - metrics[METRIC_MIN]) / (metrics[METRIC_MAX] - metrics[METRIC_MIN])) * metrics[METRIC_MISSING_PENALTY];
   }

   static float validateProbabilisticScore(
         final DemographicData goldenRecord,
         final DemographicData interaction) {
      // min, max, score, missingPenalty
      final float[] metrics = {0, 0, 0, 1.0F};
      for (int i = 0; i < LINKER_CONFIG.probabilisticValidateFields.size(); i++) {
         updateMetricsForStringField(metrics,
                                     goldenRecord.fields.get(LINKER_CONFIG.probabilisticValidateFields.get(i)
                                                                                                      .demographicDataIndex())
                                                        .value(),
                                     interaction.fields.get(LINKER_CONFIG.probabilisticValidateFields.get(i)
                                                                                                     .demographicDataIndex())
                                                       .value(),
                                     currentProbabilisticValidateFields.get(i));
      }
      return ((metrics[METRIC_SCORE] - metrics[METRIC_MIN]) / (metrics[METRIC_MAX] - metrics[METRIC_MIN])) * metrics[METRIC_MISSING_PENALTY];
   }

   static float linkProbabilisticScore(
         final DemographicData goldenRecord,
         final DemographicData interaction) {
      // min, max, score, missingPenalty
      final float[] metrics = {0, 0, 0, 1.0F};
      for (int i = 0; i < LINKER_CONFIG.probabilisticLinkFields.size(); i++) {
         updateMetricsForStringField(metrics,
                                     goldenRecord.fields.get(LINKER_CONFIG.probabilisticLinkFields.get(i).demographicDataIndex())
                                                        .value(),
                                     interaction.fields.get(LINKER_CONFIG.probabilisticLinkFields.get(i).demographicDataIndex())
                                                       .value(),
                                     currentProbabilisticLinkFields.get(i));
      }
      return ((metrics[METRIC_SCORE] - metrics[METRIC_MIN]) / (metrics[METRIC_MAX] - metrics[METRIC_MIN])) * metrics[METRIC_MISSING_PENALTY];
   }

   public static MUPacket.Probability getProbability(final ProbabilisticField field) {
      return new MUPacket.Probability(field.m(), field.u());
   }

   public static void updateMetricsForStringField(
         final float[] metrics,
         final String left,
         final String right,
         final ProbabilisticField field) {
      metrics[METRIC_MIN] += field.min;
      metrics[METRIC_MAX] += field.max;
      if (StringUtils.isNotBlank(left) && StringUtils.isNotBlank(right)) {
         metrics[METRIC_SCORE] += fieldScore(left, right, field);
      }
   }

   public record FieldScoreInfo(
         Boolean isMatch,
         Float score) {
   }

   static class ExactSimilarity implements SimilarityScore<Double> {

      @Override
      public Double apply(
            final CharSequence left,
            final CharSequence right) {
         if (StringUtils.isEmpty(left) || StringUtils.isEmpty(right)) {
            return 0.5;
         }

         return StringUtils.equals(left, right)
               ? 1.0
               : 0.0;
      }

   }

   static class SoundexSimilarity implements SimilarityScore<Double> {

      private final Soundex soundex = new Soundex();

      @Override
      public Double apply(
            final CharSequence left,
            final CharSequence right) {
         if (StringUtils.isEmpty(left) || StringUtils.isEmpty(right)) {
            return 0.5;
         }

         return StringUtils.equals(soundex.soundex(left.toString()), soundex.soundex(right.toString()))
               ? 1.0
               : 0.0;
      }

   }

   static class JaroSimilarity implements SimilarityScore<Double> {

      @Override
      public Double apply(
            final CharSequence s,
            final CharSequence t) {

         // https://rosettacode.org/wiki/Jaro_similarity#Java

         int sLen = s.length();
         int tLen = t.length();

         if (sLen == 0 && tLen == 0) {
            return 1.0;
         }

         int matchDistance = Integer.max(sLen, tLen) / 2 - 1;

         boolean[] sMatches = new boolean[sLen];
         boolean[] tMatches = new boolean[tLen];

         int matches = 0;
         int transpositions = 0;

         for (int i = 0; i < sLen; i++) {
            int start = Integer.max(0, i - matchDistance);
            int end = Integer.min(i + matchDistance + 1, tLen);

            for (int j = start; j < end; j++) {
               if ((tMatches[j]) || (s.charAt(i) != t.charAt(j))) {
                  continue;
               }
               sMatches[i] = true;
               tMatches[j] = true;
               matches++;
               break;
            }
         }

         if (matches == 0) {
            return 0.0;
         }

         int k = 0;
         for (int i = 0; i < sLen; i++) {
            if (!sMatches[i]) {
               continue;
            }
            while (!tMatches[k]) {
               k++;
            }
            if (s.charAt(i) != t.charAt(k)) {
               transpositions++;
            }
            k++;
         }

         return (((double) matches / sLen) + ((double) matches / tLen) + ((matches - transpositions / 2.0) / matches)) / 3.0;

      }

   }

   static class DoubleMetaphoneSimilarity implements SimilarityScore<Double> {

      private final DoubleMetaphone doubleMetaphone = new DoubleMetaphone();

      @Override
      public Double apply(
            final CharSequence left,
            final CharSequence right) {
         if (StringUtils.isEmpty(left) || StringUtils.isEmpty(right)) {
            return 0.5;
         }

         String leftPrimary = doubleMetaphone.doubleMetaphone(left.toString(), false);
         String leftAlternate = doubleMetaphone.doubleMetaphone(left.toString(), true);
         String rightPrimary = doubleMetaphone.doubleMetaphone(right.toString(), false);
         String rightAlternate = doubleMetaphone.doubleMetaphone(right.toString(), true);

         if (StringUtils.equals(leftPrimary, rightPrimary) || StringUtils.equals(leftPrimary, rightAlternate)
         || StringUtils.equals(leftAlternate, rightPrimary) || StringUtils.equals(leftAlternate, rightAlternate)) {
            return 1.0;
         }
         return 0.0;
      }
   }

   public record ProbabilisticField(
         SimilarityScore<Double> similarityScore,
         List<Float> comparisonLevels,
         List<Float> weights,
         float m,
         float u,
         float min,
         float max) {
      public ProbabilisticField {
         m = limitProbability(m);
         u = limitProbability(u);
         min = fieldScore(false, m, u);
         max = fieldScore(true, m, u);
      }

      public ProbabilisticField(
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
            final var w = IntStream.range(0, n).mapToDouble(i -> abs(1.0 - (z * i))).boxed().map(Double::floatValue).toList();
            if (LOGGER.isDebugEnabled()) {
               try {
                  LOGGER.debug("{}", OBJECT_MAPPER.writeValueAsString(w));
               } catch (JsonProcessingException e) {
                  LOGGER.error(e.getLocalizedMessage(), e);
               }
            }
            return w;
         } else {
            final var k = (n + 1) / 2;
            final var z = 1.0F / k;
            return IntStream.range(0, n)
                            .mapToDouble(i -> abs(1.0 - (z * (i < k
                                                                    ? i
                                                                    : i + 1))))
                            .boxed()
                            .map(Double::floatValue)
                            .toList();
         }
      }

   }

}
