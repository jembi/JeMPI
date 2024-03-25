package org.jembi.jempi.linker.backend;

import org.jembi.jempi.shared.config.Config;
import org.jembi.jempi.shared.models.CustomMU;

import java.util.Collections;
import java.util.List;

final class CustomLinkerProbabilistic {

   static final boolean PROBABILISTIC_DO_LINKING = true;
   static final boolean PROBABILISTIC_DO_VALIDATING = false;
   static final boolean PROBABILISTIC_DO_MATCHING = false;


   private CustomLinkerProbabilistic() {
   }

   static List<LinkerProbabilistic.ProbabilisticField> toLinkProbabilisticFieldList(final CustomMU.CustomLinkMU mu) {
      if (mu.givenName().m() > mu.givenName().u()
          && mu.familyName().m() > mu.familyName().u()
          && mu.gender().m() > mu.gender().u()
          && mu.dob().m() > mu.dob().u()
          && mu.city().m() > mu.city().u()
          && mu.phoneNumber().m() > mu.phoneNumber().u()
          && mu.nationalId().m() > mu.nationalId().u()) {
         return List.of(
            new LinkerProbabilistic
               .ProbabilisticField(LinkerProbabilistic.getSimilarityFunction(Config.LINKER.probabilisticLinkFields.get(0)
                                                                                                                  .similarityScore()),
                                    Config.LINKER.probabilisticLinkFields.get(0).comparisonLevels(),
                                    mu.givenName().m(),
                                    mu.givenName().u()),
            new LinkerProbabilistic
               .ProbabilisticField(LinkerProbabilistic.getSimilarityFunction(Config.LINKER.probabilisticLinkFields.get(1)
                                                                                                                  .similarityScore()),
                                    Config.LINKER.probabilisticLinkFields.get(1).comparisonLevels(),
                                    mu.familyName().m(),
                                    mu.familyName().u()),
            new LinkerProbabilistic
               .ProbabilisticField(LinkerProbabilistic.getSimilarityFunction(Config.LINKER.probabilisticLinkFields.get(2)
                                                                                                                  .similarityScore()),
                                    Config.LINKER.probabilisticLinkFields.get(2).comparisonLevels(),
                                    mu.gender().m(),
                                    mu.gender().u()),
            new LinkerProbabilistic
               .ProbabilisticField(LinkerProbabilistic.getSimilarityFunction(Config.LINKER.probabilisticLinkFields.get(3)
                                                                                                                  .similarityScore()),
                                    Config.LINKER.probabilisticLinkFields.get(3).comparisonLevels(),
                                    mu.dob().m(),
                                    mu.dob().u()),
            new LinkerProbabilistic
               .ProbabilisticField(LinkerProbabilistic.getSimilarityFunction(Config.LINKER.probabilisticLinkFields.get(4)
                                                                                                                  .similarityScore()),
                                    Config.LINKER.probabilisticLinkFields.get(4).comparisonLevels(),
                                    mu.city().m(),
                                    mu.city().u()),
            new LinkerProbabilistic
               .ProbabilisticField(LinkerProbabilistic.getSimilarityFunction(Config.LINKER.probabilisticLinkFields.get(5)
                                                                                                                  .similarityScore()),
                                    Config.LINKER.probabilisticLinkFields.get(5).comparisonLevels(),
                                    mu.phoneNumber().m(),
                                    mu.phoneNumber().u()),
            new LinkerProbabilistic
               .ProbabilisticField(LinkerProbabilistic.getSimilarityFunction(Config.LINKER.probabilisticLinkFields.get(6)
                                                                                                                  .similarityScore()),
                                    Config.LINKER.probabilisticLinkFields.get(6).comparisonLevels(),
                                    mu.nationalId().m(),
                                    mu.nationalId().u()));
      } else {
         return Collections.emptyList();
      }
    }

   static List<LinkerProbabilistic.ProbabilisticField> toValidateProbabilisticFieldList(final CustomMU.CustomValidateMU mu) {
      return Collections.emptyList();
   }

   static List<LinkerProbabilistic.ProbabilisticField> toMatchProbabilisticFieldList(final CustomMU.CustomMatchMU mu) {
      return Collections.emptyList();
   }

}
