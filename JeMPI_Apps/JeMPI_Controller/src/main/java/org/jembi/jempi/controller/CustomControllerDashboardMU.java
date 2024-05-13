package  org.jembi.jempi.controller;

import org.jembi.jempi.shared.models.FieldTallies;
import org.jembi.jempi.shared.models.FieldTallies.FieldTally;

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

   static CustomControllerDashboardMU fromCustomFieldTallies(final FieldTallies customFieldTallies) {
      return new CustomControllerDashboardMU(getMU(customFieldTallies.fieldTallies().get(0)),
                                             getMU(customFieldTallies.fieldTallies().get(1)),
                                             getMU(customFieldTallies.fieldTallies().get(2)),
                                             getMU(customFieldTallies.fieldTallies().get(3)),
                                             getMU(customFieldTallies.fieldTallies().get(4)),
                                             getMU(customFieldTallies.fieldTallies().get(5)),
                                             getMU(customFieldTallies.fieldTallies().get(6)));
   }

}
