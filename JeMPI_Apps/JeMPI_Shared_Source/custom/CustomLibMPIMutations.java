package org.jembi.jempi.libmpi.dgraph;

import java.util.UUID;

import org.jembi.jempi.shared.models.CustomPatient;
import org.jembi.jempi.shared.utils.AppUtils;

class CustomLibMPIMutations {

   private CustomLibMPIMutations() {}

   static String createPatientTriple(final CustomPatient patient, final String sourceUID) {
      final String uuid = UUID.randomUUID().toString();
      return String.format(
         """
         _:%s  <Patient.source_id>                <%s>        .
         _:%s  <Patient.aux_id>                   %s          .
         _:%s  <Patient.given_name>               %s          .
         _:%s  <Patient.family_name>              %s          .
         _:%s  <Patient.gender>                   %s          .
         _:%s  <Patient.dob>                      %s          .
         _:%s  <Patient.city>                     %s          .
         _:%s  <Patient.phone_number>             %s          .
         _:%s  <Patient.national_id>              %s          .
         _:%s  <dgraph.type>                     "Patient"    .
         """,
         uuid, sourceUID,
         uuid, AppUtils.quotedValue(patient.auxId()),
         uuid, AppUtils.quotedValue(patient.givenName()),
         uuid, AppUtils.quotedValue(patient.familyName()),
         uuid, AppUtils.quotedValue(patient.gender()),
         uuid, AppUtils.quotedValue(patient.dob()),
         uuid, AppUtils.quotedValue(patient.city()),
         uuid, AppUtils.quotedValue(patient.phoneNumber()),
         uuid, AppUtils.quotedValue(patient.nationalId()),
         uuid);
   }

   static String createLinkedGoldenRecordTriple(final CustomPatient patient,
                                                final String patientUID,
                                                final String sourceUID,
                                                final float score) {
      final String uuid = UUID.randomUUID().toString();
      return String.format(
         """
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
         uuid, AppUtils.quotedValue(patient.auxId()),
         uuid, AppUtils.quotedValue(patient.givenName()),
         uuid, AppUtils.quotedValue(patient.familyName()),
         uuid, AppUtils.quotedValue(patient.gender()),
         uuid, AppUtils.quotedValue(patient.dob()),
         uuid, AppUtils.quotedValue(patient.city()),
         uuid, AppUtils.quotedValue(patient.phoneNumber()),
         uuid, AppUtils.quotedValue(patient.nationalId()),
         uuid, patientUID, score,
         uuid);
   }
}
