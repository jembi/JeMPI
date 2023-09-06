package org.jembi.jempi.linker.backend;

import org.apache.commons.lang3.StringUtils;

import org.jembi.jempi.shared.models.CustomDemographicData;

final class CustomLinkerDeterministic {

   private CustomLinkerDeterministic() {
   }

   private static boolean isMatch(
         final String left,
         final String right) {
      return StringUtils.isNotBlank(left) && StringUtils.equals(left, right);
   }

   static boolean linkDeterministicMatch(
         final CustomDemographicData goldenRecord,
         final CustomDemographicData interaction) {
      final var nationalIdL = goldenRecord.nationalId;
      final var nationalIdR = interaction.nationalId;
      return isMatch(nationalIdL, nationalIdR);
   }

   static boolean validateDeterministicMatch(
         final CustomDemographicData goldenRecord,
         final CustomDemographicData interaction) {
      final var givenNameL = goldenRecord.givenName;
      final var givenNameR = interaction.givenName;
      final var familyNameL = goldenRecord.familyName;
      final var familyNameR = interaction.familyName;
      final var phoneNumberL = goldenRecord.phoneNumber;
      final var phoneNumberR = interaction.phoneNumber;
      return (isMatch(givenNameL, givenNameR) && isMatch(familyNameL, familyNameR) && isMatch(phoneNumberL, phoneNumberR));
   }

}
