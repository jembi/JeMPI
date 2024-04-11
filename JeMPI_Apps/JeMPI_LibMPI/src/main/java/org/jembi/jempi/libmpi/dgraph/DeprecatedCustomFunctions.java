package org.jembi.jempi.libmpi.dgraph;

import com.fasterxml.jackson.databind.JsonNode;
import org.jembi.jempi.shared.models.*;
import org.jembi.jempi.shared.utils.AppUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.jembi.jempi.shared.config.Config.JSON_CONFIG;

final class DeprecatedCustomFunctions {

   private DeprecatedCustomFunctions() {
   }

   static GoldenRecord toGoldenRecord(final CustomDgraphGoldenRecord me) {
      return new GoldenRecord(me.goldenId(),
                              me.sourceId() != null
                                    ? me.sourceId().stream().map(DgraphSourceId::toSourceId).toList()
                                    : List.of(),
                              new CustomUniqueGoldenRecordData(me.auxDateCreated(),
                                                               me.auxAutoUpdateEnabled(),
                                                               me.auxId()),
                              CustomDemographicData.fromCustomDemographicFields(me.givenName(),
                                                                                me.familyName(),
                                                                                me.gender(),
                                                                                me.dob(),
                                                                                me.city(),
                                                                                me.phoneNumber(),
                                                                                me.nationalId()));
   }

   static GoldenRecord toGoldenRecord(final JsonNode jsonNode) {
      final var sourceIdList = new ArrayList<CustomSourceId>();
      for (int i = 0; i < JSON_CONFIG.additionalNodes().size(); i++) {
         final var elements = jsonNode.get("GoldenRecord." + AppUtils.camelToSnake(JSON_CONFIG.additionalNodes()
                                                                                              .get(i)
                                                                                              .nodeName()))
                                      .elements();
         while (elements.hasNext()) {
            final var element = elements.next();
            final var uid = element.get("uid").textValue();
            final var facility = element.get("SourceId.facility").textValue();
            final var patient = element.get("SourceId.patient").textValue();
            final var sourceId = new CustomSourceId(uid, facility, patient);
            sourceIdList.add(sourceId);
         }
      }
      return new GoldenRecord(jsonNode.get("uid").asText(),
                              sourceIdList,
                              new CustomUniqueGoldenRecordData(
                                    LocalDateTime.parse(jsonNode.get(CustomDgraphConstants.PREDICATE_GOLDEN_RECORD_AUX_DATE_CREATED)
                                                                .textValue()),
                                    jsonNode.get(CustomDgraphConstants.PREDICATE_GOLDEN_RECORD_AUX_AUTO_UPDATE_ENABLED)
                                            .booleanValue(),
                                    jsonNode.get(CustomDgraphConstants.PREDICATE_GOLDEN_RECORD_AUX_ID)
                                            .textValue()),
                              new DemographicData(JSON_CONFIG.demographicFields()
                                                             .stream()
                                                             .map(field -> new DemographicData.Field(
                                                                   field.fieldName(),
                                                                   jsonNode.get("GoldenRecord." + AppUtils.camelToSnake(field.fieldName()))
                                                                           .textValue()))
                                                             .toList()));
   }

   private static GoldenRecord toGoldenRecord(final CustomDgraphExpandedGoldenRecord me) {
      return new GoldenRecord(me.goldenId(),
                              me.sourceId() != null
                                    ? me.sourceId().stream().map(DgraphSourceId::toSourceId).toList()
                                    : List.of(),
                              new CustomUniqueGoldenRecordData(me.auxDateCreated(),
                                                               me.auxAutoUpdateEnabled(),
                                                               me.auxId()),
                              CustomDemographicData.fromCustomDemographicFields(me.givenName(),
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
                              CustomDemographicData.fromCustomDemographicFields(me.givenName(),
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
                             CustomDemographicData.fromCustomDemographicFields(me.givenName(),
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
                             CustomDemographicData.fromCustomDemographicFields(me.givenName(),
                                                                               me.familyName(),
                                                                               me.gender(),
                                                                               me.dob(),
                                                                               me.city(),
                                                                               me.phoneNumber(),
                                                                               me.nationalId()));
   }

   static InteractionWithScore toInteractionWithScore(final CustomDgraphInteraction me) {
      return new InteractionWithScore(DeprecatedCustomFunctions.toInteraction(me), me.score());
   }

   static ExpandedInteraction toExpandedInteraction(final CustomDgraphExpandedInteraction me) {
      return new ExpandedInteraction(DeprecatedCustomFunctions.toInteraction(me),
                                     me.dgraphGoldenRecordList()
                                       .stream()
                                       .map(DeprecatedCustomFunctions::toGoldenRecordWithScore)
                                       .toList());
   }

}
