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



   public static Interaction applyAutoCreateFunctions(final Interaction interaction) {
      return new Interaction(interaction.interactionId(),
                             interaction.sourceId(),
                             interaction.uniqueInteractionData(),
                             new CustomDemographicData(interaction.demographicData().givenName,
                                                       interaction.demographicData().familyName,
                                                       interaction.demographicData().gender,
                                                       interaction.demographicData().dob,
                                                       interaction.demographicData().city,
                                                       interaction.demographicData().phoneNumberHome,
                                                       interaction.demographicData().phoneNumberMobile,
                                                       interaction.demographicData().phn,
                                                       interaction.demographicData().nic,
                                                       interaction.demographicData().ppn,
                                                       interaction.demographicData().scn,
                                                       interaction.demographicData().dl));
   }

   static void updateGoldenRecordFields(
         final LibMPI libMPI,
         final float threshold,
         final String interactionId,
         final String goldenId) {
      final var expandedGoldenRecord = libMPI.findExpandedGoldenRecords(List.of(goldenId)).get(0);
      final var goldenRecord = expandedGoldenRecord.goldenRecord();
      final var demographicData = goldenRecord.demographicData();
      var k = 0;

      k += LinkerDWH.helperUpdateGoldenRecordField(libMPI, interactionId, expandedGoldenRecord,
                                                  "givenName", demographicData.givenName, CustomDemographicData::getGivenName)
            ? 1
            : 0;
      k += LinkerDWH.helperUpdateGoldenRecordField(libMPI, interactionId, expandedGoldenRecord,
                                                  "familyName", demographicData.familyName, CustomDemographicData::getFamilyName)
            ? 1
            : 0;
      k += LinkerDWH.helperUpdateGoldenRecordField(libMPI, interactionId, expandedGoldenRecord,
                                                  "gender", demographicData.gender, CustomDemographicData::getGender)
            ? 1
            : 0;
      k += LinkerDWH.helperUpdateGoldenRecordField(libMPI, interactionId, expandedGoldenRecord,
                                                  "dob", demographicData.dob, CustomDemographicData::getDob)
            ? 1
            : 0;
      k += LinkerDWH.helperUpdateGoldenRecordField(libMPI, interactionId, expandedGoldenRecord,
                                                  "city", demographicData.city, CustomDemographicData::getCity)
            ? 1
            : 0;
      k += LinkerDWH.helperUpdateGoldenRecordField(libMPI, interactionId, expandedGoldenRecord,
                                                  "phoneNumberHome", demographicData.phoneNumberHome, CustomDemographicData::getPhoneNumberHome)
            ? 1
            : 0;
      k += LinkerDWH.helperUpdateGoldenRecordField(libMPI, interactionId, expandedGoldenRecord,
                                                  "phoneNumberMobile", demographicData.phoneNumberMobile, CustomDemographicData::getPhoneNumberMobile)
            ? 1
            : 0;
      k += LinkerDWH.helperUpdateGoldenRecordField(libMPI, interactionId, expandedGoldenRecord,
                                                  "phn", demographicData.phn, CustomDemographicData::getPhn)
            ? 1
            : 0;
      k += LinkerDWH.helperUpdateGoldenRecordField(libMPI, interactionId, expandedGoldenRecord,
                                                  "nic", demographicData.nic, CustomDemographicData::getNic)
            ? 1
            : 0;
      k += LinkerDWH.helperUpdateGoldenRecordField(libMPI, interactionId, expandedGoldenRecord,
                                                  "ppn", demographicData.ppn, CustomDemographicData::getPpn)
            ? 1
            : 0;
      k += LinkerDWH.helperUpdateGoldenRecordField(libMPI, interactionId, expandedGoldenRecord,
                                                  "scn", demographicData.scn, CustomDemographicData::getScn)
            ? 1
            : 0;
      k += LinkerDWH.helperUpdateGoldenRecordField(libMPI, interactionId, expandedGoldenRecord,
                                                  "dl", demographicData.dl, CustomDemographicData::getDl)
            ? 1
            : 0;

      if (k > 0) {
        LinkerDWH.helperUpdateInteractionsScore(libMPI, threshold, expandedGoldenRecord);
      }

   }

}
