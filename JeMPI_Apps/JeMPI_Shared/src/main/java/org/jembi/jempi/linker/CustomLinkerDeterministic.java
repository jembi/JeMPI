package org.jembi.jempi.linker;

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

   static boolean deterministicMatch(
         final CustomDemographicData goldenRecord,
         final CustomDemographicData patient) {
      final var givenNameL = goldenRecord.givenName;
      final var givenNameR = patient.givenName;
      final var familyNameL = goldenRecord.familyName;
      final var familyNameR = patient.familyName;
      final var phoneNumberL = goldenRecord.phoneNumber;
      final var phoneNumberR = patient.phoneNumber;
      final var nationalIdL = goldenRecord.nationalId;
      final var nationalIdR = patient.nationalId;
      return (isMatch(nationalIdL, nationalIdR) || (isMatch(givenNameL, givenNameR) && isMatch(familyNameL, familyNameR) && isMatch(phoneNumberL, phoneNumberR)));
   }

}
