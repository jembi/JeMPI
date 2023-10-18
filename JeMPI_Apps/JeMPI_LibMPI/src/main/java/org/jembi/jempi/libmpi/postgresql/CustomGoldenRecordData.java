package org.jembi.jempi.libmpi.postgresql;

import org.jembi.jempi.shared.models.CustomDemographicData;

final class CustomGoldenRecordData extends CustomDemographicData implements NodeData {

   CustomGoldenRecordData(final CustomDemographicData customDemographicData) {
      super(customDemographicData.givenName,
            customDemographicData.familyName,
            customDemographicData.gender,
            customDemographicData.dob,
            customDemographicData.city,
            customDemographicData.phoneNumber,
            customDemographicData.nationalId);
   }

}

