package org.jembi.jempi.libmpi.dgraph;

import org.jembi.jempi.shared.models.*;
import org.jembi.jempi.shared.utils.AppUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public final class DeprecatedCustomFunctions {

   private DeprecatedCustomFunctions() {
   }

   private static DemographicData fromCustomDemographicFields(
         final String givenName,
         final String familyName,
         final String gender,
         final String dob,
         final String city,
         final String phoneNumber,
         final String nationalId) {
      return new DemographicData(new ArrayList<>(Arrays.asList(
            new DemographicData.DemographicField("givenName", givenName),
            new DemographicData.DemographicField("familyName", familyName),
            new DemographicData.DemographicField("gender", gender),
            new DemographicData.DemographicField("dob", dob),
            new DemographicData.DemographicField("city", city),
            new DemographicData.DemographicField("phoneNumber", phoneNumber),
            new DemographicData.DemographicField("nationalId", nationalId))));
   }

   private static GoldenRecord toGoldenRecord(final CustomDgraphExpandedGoldenRecord me) {
      return new GoldenRecord(me.goldenId(),
                              me.sourceId() != null
                                    ? me.sourceId().stream().map(DgraphSourceId::toSourceId).toList()
                                    : List.of(),
                              new CustomUniqueGoldenRecordData(me.auxDateCreated(),
                                                               me.auxAutoUpdateEnabled(),
                                                               me.auxId()),
                              fromCustomDemographicFields(me.givenName(),
                                                          me.familyName(),
                                                          me.gender(),
                                                          me.dob(),
                                                          me.city(),
                                                          me.phoneNumber(),
                                                          me.nationalId()));
   }

   private static GoldenRecord toGoldenRecord(final CustomDgraphReverseGoldenRecord me) {
      return new GoldenRecord(me.goldenId(),
                              me.sourceId() != null
                                    ? me.sourceId().stream().map(DgraphSourceId::toSourceId).toList()
                                    : List.of(),
                              new CustomUniqueGoldenRecordData(me.auxDateCreated(),
                                                               me.auxAutoUpdateEnabled(),
                                                               me.auxId()),
                              fromCustomDemographicFields(me.givenName(),
                                                          me.familyName(),
                                                          me.gender(),
                                                          me.dob(),
                                                          me.city(),
                                                          me.phoneNumber(),
                                                          me.nationalId()));
   }

   private static GoldenRecordWithScore toGoldenRecordWithScore(final CustomDgraphReverseGoldenRecord me) {
      return new GoldenRecordWithScore(toGoldenRecord(me), me.score());
   }

   static ExpandedGoldenRecord toExpandedGoldenRecord(final CustomDgraphExpandedGoldenRecord me) {
      return new ExpandedGoldenRecord(DeprecatedCustomFunctions.toGoldenRecord(me),
                                      me.interactions()
                                        .stream()
                                        .map(DeprecatedCustomFunctions::toInteractionWithScore)
                                        .toList());
   }

   static Interaction toInteraction(final CustomDgraphInteraction me) {
      return new Interaction(me.interactionId(),
                             me.sourceId() != null
                                   ? me.sourceId().toSourceId()
                                   : null,
                             new CustomUniqueInteractionData(me.auxDateCreated(),
                                                             me.auxId(),
                                                             me.auxClinicalData()),
                             fromCustomDemographicFields(me.givenName(),
                                                         me.familyName(),
                                                         me.gender(),
                                                         me.dob(),
                                                         me.city(),
                                                         me.phoneNumber(),
                                                         me.nationalId()));
   }

   private static Interaction toInteraction(final CustomDgraphExpandedInteraction me) {
      return new Interaction(me.interactionId(),
                             me.sourceId().toSourceId(),
                             new CustomUniqueInteractionData(me.auxDateCreated(),
                                                             me.auxId(),
                                                             me.auxClinicalData()),
                             fromCustomDemographicFields(me.givenName(),
                                                         me.familyName(),
                                                         me.gender(),
                                                         me.dob(),
                                                         me.city(),
                                                         me.phoneNumber(),
                                                         me.nationalId()));
   }

   static InteractionWithScore toInteractionWithScore(final CustomDgraphInteraction me) {
      return new InteractionWithScore(toInteraction(me), me.score());
   }

   static ExpandedInteraction toExpandedInteraction(final CustomDgraphExpandedInteraction me) {
      return new ExpandedInteraction(DeprecatedCustomFunctions.toInteraction(me),
                                     me.dgraphGoldenRecordList()
                                       .stream()
                                       .map(DeprecatedCustomFunctions::toGoldenRecordWithScore)
                                       .toList());
   }

   static String createInteractionTriple(
         final CustomUniqueInteractionData uniqueInteractionData,
         final DemographicData demographicData,
         final String sourceUID) {
      final String uuid = UUID.randomUUID().toString();
      final List<Object> params = new ArrayList<>(23);
      params.addAll(List.of(uuid, sourceUID));
      params.addAll(List.of(uuid, AppUtils.quotedValue(uniqueInteractionData.auxDateCreated().toString())));
      params.addAll(List.of(uuid, AppUtils.quotedValue(uniqueInteractionData.auxId())));
      params.addAll(List.of(uuid, AppUtils.quotedValue(uniqueInteractionData.auxClinicalData())));
      params.addAll(List.of(uuid, AppUtils.quotedValue(demographicData.fields.get(0).value())));
      params.addAll(List.of(uuid, AppUtils.quotedValue(demographicData.fields.get(1).value())));
      params.addAll(List.of(uuid, AppUtils.quotedValue(demographicData.fields.get(2).value())));
      params.addAll(List.of(uuid, AppUtils.quotedValue(demographicData.fields.get(3).value())));
      params.addAll(List.of(uuid, AppUtils.quotedValue(demographicData.fields.get(4).value())));
      params.addAll(List.of(uuid, AppUtils.quotedValue(demographicData.fields.get(5).value())));
      params.addAll(List.of(uuid, AppUtils.quotedValue(demographicData.fields.get(6).value())));
      params.add(uuid);
      return """
             _:%s  <Interaction.source_id>                     <%s>                  .
             _:%s  <Interaction.aux_date_created>              %s^^<xs:dateTime>     .
             _:%s  <Interaction.aux_id>                        %s                    .
             _:%s  <Interaction.aux_clinical_data>             %s                    .
             _:%s  <Interaction.demographic_field_00>          %s                    .
             _:%s  <Interaction.demographic_field_01>          %s                    .
             _:%s  <Interaction.demographic_field_02>          %s                    .
             _:%s  <Interaction.demographic_field_03>          %s                    .
             _:%s  <Interaction.demographic_field_04>          %s                    .
             _:%s  <Interaction.demographic_field_05>          %s                    .
             _:%s  <Interaction.demographic_field_06>          %s                    .
             _:%s  <dgraph.type>                               "Interaction"         .
             """.formatted(params.toArray(Object[]::new));
   }

   static String createLinkedGoldenRecordTriple(
         final CustomUniqueGoldenRecordData uniqueGoldenRecordData,
         final DemographicData demographicData,
         final String interactionUID,
         final String sourceUID,
         final float score) {
      final String uuid = UUID.randomUUID().toString();
      final List<Object> params = new ArrayList<>(26);
      params.addAll(List.of(uuid, sourceUID));
      params.addAll(List.of(uuid, AppUtils.quotedValue(uniqueGoldenRecordData.auxDateCreated().toString())));
      params.addAll(List.of(uuid, AppUtils.quotedValue(uniqueGoldenRecordData.auxAutoUpdateEnabled().toString())));
      params.addAll(List.of(uuid, AppUtils.quotedValue(uniqueGoldenRecordData.auxId())));
      params.addAll(List.of(uuid, AppUtils.quotedValue(demographicData.fields.get(0).value())));
      params.addAll(List.of(uuid, AppUtils.quotedValue(demographicData.fields.get(1).value())));
      params.addAll(List.of(uuid, AppUtils.quotedValue(demographicData.fields.get(2).value())));
      params.addAll(List.of(uuid, AppUtils.quotedValue(demographicData.fields.get(3).value())));
      params.addAll(List.of(uuid, AppUtils.quotedValue(demographicData.fields.get(4).value())));
      params.addAll(List.of(uuid, AppUtils.quotedValue(demographicData.fields.get(5).value())));
      params.addAll(List.of(uuid, AppUtils.quotedValue(demographicData.fields.get(6).value())));
      params.addAll(List.of(uuid, interactionUID, score));
      params.add(uuid);
      return """
             _:%s  <GoldenRecord.source_id>                     <%s>                  .
             _:%s  <GoldenRecord.aux_date_created>              %s^^<xs:dateTime>     .
             _:%s  <GoldenRecord.aux_auto_update_enabled>       %s^^<xs:boolean>      .
             _:%s  <GoldenRecord.aux_id>                        %s                    .
             _:%s  <GoldenRecord.demographic_field_00>          %s                    .
             _:%s  <GoldenRecord.demographic_field_01>          %s                    .
             _:%s  <GoldenRecord.demographic_field_02>          %s                    .
             _:%s  <GoldenRecord.demographic_field_03>          %s                    .
             _:%s  <GoldenRecord.demographic_field_04>          %s                    .
             _:%s  <GoldenRecord.demographic_field_05>          %s                    .
             _:%s  <GoldenRecord.demographic_field_06>          %s                    .
             _:%s  <GoldenRecord.interactions>                  <%s> (score=%f)       .
             _:%s  <dgraph.type>                                "GoldenRecord"        .
             """.formatted(params.toArray(Object[]::new));
   }

}
