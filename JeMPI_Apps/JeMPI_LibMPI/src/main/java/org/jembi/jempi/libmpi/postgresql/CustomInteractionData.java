package org.jembi.jempi.libmpi.postgresql;

import org.jembi.jempi.shared.models.CustomDemographicData;

final class CustomInteractionData extends CustomDemographicData implements NodeData {

   CustomInteractionData(final CustomDemographicData customDemographicData) {
      super(customDemographicData.givenName,
            customDemographicData.familyName,
            customDemographicData.gender,
            customDemographicData.dob,
            customDemographicData.city,
            customDemographicData.phoneNumber,
            customDemographicData.nationalId);
   }

}

