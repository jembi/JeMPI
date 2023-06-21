package org.jembi.jempi.linker.backend;

import org.jembi.jempi.libmpi.LibMPI;
import org.jembi.jempi.shared.models.CustomDemographicData;

import java.util.List;

public final class CustomLinkerBackEnd {

   private CustomLinkerBackEnd() {
   }

   static void updateGoldenRecordFields(
         final BackEnd backEnd,
         final LibMPI libMPI,
         final float threshold,
         final String interactionId,
         final String goldenId) {
      final var expandedGoldenRecord = libMPI.findExpandedGoldenRecords(List.of(goldenId)).get(0);
      final var goldenRecord = expandedGoldenRecord.goldenRecord();
      final var demographicData = goldenRecord.demographicData();
      var k = 0;

      k += backEnd.helperUpdateGoldenRecordField(interactionId, expandedGoldenRecord,
                                                 "givenName", demographicData.givenName, CustomDemographicData::getGivenName)
            ? 1
            : 0;
      k += backEnd.helperUpdateGoldenRecordField(interactionId, expandedGoldenRecord,
                                                 "familyName", demographicData.familyName, CustomDemographicData::getFamilyName)
            ? 1
            : 0;
      k += backEnd.helperUpdateGoldenRecordField(interactionId, expandedGoldenRecord,
                                                 "gender", demographicData.gender, CustomDemographicData::getGender)
            ? 1
            : 0;
      k += backEnd.helperUpdateGoldenRecordField(interactionId, expandedGoldenRecord,
                                                 "dob", demographicData.dob, CustomDemographicData::getDob)
            ? 1
            : 0;
      k += backEnd.helperUpdateGoldenRecordField(interactionId, expandedGoldenRecord,
                                                 "city", demographicData.city, CustomDemographicData::getCity)
            ? 1
            : 0;
      k += backEnd.helperUpdateGoldenRecordField(interactionId, expandedGoldenRecord,
                                                 "phoneNumber", demographicData.phoneNumber, CustomDemographicData::getPhoneNumber)
            ? 1
            : 0;
      k += backEnd.helperUpdateGoldenRecordField(interactionId, expandedGoldenRecord,
                                                 "nationalId", demographicData.nationalId, CustomDemographicData::getNationalId)
            ? 1
            : 0;

      if (k > 0) {
        backEnd.helperUpdateInteractionsScore(threshold, expandedGoldenRecord);
      }

   }

}
