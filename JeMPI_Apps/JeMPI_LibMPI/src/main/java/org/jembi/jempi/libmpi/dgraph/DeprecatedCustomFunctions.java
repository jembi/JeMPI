package org.jembi.jempi.libmpi.dgraph;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.shared.models.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class DeprecatedCustomFunctions {

   private static final Logger LOGGER = LogManager.getLogger(DeprecatedCustomFunctions.class);

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

/*
   private static GoldenRecord toGoldenRecord(final CustomDgraphExpandedGoldenRecord me) {
      return new GoldenRecord(me.goldenId(),
                              me.sourceId() != null
                                    ? me.sourceId().stream().map(DgraphSourceId::toSourceId).toList()
                                    : List.of(),
                              new AuxGoldenRecordData(me.auxDateCreated(),
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
*/

   private static GoldenRecord toGoldenRecord(final CustomDgraphReverseGoldenRecord me) {
      return new GoldenRecord(me.goldenId(),
                              me.sourceId() != null
                                    ? me.sourceId().stream().map(DgraphSourceId::toSourceId).toList()
                                    : List.of(),
                              new AuxGoldenRecordData(me.auxDateCreated(),
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

   private static Interaction toInteraction(final CustomDgraphExpandedInteraction me) {
      return new Interaction(me.interactionId(),
                             me.sourceId().toSourceId(),
                             new AuxInteractionData(me.auxDateCreated(),
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

/*
   private static Interaction toInteraction(final CustomDgraphInteraction me) {
      return new Interaction(me.interactionId(),
                             me.sourceId() != null
                                   ? me.sourceId().toSourceId()
                                   : null,
                             new AuxInteractionData(me.auxDateCreated(),
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
*/

/*
   private static InteractionWithScore toInteractionWithScore(final CustomDgraphInteraction me) {
      return new InteractionWithScore(toInteraction(me), me.score());
   }
*/

/*
   private static ExpandedGoldenRecord toExpandedGoldenRecord(final CustomDgraphExpandedGoldenRecord me) {
      try {
         final var json = System.lineSeparator() + OBJECT_MAPPER.writeValueAsString(me);
         LOGGER.debug("{}", json);
      } catch (JsonProcessingException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
      }
      final var interactions = me.interactions();
      return new ExpandedGoldenRecord(toGoldenRecord(me),
                                      interactions
                                            .stream()
                                            .map(DeprecatedCustomFunctions::toInteractionWithScore)
                                            .toList());
   }
*/

   static ExpandedInteraction toExpandedInteraction(final CustomDgraphExpandedInteraction me) {
      return new ExpandedInteraction(DeprecatedCustomFunctions.toInteraction(me),
                                     me.dgraphGoldenRecordList()
                                       .stream()
                                       .map(DeprecatedCustomFunctions::toGoldenRecordWithScore)
                                       .toList());
   }

}
