package org.jembi.jempi.linker;

import org.jembi.jempi.libmpi.LibMPI;
import org.jembi.jempi.shared.models.CustomDemographicData;

import java.util.List;

public final class CustomLinkerBackEnd {

   private CustomLinkerBackEnd() {
   }

   static void updateGoldenRecordFields(
         final LibMPI libMPI,
         final String goldenId) {
      final var expandedGoldenRecord = libMPI.findExpandedGoldenRecords(List.of(goldenId)).get(0);
      final var goldenRecord = expandedGoldenRecord.goldenRecord();
      final var demographicData = goldenRecord.demographicData();

      BackEnd.updateGoldenRecordField(expandedGoldenRecord,
                                      "givenName", demographicData.givenName(), CustomDemographicData::givenName);
      BackEnd.updateGoldenRecordField(expandedGoldenRecord,
                                      "familyName", demographicData.familyName(), CustomDemographicData::familyName);
      BackEnd.updateGoldenRecordField(expandedGoldenRecord,
                                      "gender", demographicData.gender(), CustomDemographicData::gender);
      BackEnd.updateGoldenRecordField(expandedGoldenRecord,
                                      "dob", demographicData.dob(), CustomDemographicData::dob);
      BackEnd.updateGoldenRecordField(expandedGoldenRecord,
                                      "city", demographicData.city(), CustomDemographicData::city);
      BackEnd.updateGoldenRecordField(expandedGoldenRecord,
                                      "phoneNumber", demographicData.phoneNumber(), CustomDemographicData::phoneNumber);
      BackEnd.updateGoldenRecordField(expandedGoldenRecord,
                                      "nationalId", demographicData.nationalId(), CustomDemographicData::nationalId);
      BackEnd.updateMatchingPatientRecordScoreForGoldenRecord(expandedGoldenRecord, goldenId);

   }

}
