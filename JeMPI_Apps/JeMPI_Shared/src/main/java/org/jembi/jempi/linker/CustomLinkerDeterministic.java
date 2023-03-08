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
      final var fpidL = goldenRecord.fpid();
      final var fpidR = patient.fpid();
      return isMatch(fpidL, fpidR);
   }

}
