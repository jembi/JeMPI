package org.jembi.jempi.linker;

import org.apache.commons.lang3.StringUtils;
import org.jembi.jempi.libmpi.models.MpiDocument;
import org.jembi.jempi.libmpi.models.MpiGoldenRecord;

public final class CustomDeterministic {

   private CustomDeterministic() {
   }

   static boolean deterministicMatch(final MpiGoldenRecord goldenRecord, final MpiDocument document) {

      final var fathersName1 = goldenRecord.entity().nameFather();
      final var givenName1 = goldenRecord.entity().nameGiven();
      final var phoneNumber1 = goldenRecord.entity().phoneNumber();
      final var givenName2 = document.entity().nameGiven();
      final var fathersName2 = document.entity().nameFather();
      final var phoneNumber2 = document.entity().phoneNumber();

      return StringUtils.isNotBlank(givenName1) && StringUtils.isNotBlank(givenName2) && StringUtils.isNotBlank(
            phoneNumber1) && StringUtils.isNotBlank(phoneNumber2) && StringUtils.isNotBlank(
            fathersName1) && StringUtils.isNotBlank(fathersName2) && givenName1.equals(givenName2) && fathersName1.equals(
            fathersName2) && phoneNumber1.equals(phoneNumber2);
   }

}
