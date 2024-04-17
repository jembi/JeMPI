package  org.jembi.jempi.controller;

import org.jembi.jempi.shared.models.CustomFieldTallies;
import org.jembi.jempi.shared.models.CustomFieldTallies.FieldTally;

record CustomControllerDashboardMU(
      MU givenName,
      MU familyName,
      MU gender,
      MU dob,
      MU city,
      MU phoneNumber,
      MU nationalId) {

   static MU getMU(final FieldTally fieldTally) {
      if (fieldTally.a() + fieldTally.b() == 0 || fieldTally.c() + fieldTally.d() == 0) {
         return new MU(-1.0, -1.0);
      }
      return new MU(fieldTally.a().doubleValue() / (fieldTally.a().doubleValue() + fieldTally.b().doubleValue()),
                    fieldTally.c().doubleValue() / (fieldTally.c().doubleValue() + fieldTally.d().doubleValue()));
   }

   record MU(
      Double m,
      Double u) {
   }

   static CustomControllerDashboardMU fromCustomFieldTallies(final CustomFieldTallies customFieldTallies) {
      return new CustomControllerDashboardMU(getMU(customFieldTallies.givenName()),
                                             getMU(customFieldTallies.familyName()),
                                             getMU(customFieldTallies.gender()),
                                             getMU(customFieldTallies.dob()),
                                             getMU(customFieldTallies.city()),
                                             getMU(customFieldTallies.phoneNumber()),
                                             getMU(customFieldTallies.nationalId()));
   }

}
