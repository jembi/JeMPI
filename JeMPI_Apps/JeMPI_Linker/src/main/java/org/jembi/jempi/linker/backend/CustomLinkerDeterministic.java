package org.jembi.jempi.linker.backend;

import org.apache.commons.lang3.StringUtils;

import org.jembi.jempi.shared.models.CustomDemographicData;

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
         final CustomDemographicData interaction) {
      return CustomLinkerProbabilistic.PROBABILISTIC_DO_LINKING
             || StringUtils.isNotBlank(interaction.fields.get(NATIONAL_ID).value())
             || StringUtils.isNotBlank(interaction.fields.get(GIVEN_NAME).value())
             && StringUtils.isNotBlank(interaction.fields.get(FAMILY_NAME).value())
             && StringUtils.isNotBlank(interaction.fields.get(PHONE_NUMBER).value());
   }

   static boolean linkDeterministicMatch(
         final CustomDemographicData goldenRecord,
         final CustomDemographicData interaction) {
      final var nationalIdL = goldenRecord.fields.get(NATIONAL_ID).value();
      final var nationalIdR = interaction.fields.get(NATIONAL_ID).value();
      if (isMatch(nationalIdL, nationalIdR)) {
         return true;
      }
      final var givenNameL = goldenRecord.fields.get(GIVEN_NAME).value();
      final var givenNameR = interaction.fields.get(GIVEN_NAME).value();
      final var familyNameL = goldenRecord.fields.get(FAMILY_NAME).value();
      final var familyNameR = interaction.fields.get(FAMILY_NAME).value();
      final var phoneNumberL = goldenRecord.fields.get(PHONE_NUMBER).value();
      final var phoneNumberR = interaction.fields.get(PHONE_NUMBER).value();
      return (isMatch(givenNameL, givenNameR) && isMatch(familyNameL, familyNameR) && isMatch(phoneNumberL, phoneNumberR));
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
