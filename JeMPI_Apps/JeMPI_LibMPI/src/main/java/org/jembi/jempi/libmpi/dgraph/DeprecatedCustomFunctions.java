package org.jembi.jempi.libmpi.dgraph;

import org.jembi.jempi.shared.models.*;

import java.util.List;

public final class DeprecatedCustomFunctions {

   private DeprecatedCustomFunctions() {
   }

//   static GoldenRecord toGoldenRecord(final CustomDgraphGoldenRecord me) {
//      return null;
//      return new GoldenRecord(me.goldenId(),
//                              me.sourceId() != null
//                                    ? me.sourceId().stream().map(DgraphSourceId::toSourceId).toList()
//                                    : List.of(),
//                              new CustomUniqueGoldenRecordData(me.auxDateCreated(),
//                                                               me.auxAutoUpdateEnabled(),
//                                                               me.auxId()),
//                              CustomDemographicData.fromCustomDemographicFields(me.givenName(),
//                                                                                me.familyName(),
//                                                                                me.gender(),
//                                                                                me.dob(),
//                                                                                me.city(),
//                                                                                me.phoneNumber(),
//                                                                                me.nationalId()));
//   }

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
