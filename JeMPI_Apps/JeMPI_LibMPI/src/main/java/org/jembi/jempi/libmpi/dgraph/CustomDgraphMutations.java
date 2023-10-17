package org.jembi.jempi.libmpi.dgraph;

import org.jembi.jempi.shared.models.CustomUniqueInteractionData;
import org.jembi.jempi.shared.models.CustomUniqueGoldenRecordData;
import org.jembi.jempi.shared.models.CustomDemographicData;
import org.jembi.jempi.shared.utils.AppUtils;

import java.util.Locale;
import java.util.UUID;

final class CustomDgraphMutations {

   private CustomDgraphMutations() {
   }

   static String createInteractionTriple(
         final CustomUniqueInteractionData uniqueInteractionData,
         final CustomDemographicData demographicData,
         final String sourceUID) {
      final String uuid = UUID.randomUUID().toString();
      return String.format(Locale.ROOT,
                           """
                           _:%s  <Interaction.source_id>                     <%s>                  .
                           _:%s  <Interaction.aux_date_created>              %s^^<xs:dateTime>     .
                           _:%s  <Interaction.aux_id>                        %s                    .
                           _:%s  <Interaction.aux_clinical_data>             %s                    .
                           _:%s  <Interaction.given_name>                    %s                    .
                           _:%s  <Interaction.family_name>                   %s                    .
                           _:%s  <Interaction.gender>                        %s                    .
                           _:%s  <Interaction.dob>                           %s                    .
                           _:%s  <Interaction.city>                          %s                    .
                           _:%s  <Interaction.phone_number>                  %s                    .
                           _:%s  <Interaction.phn>                           %s                    .
                           _:%s  <Interaction.nic>                           %s                    .
                           _:%s  <Interaction.my_golden_id_a>                %s                    .
                           _:%s  <Interaction.my_golden_id_b>                %s                    .
                           _:%s  <dgraph.type>                               "Interaction"         .
                           """,
                           uuid, sourceUID,
                           uuid, AppUtils.quotedValue(uniqueInteractionData.auxDateCreated().toString()),
                           uuid, AppUtils.quotedValue(uniqueInteractionData.auxId()),
                           uuid, AppUtils.quotedValue(uniqueInteractionData.auxClinicalData()),
                           uuid, AppUtils.quotedValue(demographicData.givenName),
                           uuid, AppUtils.quotedValue(demographicData.familyName),
                           uuid, AppUtils.quotedValue(demographicData.gender),
                           uuid, AppUtils.quotedValue(demographicData.dob),
                           uuid, AppUtils.quotedValue(demographicData.city),
                           uuid, AppUtils.quotedValue(demographicData.phoneNumber),
                           uuid, AppUtils.quotedValue(demographicData.phn),
                           uuid, AppUtils.quotedValue(demographicData.nic),
                           uuid, AppUtils.quotedValue(demographicData.myGoldenIdA),
                           uuid, AppUtils.quotedValue(demographicData.myGoldenIdB),
                           uuid);
   }

   static String createLinkedGoldenRecordTriple(
         final CustomUniqueGoldenRecordData uniqueGoldenRecordData,
         final CustomDemographicData demographicData,
         final String interactionUID,
         final String sourceUID,
         final float score) {
      final String uuid = UUID.randomUUID().toString();
      return String.format(Locale.ROOT,
                           """
                           _:%s  <GoldenRecord.source_id>                     <%s>                  .
                           _:%s  <GoldenRecord.aux_date_created>              %s^^<xs:dateTime>     .
                           _:%s  <GoldenRecord.aux_auto_update_enabled>       %s^^<xs:boolean>      .
                           _:%s  <GoldenRecord.aux_id>                        %s                    .
                           _:%s  <GoldenRecord.given_name>                    %s                    .
                           _:%s  <GoldenRecord.family_name>                   %s                    .
                           _:%s  <GoldenRecord.gender>                        %s                    .
                           _:%s  <GoldenRecord.dob>                           %s                    .
                           _:%s  <GoldenRecord.city>                          %s                    .
                           _:%s  <GoldenRecord.phone_number>                  %s                    .
                           _:%s  <GoldenRecord.phn>                           %s                    .
                           _:%s  <GoldenRecord.nic>                           %s                    .
                           _:%s  <GoldenRecord.my_golden_id_a>                %s                    .
                           _:%s  <GoldenRecord.my_golden_id_b>                %s                    .
                           _:%s  <GoldenRecord.interactions>                  <%s> (score=%f)       .
                           _:%s  <dgraph.type>                                "GoldenRecord"        .
                           """,
                           uuid, sourceUID,
                           uuid, AppUtils.quotedValue(uniqueGoldenRecordData.auxDateCreated().toString()),
                           uuid, AppUtils.quotedValue(uniqueGoldenRecordData.auxAutoUpdateEnabled().toString()),
                           uuid, AppUtils.quotedValue(uniqueGoldenRecordData.auxId()),
                           uuid, AppUtils.quotedValue(demographicData.givenName),
                           uuid, AppUtils.quotedValue(demographicData.familyName),
                           uuid, AppUtils.quotedValue(demographicData.gender),
                           uuid, AppUtils.quotedValue(demographicData.dob),
                           uuid, AppUtils.quotedValue(demographicData.city),
                           uuid, AppUtils.quotedValue(demographicData.phoneNumber),
                           uuid, AppUtils.quotedValue(demographicData.phn),
                           uuid, AppUtils.quotedValue(demographicData.nic),
                           uuid, AppUtils.quotedValue(demographicData.myGoldenIdA),
                           uuid, AppUtils.quotedValue(demographicData.myGoldenIdB),
                           uuid, interactionUID, score,
                           uuid);
   }
}
