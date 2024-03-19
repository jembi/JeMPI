package org.jembi.jempi.linker.backend;

import org.jembi.jempi.libmpi.LibMPI;
import org.jembi.jempi.shared.models.CustomDemographicData;
import org.jembi.jempi.shared.models.Interaction;
import org.jembi.jempi.shared.utils.AppUtils;

import java.util.List;
import java.util.function.Supplier;

public final class CustomLinkerBackEnd {

   private CustomLinkerBackEnd() {
   }



   static void updateGoldenRecordFields(
         final LibMPI libMPI,
         final float threshold,
         final String interactionId,
         final String goldenId) {
      final var expandedGoldenRecord = libMPI.findExpandedGoldenRecords(List.of(goldenId)).getFirst();
      final var goldenRecord = expandedGoldenRecord.goldenRecord();
      final var demographicData = goldenRecord.demographicData();
      var k = 0;

      k += LinkerDWH.helperUpdateGoldenRecordField(libMPI, interactionId, expandedGoldenRecord,
                                                  "givenName", demographicData.givenName,
                                                  expandedGoldenRecord.interactionsWithScore()
                                                                      .stream()
                                                                      .map(rec -> rec.interaction().demographicData().givenName))
            ? 1
            : 0;
      k += LinkerDWH.helperUpdateGoldenRecordField(libMPI, interactionId, expandedGoldenRecord,
                                                  "familyName", demographicData.familyName,
                                                  expandedGoldenRecord.interactionsWithScore()
                                                                      .stream()
                                                                      .map(rec -> rec.interaction().demographicData().familyName))
            ? 1
            : 0;
      k += LinkerDWH.helperUpdateGoldenRecordField(libMPI, interactionId, expandedGoldenRecord,
                                                  "gender", demographicData.gender,
                                                  expandedGoldenRecord.interactionsWithScore()
                                                                      .stream()
                                                                      .map(rec -> rec.interaction().demographicData().gender))
            ? 1
            : 0;
      k += LinkerDWH.helperUpdateGoldenRecordField(libMPI, interactionId, expandedGoldenRecord,
                                                  "dob", demographicData.dob,
                                                  expandedGoldenRecord.interactionsWithScore()
                                                                      .stream()
                                                                      .map(rec -> rec.interaction().demographicData().dob))
            ? 1
            : 0;
      k += LinkerDWH.helperUpdateGoldenRecordField(libMPI, interactionId, expandedGoldenRecord,
                                                  "city", demographicData.city,
                                                  expandedGoldenRecord.interactionsWithScore()
                                                                      .stream()
                                                                      .map(rec -> rec.interaction().demographicData().city))
            ? 1
            : 0;
      k += LinkerDWH.helperUpdateGoldenRecordField(libMPI, interactionId, expandedGoldenRecord,
                                                  "phoneNumber", demographicData.phoneNumber,
                                                  expandedGoldenRecord.interactionsWithScore()
                                                                      .stream()
                                                                      .map(rec -> rec.interaction().demographicData().phoneNumber))
            ? 1
            : 0;
      k += LinkerDWH.helperUpdateGoldenRecordField(libMPI, interactionId, expandedGoldenRecord,
                                                  "nationalId", demographicData.nationalId,
                                                  expandedGoldenRecord.interactionsWithScore()
                                                                      .stream()
                                                                      .map(rec -> rec.interaction().demographicData().nationalId))
            ? 1
            : 0;

      if (k > 0) {
        LinkerDWH.helperUpdateInteractionsScore(libMPI, threshold, expandedGoldenRecord);
      }

   }

}
