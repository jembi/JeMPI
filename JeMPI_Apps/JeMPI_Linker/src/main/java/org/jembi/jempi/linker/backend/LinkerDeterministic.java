package org.jembi.jempi.linker.backend;

import org.jembi.jempi.shared.config.LinkerConfig;
import org.jembi.jempi.shared.models.DemographicData;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import static org.jembi.jempi.shared.config.Config.LINKER_CONFIG;

public final class LinkerDeterministic {

   private LinkerDeterministic() {
   }

   static boolean runPrograms(
         final List<List<LinkerConfig.Operation>> programs,
         final DemographicData goldenRecord,
         final DemographicData interaction) {
      for (final var program : programs) {
         final Deque<Boolean> evalStack = new ArrayDeque<>();
         for (final var operation : program) {
            operation.opcode()
                     .accept(evalStack,
                             new LinkerConfig.Arguments(interaction.fields,
                                                        goldenRecord.fields,
                                                        operation.field(),
                                                        operation.aux()));
         }
         if (Boolean.TRUE.equals(evalStack.pop())) {
            return true;
         }
      }
      return false;
   }

   static boolean linkDeterministicMatch(
         final DemographicData goldenRecord,
         final DemographicData interaction) {
      return runPrograms(LINKER_CONFIG.deterministicLinkPrograms, goldenRecord, interaction);
   }


   static boolean validateDeterministicMatch(
         final DemographicData goldenRecord,
         final DemographicData interaction) {
      return runPrograms(LINKER_CONFIG.deterministicValidatePrograms, goldenRecord, interaction);
   }

   static boolean matchNotificationDeterministicMatch(
         final DemographicData goldenRecord,
         final DemographicData interaction) {
      return runPrograms(LINKER_CONFIG.deterministicMatchPrograms, goldenRecord, interaction);
   }

}
