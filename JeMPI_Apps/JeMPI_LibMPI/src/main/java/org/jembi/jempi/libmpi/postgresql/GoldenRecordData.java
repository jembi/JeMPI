package org.jembi.jempi.libmpi.postgresql;

import org.jembi.jempi.shared.models.DemographicData;

final class GoldenRecordData extends DemographicData implements NodeData {

   GoldenRecordData(final DemographicData demographicData) {
      super(demographicData);
   }

}
