package org.jembi.jempi.linker.backend;

import org.jembi.jempi.shared.config.LinkerConfig;
import org.jembi.jempi.shared.models.DemographicData;

import java.util.ArrayDeque;
import java.util.Deque;

import static org.jembi.jempi.shared.config.Config.LINKER_CONFIG;

public final class LinkerDeterministic {

   private LinkerDeterministic() {
   }

   static boolean linkDeterministicMatch(
         final DemographicData goldenRecord,
         final DemographicData interaction) {

      for (final var program : LINKER_CONFIG.deterministicPrograms) {
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

}
