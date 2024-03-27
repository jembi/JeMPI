package org.jembi.jempi.libmpi.postgresql;

import org.jembi.jempi.shared.models.CustomDemographicData;

final class GoldenRecordData extends CustomDemographicData implements NodeData {

   GoldenRecordData(final CustomDemographicData customDemographicData) {
      super(customDemographicData);
   }

}
