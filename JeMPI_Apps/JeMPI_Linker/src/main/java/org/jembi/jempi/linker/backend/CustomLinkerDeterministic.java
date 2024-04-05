package org.jembi.jempi.linker.backend;

import org.apache.commons.lang3.StringUtils;
import org.jembi.jempi.shared.models.DemographicData;
import org.jembi.jempi.shared.models.MUPacket;

import static org.jembi.jempi.shared.models.CustomDemographicData.*;

final class CustomLinkerDeterministic {

   static final boolean DETERMINISTIC_DO_LINKING = true;
   static final boolean DETERMINISTIC_DO_VALIDATING = false;
   static final boolean DETERMINISTIC_DO_MATCHING = false;

   private CustomLinkerDeterministic() {
   }

   private static boolean isMatch(
         final String left,
         final String right) {
      return StringUtils.isNotBlank(left) && StringUtils.equals(left, right);
   }

   static boolean canApplyLinking(
         final DemographicData interaction) {
      return MUPacket.LINK_MU_FIELD_COUNT > 0
             || StringUtils.isNotBlank(interaction.fields.get(NATIONAL_ID).value())
             || StringUtils.isNotBlank(interaction.fields.get(GIVEN_NAME).value())
             && StringUtils.isNotBlank(interaction.fields.get(FAMILY_NAME).value())
             && StringUtils.isNotBlank(interaction.fields.get(PHONE_NUMBER).value());
   }

   static boolean validateDeterministicMatch(
         final DemographicData goldenRecord,
         final DemographicData interaction) {
      return false;
   }

   static boolean matchNotificationDeterministicMatch(
         final DemographicData goldenRecord,
         final DemographicData interaction) {
      return false;
   }

}
