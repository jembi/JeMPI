package org.jembi.jempi.linker.backend;

import org.jembi.jempi.libmpi.LibMPI;
import org.jembi.jempi.shared.models.CustomDemographicData;
import org.jembi.jempi.shared.models.Interaction;
import org.jembi.jempi.shared.utils.AppUtils;

import java.util.List;
import java.util.function.Supplier;

import static org.jembi.jempi.shared.models.CustomDemographicData.*;

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
                                                   demographicData.fields.get(GIVEN_NAME).tag(),
                                                   demographicData.fields.get(GIVEN_NAME).value(),
                                                   expandedGoldenRecord.interactionsWithScore()
                                                                       .stream()
                                                                       .map(rec -> rec.interaction().demographicData().fields.get(GIVEN_NAME).value()))
            ? 1
            : 0;
      k += LinkerDWH.helperUpdateGoldenRecordField(libMPI, interactionId, expandedGoldenRecord,
                                                   demographicData.fields.get(FAMILY_NAME).tag(),
                                                   demographicData.fields.get(FAMILY_NAME).value(),
                                                   expandedGoldenRecord.interactionsWithScore()
                                                                       .stream()
                                                                       .map(rec -> rec.interaction().demographicData().fields.get(FAMILY_NAME).value()))
            ? 1
            : 0;
      k += LinkerDWH.helperUpdateGoldenRecordField(libMPI, interactionId, expandedGoldenRecord,
                                                   demographicData.fields.get(GENDER).tag(),
                                                   demographicData.fields.get(GENDER).value(),
                                                   expandedGoldenRecord.interactionsWithScore()
                                                                       .stream()
                                                                       .map(rec -> rec.interaction().demographicData().fields.get(GENDER).value()))
            ? 1
            : 0;
      k += LinkerDWH.helperUpdateGoldenRecordField(libMPI, interactionId, expandedGoldenRecord,
                                                   demographicData.fields.get(DOB).tag(),
                                                   demographicData.fields.get(DOB).value(),
                                                   expandedGoldenRecord.interactionsWithScore()
                                                                       .stream()
                                                                       .map(rec -> rec.interaction().demographicData().fields.get(DOB).value()))
            ? 1
            : 0;
      k += LinkerDWH.helperUpdateGoldenRecordField(libMPI, interactionId, expandedGoldenRecord,
                                                   demographicData.fields.get(CITY).tag(),
                                                   demographicData.fields.get(CITY).value(),
                                                   expandedGoldenRecord.interactionsWithScore()
                                                                       .stream()
                                                                       .map(rec -> rec.interaction().demographicData().fields.get(CITY).value()))
            ? 1
            : 0;
      k += LinkerDWH.helperUpdateGoldenRecordField(libMPI, interactionId, expandedGoldenRecord,
                                                   demographicData.fields.get(PHONE_NUMBER).tag(),
                                                   demographicData.fields.get(PHONE_NUMBER).value(),
                                                   expandedGoldenRecord.interactionsWithScore()
                                                                       .stream()
                                                                       .map(rec -> rec.interaction().demographicData().fields.get(PHONE_NUMBER).value()))
            ? 1
            : 0;
      k += LinkerDWH.helperUpdateGoldenRecordField(libMPI, interactionId, expandedGoldenRecord,
                                                   demographicData.fields.get(NATIONAL_ID).tag(),
                                                   demographicData.fields.get(NATIONAL_ID).value(),
                                                   expandedGoldenRecord.interactionsWithScore()
                                                                       .stream()
                                                                       .map(rec -> rec.interaction().demographicData().fields.get(NATIONAL_ID).value()))
            ? 1
            : 0;

      if (k > 0) {
        LinkerDWH.helperUpdateInteractionsScore(libMPI, threshold, expandedGoldenRecord);
      }

   }

}
