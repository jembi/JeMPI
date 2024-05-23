package org.jembi.jempi.linker.backend;

import org.jembi.jempi.shared.config.linker.Programs;
import org.jembi.jempi.shared.models.DemographicData;

import static org.jembi.jempi.shared.config.Config.LINKER_CONFIG;

public final class LinkerDeterministic {

   private LinkerDeterministic() {
   }

   static boolean linkDeterministicMatch(
         final DemographicData goldenRecord,
         final DemographicData interaction) {
      return Programs.runDeterministicPrograms(LINKER_CONFIG.deterministicLinkPrograms, interaction, goldenRecord);
   }

   static boolean validateDeterministicMatch(
         final DemographicData goldenRecord,
         final DemographicData interaction) {
      return Programs.runDeterministicPrograms(LINKER_CONFIG.deterministicValidatePrograms, interaction, goldenRecord);
   }

   static boolean matchNotificationDeterministicMatch(
         final DemographicData goldenRecord,
         final DemographicData interaction) {
      return Programs.runDeterministicPrograms(LINKER_CONFIG.deterministicMatchPrograms, interaction, goldenRecord);
   }

}
