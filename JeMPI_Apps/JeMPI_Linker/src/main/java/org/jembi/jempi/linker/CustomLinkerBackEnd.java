package org.jembi.jempi.linker;

import org.jembi.jempi.libmpi.LibMPI;
import org.jembi.jempi.shared.models.CustomDemographicData;

import java.util.List;

public final class CustomLinkerBackEnd {

   private CustomLinkerBackEnd() {
   }

   static void updateGoldenRecordFields(
         final BackEnd backEnd,
         final LibMPI libMPI,
         final String goldenId) {
      final var expandedGoldenRecord = libMPI.findExpandedGoldenRecords(List.of(goldenId)).get(0);
      final var goldenRecord = expandedGoldenRecord.goldenRecord();
      final var demographicData = goldenRecord.demographicData();
      var k = 0;

      k += backEnd.updateGoldenRecordField(expandedGoldenRecord,
                                           "givenName", demographicData.givenName(), CustomDemographicData::givenName)
            ? 1
            : 0;
      k += backEnd.updateGoldenRecordField(expandedGoldenRecord,
                                           "familyName", demographicData.familyName(), CustomDemographicData::familyName)
            ? 1
            : 0;
      k += backEnd.updateGoldenRecordField(expandedGoldenRecord,
                                           "gender", demographicData.gender(), CustomDemographicData::gender)
            ? 1
            : 0;
      k += backEnd.updateGoldenRecordField(expandedGoldenRecord,
                                           "dob", demographicData.dob(), CustomDemographicData::dob)
            ? 1
            : 0;
      k += backEnd.updateGoldenRecordField(expandedGoldenRecord,
                                           "city", demographicData.city(), CustomDemographicData::city)
            ? 1
            : 0;
      k += backEnd.updateGoldenRecordField(expandedGoldenRecord,
                                           "phoneNumber", demographicData.phoneNumber(), CustomDemographicData::phoneNumber)
            ? 1
            : 0;
      k += backEnd.updateGoldenRecordField(expandedGoldenRecord,
                                           "nationalId", demographicData.nationalId(), CustomDemographicData::nationalId)
            ? 1
            : 0;

      if (k > 0) {
        backEnd.updateMatchingPatientRecordScoreForGoldenRecord(expandedGoldenRecord);
      }

   }

}
