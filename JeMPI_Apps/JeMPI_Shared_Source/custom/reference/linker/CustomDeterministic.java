DEPRECATED

package org.jembi.jempi.linker;

import org.apache.commons.lang3.StringUtils;
import org.jembi.jempi.libmpi.models.MpiDocument;
import org.jembi.jempi.libmpi.models.MpiGoldenRecord;

public final class CustomDeterministic {

   private CustomDeterministic() {
   }

/*
   static boolean deterministicMatch(final MpiGoldenRecord goldenRecord, final MpiDocument document) {

      final var nationalID1 = goldenRecord.entity().nationalId();
      final var nationalID2 = document.entity().nationalId();
      if (StringUtils.isNotBlank(nationalID1) && StringUtils.isNotBlank(nationalID2)
          && nationalID1.equals(nationalID2)) {
         return true;
      }

      final var familyName1 = goldenRecord.entity().familyName();
      final var givenName1 = goldenRecord.entity().givenName();
      final var phoneNumber1 = goldenRecord.entity().phoneNumber();
      final var givenName2 = document.entity().givenName();
      final var familyName2 = document.entity().familyName();
      final var phoneNumber2 = document.entity().phoneNumber();

       return StringUtils.isNotBlank(givenName1) && StringUtils.isNotBlank(givenName2)
              && StringUtils.isNotBlank(phoneNumber1) && StringUtils.isNotBlank(phoneNumber2)
              && StringUtils.isNotBlank(familyName1) && StringUtils.isNotBlank(familyName2)
              && givenName1.equals(givenName2) && familyName1.equals(familyName2) && phoneNumber1.equals(phoneNumber2);
   }
*/

}
