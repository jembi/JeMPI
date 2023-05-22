package org.jembi.jempi.libmpi.postgresql;

import org.jembi.jempi.shared.models.CustomDemographicData;

public class CustomInteractionData extends CustomDemographicData implements NodeData {
   CustomInteractionData(
         final String auxId,
         final String givenName,
         final String familyName,
         final String gender,
         final String dob,
         final String city,
         final String phoneNumber,
         final String nationalId) {
      super(auxId, givenName, familyName, gender, dob, city, phoneNumber, nationalId);
   }

   CustomInteractionData(final CustomDemographicData customDemographicData) {
      super(customDemographicData.auxId,
            customDemographicData.givenName,
            customDemographicData.familyName,
            customDemographicData.gender,
            customDemographicData.dob,
            customDemographicData.city,
            customDemographicData.phoneNumber,
            customDemographicData.nationalId);
   }

}
