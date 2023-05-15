package org.jembi.jempi.libmpi.dgraph;

import org.jembi.jempi.shared.models.CustomDemographicData;
import org.jembi.jempi.shared.utils.AppUtils;

import java.util.UUID;

final class CustomDgraphMutations {

   private CustomDgraphMutations() {
   }

   static String createPatientTriple(
         final CustomDemographicData demographicData,
         final String sourceUID) {
      final String uuid = UUID.randomUUID().toString();
      return String.format("""
                           _:%s  <PatientRecord.source_id>                <%s>        .
                           _:%s  <PatientRecord.aux_id>                   %s          .
                           _:%s  <PatientRecord.given_name>               %s          .
                           _:%s  <PatientRecord.family_name>              %s          .
                           _:%s  <PatientRecord.gender>                   %s          .
                           _:%s  <PatientRecord.dob>                      %s          .
                           _:%s  <PatientRecord.city>                     %s          .
                           _:%s  <PatientRecord.phone_number>             %s          .
                           _:%s  <PatientRecord.national_id>              %s          .
                           _:%s  <dgraph.type>                     "PatientRecord"    .
                           """,
                           uuid, sourceUID,
                           uuid, AppUtils.quotedValue(demographicData.auxId),
                           uuid, AppUtils.quotedValue(demographicData.givenName),
                           uuid, AppUtils.quotedValue(demographicData.familyName),
                           uuid, AppUtils.quotedValue(demographicData.gender),
                           uuid, AppUtils.quotedValue(demographicData.dob),
                           uuid, AppUtils.quotedValue(demographicData.city),
                           uuid, AppUtils.quotedValue(demographicData.phoneNumber),
                           uuid, AppUtils.quotedValue(demographicData.nationalId),
                           uuid);
   }

   static String createLinkedGoldenRecordTriple(
         final CustomDemographicData demographicData,
         final String patientUID,
         final String sourceUID,
         final float score) {
      final String uuid = UUID.randomUUID().toString();
      return String.format("""
                           _:%s  <GoldenRecord.source_id>                     <%s>             .
                           _:%s  <GoldenRecord.aux_id>                        %s               .
                           _:%s  <GoldenRecord.given_name>                    %s               .
                           _:%s  <GoldenRecord.family_name>                   %s               .
                           _:%s  <GoldenRecord.gender>                        %s               .
                           _:%s  <GoldenRecord.dob>                           %s               .
                           _:%s  <GoldenRecord.city>                          %s               .
                           _:%s  <GoldenRecord.phone_number>                  %s               .
                           _:%s  <GoldenRecord.national_id>                   %s               .
                           _:%s  <GoldenRecord.patients>                      <%s> (score=%f)  .
                           _:%s  <dgraph.type>                                "GoldenRecord"   .
                           """,
                           uuid, sourceUID,
                           uuid, AppUtils.quotedValue(demographicData.auxId),
                           uuid, AppUtils.quotedValue(demographicData.givenName),
                           uuid, AppUtils.quotedValue(demographicData.familyName),
                           uuid, AppUtils.quotedValue(demographicData.gender),
                           uuid, AppUtils.quotedValue(demographicData.dob),
                           uuid, AppUtils.quotedValue(demographicData.city),
                           uuid, AppUtils.quotedValue(demographicData.phoneNumber),
                           uuid, AppUtils.quotedValue(demographicData.nationalId),
                           uuid, patientUID, score,
                           uuid);
   }
}
