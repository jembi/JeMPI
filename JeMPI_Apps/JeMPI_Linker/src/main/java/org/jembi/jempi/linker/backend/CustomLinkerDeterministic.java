package org.jembi.jempi.linker.backend;

import org.apache.commons.lang3.StringUtils;

import org.jembi.jempi.shared.models.CustomDemographicData;

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
         final CustomDemographicData interaction) {
      return CustomLinkerProbabilistic.PROBABILISTIC_DO_LINKING
             || StringUtils.isNotBlank(interaction.givenName)
             && StringUtils.isNotBlank(interaction.familyName)
             && StringUtils.isNotBlank(interaction.phoneNumberMobile)
             && StringUtils.isNotBlank(interaction.phoneNumberHome)
             || StringUtils.isNotBlank(interaction.scn)
             || StringUtils.isNotBlank(interaction.nic)
             || StringUtils.isNotBlank(interaction.phn)
             || StringUtils.isNotBlank(interaction.ppn);
   }

   static boolean linkDeterministicMatch(
         final CustomDemographicData goldenRecord,
         final CustomDemographicData interaction) {
      final var givenNameL = goldenRecord.givenName;
      final var givenNameR = interaction.givenName;
      final var familyNameL = goldenRecord.familyName;
      final var familyNameR = interaction.familyName;
      final var phoneNumberMobileL = goldenRecord.phoneNumberMobile;
      final var phoneNumberMobileR = interaction.phoneNumberMobile;
      final var phoneNumberHomeL = goldenRecord.phoneNumberHome;
      final var phoneNumberHomeR = interaction.phoneNumberHome;
      if ((isMatch(givenNameL, givenNameR) && isMatch(familyNameL, familyNameR) && (isMatch(phoneNumberMobileL, phoneNumberMobileR) || isMatch(phoneNumberHomeL, phoneNumberHomeR)))) {
         return true;
      }
      final var scnL = goldenRecord.scn;
      final var scnR = interaction.scn;
      if (isMatch(scnL, scnR)) {
         return true;
      }
      final var nicL = goldenRecord.nic;
      final var nicR = interaction.nic;
      if (isMatch(nicL, nicR)) {
         return true;
      }
      final var phnL = goldenRecord.phn;
      final var phnR = interaction.phn;
      if (isMatch(phnL, phnR)) {
         return true;
      }
      final var ppnL = goldenRecord.ppn;
      final var ppnR = interaction.ppn;
      return isMatch(ppnL, ppnR);
   }

   static boolean validateDeterministicMatch(
         final CustomDemographicData goldenRecord,
         final CustomDemographicData interaction) {
      return false;
   }

   static boolean matchNotificationDeterministicMatch(
         final CustomDemographicData goldenRecord,
         final CustomDemographicData interaction) {
      return false;
   }

}
