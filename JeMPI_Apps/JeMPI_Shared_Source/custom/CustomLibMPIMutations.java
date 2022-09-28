package org.jembi.jempi.libmpi.dgraph;

import java.util.UUID;

import org.jembi.jempi.shared.models.CustomEntity;
import org.jembi.jempi.shared.utils.AppUtils;

class CustomLibMPIMutations {

   private CustomLibMPIMutations() {}

   static String createEntityTriple(final CustomEntity customEntity, final String sourceIdUid) {
      final String uuid = UUID.randomUUID().toString();
      return String.format(
         """
         _:%s  <Entity.source_id>                <%s>        .
         _:%s  <Entity.aux_id>                   %s          .
         _:%s  <Entity.given_name>               %s          .
         _:%s  <Entity.family_name>              %s          .
         _:%s  <Entity.gender>                   %s          .
         _:%s  <Entity.dob>                      %s          .
         _:%s  <Entity.city>                     %s          .
         _:%s  <Entity.phone_number>             %s          .
         _:%s  <Entity.national_id>              %s          .
         _:%s  <dgraph.type>                     "Entity"    .
         """,
         uuid, sourceIdUid,
         uuid, AppUtils.quotedValue(customEntity.auxId()),
         uuid, AppUtils.quotedValue(customEntity.givenName()),
         uuid, AppUtils.quotedValue(customEntity.familyName()),
         uuid, AppUtils.quotedValue(customEntity.gender()),
         uuid, AppUtils.quotedValue(customEntity.dob()),
         uuid, AppUtils.quotedValue(customEntity.city()),
         uuid, AppUtils.quotedValue(customEntity.phoneNumber()),
         uuid, AppUtils.quotedValue(customEntity.nationalId()),
         uuid);
   }

   static String createLinkedGoldenRecordTriple(final CustomEntity customEntity,
                                                final String entityUid,
                                                final String sourceUid,
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
         _:%s  <GoldenRecord.entity_list>                   <%s> (score=%f)  .
         _:%s  <dgraph.type>                                "GoldenRecord"   .
         """,
         uuid, sourceUid,
         uuid, AppUtils.quotedValue(customEntity.auxId()),
         uuid, AppUtils.quotedValue(customEntity.givenName()),
         uuid, AppUtils.quotedValue(customEntity.familyName()),
         uuid, AppUtils.quotedValue(customEntity.gender()),
         uuid, AppUtils.quotedValue(customEntity.dob()),
         uuid, AppUtils.quotedValue(customEntity.city()),
         uuid, AppUtils.quotedValue(customEntity.phoneNumber()),
         uuid, AppUtils.quotedValue(customEntity.nationalId()),
         uuid, entityUid, score,
         uuid);
   }
}
