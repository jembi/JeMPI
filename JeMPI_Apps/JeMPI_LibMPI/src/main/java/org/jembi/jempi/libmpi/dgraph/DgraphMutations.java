package org.jembi.jempi.libmpi.dgraph;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.protobuf.ByteString;
import io.dgraph.DgraphProto;
import io.vavr.control.Either;
import io.vavr.control.Option;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.jembi.jempi.libmpi.LibMPIClientInterface;
import org.jembi.jempi.libmpi.MpiGeneralError;
import org.jembi.jempi.libmpi.MpiServiceError;
import org.jembi.jempi.shared.models.*;
import org.jembi.jempi.shared.utils.AppUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.jembi.jempi.shared.utils.AppUtils.OBJECT_MAPPER;

final class DgraphMutations {

   private static final Logger LOGGER = LogManager.getLogger(DgraphMutations.class);

   DgraphMutations(final Level level) {
      Configurator.setLevel(this.getClass(), level);
   }

   LinkInfo addNewDGraphInteraction(final Interaction interaction) {
      final var result = insertInteraction(interaction);
      if (result.interactionUID == null) {
         LOGGER.error("Failed to insert interaction");
         return null;
      }
      final var grUID = cloneGoldenRecordFromInteraction(interaction.demographicData(),
                                                         result.interactionUID,
                                                         result.sourceUID,
                                                         1.0F,
                                                         new CustomUniqueGoldenRecordData(LocalDateTime.now(),
                                                                                          true,
                                                                                          interaction.uniqueInteractionData()
                                                                                                     .auxId()));
      if (grUID == null) {
         LOGGER.error("Failed to insert golden record");
         return null;
      }
      return new LinkInfo(grUID, result.interactionUID, 1.0F);
   }

   boolean updateGoldenRecordField(
         final String goldenId,
         final String fieldName,
         final String val) {
      String predicate = "GoldenRecord." + camelToSnake(fieldName);
      return updateGoldenRecordPredicate(goldenId, predicate, val);
   }

   private String createSourceIdTriple(final CustomSourceId sourceId) {
      final String uuid = UUID.randomUUID().toString();
      return String.format("""
                           _:%s  <SourceId.facility>                 %s          .
                           _:%s  <SourceId.patient>                  %s          .
                           _:%s  <dgraph.type>                      "SourceId"   .
                           """, uuid, AppUtils.quotedValue(sourceId.facility()), uuid, AppUtils.quotedValue(sourceId.patient()),
                           uuid);
   }

   private DgraphSourceIds getSourceId(final CustomSourceId sourceId) {
      if (StringUtils.isBlank(sourceId.facility())
          || StringUtils.isBlank(sourceId.patient())) {
         return new DgraphSourceIds(List.of());
      }
      final String query = String.format(
            """
            query query_source_id() {
               var(func: eq(SourceId.facility, "%s")) {
                  A as uid
               }
               var(func: eq(SourceId.patient, "%s")) {
                  B as uid
               }
               all(func: uid(A,B)) @filter (uid(A) AND uid(B)) {
                  uid
                  expand(SourceId)
               }
            }
            """, sourceId.facility(), sourceId.patient());
      return DgraphQueries.runSourceIdQuery(query);
   }

   private boolean updateGoldenRecordPredicate(
         final String goldenId,
         final String predicate,
         final String value) {
      if (LOGGER.isDebugEnabled()) {
         LOGGER.debug("{} {} {}", goldenId, predicate, value);
      }

      final var mutation = DgraphProto.Mutation.newBuilder()
                                               .setSetNquads(ByteString.copyFromUtf8(String.format(
                                                     """
                                                     <%s> <%s>          "%s"^^<xs:string>    .
                                                     <%s> <dgraph.type> "GoldenRecord"       .
                                                     """, goldenId, predicate, value, goldenId)))
                                               .build();
      return DgraphClient.getInstance().doMutateTransaction(mutation) != null;
   }

   //Use this when checking auto-update
   private boolean updateGoldenRecordPredicate(
         final String goldenId,
         final String predicate,
         final Boolean value) {
      if (LOGGER.isDebugEnabled()) {
         LOGGER.debug("{} {} {}", goldenId, predicate, value);
      }
      final var mutation = DgraphProto.Mutation.newBuilder()
                                               .setSetNquads(ByteString.copyFromUtf8(String.format(
                                                     """
                                                     <%s> <%s>          "%s"^^<xs:boolean>   .
                                                     <%s> <dgraph.type> "GoldenRecord"       .
                                                     """,
                                                     goldenId,
                                                     predicate,
                                                     Boolean.TRUE.equals(value)
                                                           ? "true"
                                                           : "false",
                                                     goldenId)))
                                               .build();
      return DgraphClient.getInstance().doMutateTransaction(mutation) != null;
   }

   private boolean updateGoldenRecordPredicate(
         final String goldenId,
         final String predicate,
         final Double value) {
      if (LOGGER.isDebugEnabled()) {
         LOGGER.debug("{} {} {}", goldenId, predicate, value);
      }
      final var mutation = DgraphProto.Mutation.newBuilder()
                                               .setSetNquads(ByteString.copyFromUtf8(String.format(
                                                     """
                                                     <%s> <%s>          "%f"^^<xs:double>    .
                                                     <%s> <dgraph.type> "GoldenRecord"       .
                                                     """, goldenId, predicate, value, goldenId)))
                                               .build();
      return DgraphClient.getInstance().doMutateTransaction(mutation) != null;
   }

   private boolean updateGoldenRecordPredicate(
         final String goldenId,
         final String predicate,
         final Long value) {
      if (LOGGER.isDebugEnabled()) {
         LOGGER.debug("{} {} {}", goldenId, predicate, value);
      }
      final var mutation = DgraphProto.Mutation.newBuilder()
                                               .setSetNquads(ByteString.copyFromUtf8(String.format(
                                                     """
                                                     <%s> <%s>          "%d"^^<xs:integer>    .
                                                     <%s> <dgraph.type> "GoldenRecord"       .
                                                     """, goldenId, predicate, value, goldenId)))
                                               .build();
      return DgraphClient.getInstance().doMutateTransaction(mutation) != null;
   }

   private boolean deletePredicate(
         final String uid,
         final String predicate,
         final String value) {
      if (LOGGER.isDebugEnabled()) {
         LOGGER.debug("{} {} {}", uid, predicate, value);
      }
      final var mutation = DgraphProto.Mutation.newBuilder()
                                               .setDelNquads(ByteString.copyFromUtf8(String.format(
                                                     """
                                                     <%s>  <%s>  <%s>  .
                                                     """, uid, predicate, value)))
                                               .build();
      return DgraphClient.getInstance().doMutateTransaction(mutation) != null;
   }

   private void addScoreFacets(final List<DgraphPairWithScore> interactionScoreList) {
      if (LOGGER.isDebugEnabled()) {
         try {
            LOGGER.debug("{}", OBJECT_MAPPER.writeValueAsString(interactionScoreList));
         } catch (JsonProcessingException e) {
            LOGGER.error(e.getLocalizedMessage(), e);
         }
      }
      StringBuilder simWeightFacet = new StringBuilder();
      for (DgraphPairWithScore interactionScore : interactionScoreList) {
         simWeightFacet.append(
               String.format("<%s> <GoldenRecord.interactions> <%s> (score=%f) .%n",
                             interactionScore.goldenUID(), interactionScore.interactionUID(), interactionScore.score()));
      }

      final var s = simWeightFacet.toString();
      final DgraphProto.Mutation mu = DgraphProto.Mutation.newBuilder().setSetNquads(ByteString.copyFromUtf8(s))
                                                          .build();

      DgraphClient.getInstance().doMutateTransaction(mu);
   }

   private void addSourceId(
         final String uid,
         final String sourceId) {
      if (LOGGER.isDebugEnabled()) {
         LOGGER.debug("{} {}", uid, sourceId);
      }
      final var mutation = String.format("<%s> <GoldenRecord.source_id> <%s> .%n", uid, sourceId);
      final DgraphProto.Mutation mu = DgraphProto.Mutation.newBuilder().setSetNquads(ByteString.copyFromUtf8(mutation))
                                                          .build();
      DgraphClient.getInstance().doMutateTransaction(mu);
   }

   private InsertInteractionResult insertInteraction(final Interaction interaction) {
      if (LOGGER.isDebugEnabled()) {
         try {
            LOGGER.debug("{}", OBJECT_MAPPER.writeValueAsString(interaction));
         } catch (JsonProcessingException e) {
            LOGGER.error(e.getLocalizedMessage(), e);
         }
      }
      final DgraphProto.Mutation sourceIdMutation = DgraphProto.Mutation.newBuilder()
                                                                        .setSetNquads(ByteString.copyFromUtf8(createSourceIdTriple(
                                                                              interaction.sourceId())))
                                                                        .build();
      final var sourceId = getSourceId(interaction.sourceId()).all();
      final var sourceIdUid = !sourceId.isEmpty()
            ? sourceId.get(0).uid()
            : DgraphClient.getInstance().doMutateTransaction(sourceIdMutation);
      final DgraphProto.Mutation mutation = DgraphProto
            .Mutation
            .newBuilder()
            .setSetNquads(ByteString.copyFromUtf8(CustomDgraphMutations
                                                        .createInteractionTriple(interaction.uniqueInteractionData(),
                                                                                 interaction.demographicData(),
                                                                                 sourceIdUid)))
            .build();
      return new InsertInteractionResult(DgraphClient.getInstance().doMutateTransaction(mutation), sourceIdUid);
   }

   private String cloneGoldenRecordFromInteraction(
         final CustomDemographicData interaction,
         final String interactionUID,
         final String sourceUID,
         final float score,
         final CustomUniqueGoldenRecordData customUniqueGoldenRecordData) {
      if (LOGGER.isDebugEnabled()) {
         try {
            LOGGER.debug("{}", OBJECT_MAPPER.writeValueAsString(customUniqueGoldenRecordData));
         } catch (JsonProcessingException e) {
            LOGGER.error(e.getLocalizedMessage(), e);
         }
      }
      final var command = CustomDgraphMutations.createLinkedGoldenRecordTriple(customUniqueGoldenRecordData,
                                                                               interaction,
                                                                               interactionUID,
                                                                               sourceUID,
                                                                               score);
      if (LOGGER.isDebugEnabled()) {
         LOGGER.debug("{}", command);
      }
      final DgraphProto.Mutation mutation = DgraphProto.Mutation.newBuilder()
                                                                .setSetNquads(ByteString.copyFromUtf8(command))
                                                                .build();
      return DgraphClient.getInstance().doMutateTransaction(mutation);
   }

   private void deleteGoldenRecord(final String goldenId) {
      final var mutation = DgraphProto.Mutation.newBuilder()
                                               .setDelNquads(ByteString.copyFromUtf8(
                                                     String.format("""
                                                                    <%s> * *  .
                                                                   """,
                                                                   goldenId)))
                                               .build();
      DgraphClient.getInstance().doMutateTransaction(mutation);
   }

   String camelToSnake(final String str) {
      return str.replaceAll("([A-Z]+)", "\\_$1").toLowerCase();
   }

   boolean updateGoldenRecordField(
         final String goldenId,
         final String fieldName,
         final Boolean val) {
      String predicate = "GoldenRecord." + camelToSnake(fieldName);
      return updateGoldenRecordPredicate(goldenId, predicate, val);
   }

   boolean updateGoldenRecordField(
         final String goldenId,
         final String fieldName,
         final Double val) {
      String predicate = "GoldenRecord." + camelToSnake(fieldName);
      return updateGoldenRecordPredicate(goldenId, predicate, val);
   }

   boolean updateGoldenRecordField(
         final String goldenId,
         final String fieldName,
         final Long val) {
      String predicate = "GoldenRecord." + camelToSnake(fieldName);
      return updateGoldenRecordPredicate(goldenId, predicate, val);
   }

   Either<MpiGeneralError, LinkInfo> linkToNewGoldenRecord(
         final String currentGoldenId,
         final String interactionId,
         final float score) {

      final var goldenUidInteractionUidList = DgraphQueries.findExpandedGoldenIds(currentGoldenId);
      if (goldenUidInteractionUidList.isEmpty() || !goldenUidInteractionUidList.contains(interactionId)) {
         return Either.left(
               new MpiServiceError.GoldenIdInteractionConflictError("Interaction not linked to GoldenRecord",
                                                                    currentGoldenId,
                                                                    interactionId));
      }
      final var count = goldenUidInteractionUidList.size();

      final var interaction = DgraphQueries.findInteraction(interactionId);
      if (interaction == null) {
         LOGGER.warn("interaction {} not found", interactionId);
         return Either.left(new MpiServiceError.InteractionIdDoesNotExistError("Interaction not found", interactionId));
      }
      final var grec = DgraphQueries.findDgraphGoldenRecord(currentGoldenId);
      if (grec == null) {
         return Either.left(new MpiServiceError.GoldenIdDoesNotExistError("Golden Record not found", currentGoldenId));
      }
      if (!deletePredicate(currentGoldenId, CustomDgraphConstants.PREDICATE_GOLDEN_RECORD_INTERACTIONS, interactionId)) {
         return Either.left(new MpiServiceError.DeletePredicateError(interactionId,
                                                                     CustomDgraphConstants.PREDICATE_GOLDEN_RECORD_INTERACTIONS));
      }
      if (count == 1) {
         deleteGoldenRecord(currentGoldenId);
      }
      final var newGoldenID = cloneGoldenRecordFromInteraction(
            interaction.demographicData(), interaction.interactionId(),
            interaction.sourceId().uid(),
            score, new CustomUniqueGoldenRecordData(LocalDateTime.now(), true, interaction.uniqueInteractionData().auxId()));
      return Either.right(new LinkInfo(newGoldenID, interactionId, score));
   }

   Either<MpiGeneralError, LinkInfo> updateLink(
         final String goldenId,
         final String newGoldenId,
         final String interactionId,
         final float score) {
      if (LOGGER.isDebugEnabled()) {
         LOGGER.debug("{} {} {} {}", goldenId, newGoldenId, interactionId, score);
      }
      final var goldenUidInteractionUidList = DgraphQueries.findExpandedGoldenIds(goldenId);
      if (goldenUidInteractionUidList.isEmpty() || !goldenUidInteractionUidList.contains(interactionId)) {
         return Either.left(
               new MpiServiceError.GoldenIdInteractionConflictError("Interaction not linked to GoldenRecord", goldenId,
                                                                    interactionId));
      }

      final var count = DgraphQueries.countGoldenRecordEntities(goldenId);
      deletePredicate(goldenId, "GoldenRecord.interactions", interactionId);
      if (count == 1) {
         deleteGoldenRecord(goldenId);
      }

      final var scoreList = new ArrayList<DgraphPairWithScore>();
      scoreList.add(new DgraphPairWithScore(newGoldenId, interactionId, score));
      addScoreFacets(scoreList);
      return Either.right(new LinkInfo(newGoldenId, interactionId, score));
   }

   LinkInfo linkDGraphInteraction(
         final Interaction interaction,
         final LibMPIClientInterface.GoldenIdScore goldenIdScore) {
      final var result = insertInteraction(interaction);

      if (result.interactionUID == null) {
         LOGGER.error("Failed to insert dgraphInteraction");
         return null;
      }
      final List<DgraphPairWithScore> interactionScoreList = new ArrayList<>();
      interactionScoreList.add(new DgraphPairWithScore(goldenIdScore.goldenId(), result.interactionUID, goldenIdScore.score()));
      addScoreFacets(interactionScoreList);
      addSourceId(interactionScoreList.get(0).goldenUID(), result.sourceUID);
      final var grUID = interactionScoreList.get(0).goldenUID();
      final var theScore = interactionScoreList.get(0).score();
      return new LinkInfo(grUID, result.interactionUID, theScore);
   }

   Option<MpiGeneralError> createSchema() {
      final var schema = CustomDgraphConstants.MUTATION_CREATE_SOURCE_ID_TYPE
                         + CustomDgraphConstants.MUTATION_CREATE_GOLDEN_RECORD_TYPE
                         + CustomDgraphConstants.MUTATION_CREATE_INTERACTION_TYPE
                         + CustomDgraphConstants.MUTATION_CREATE_SOURCE_ID_FIELDS
                         + CustomDgraphConstants.MUTATION_CREATE_GOLDEN_RECORD_FIELDS
                         + CustomDgraphConstants.MUTATION_CREATE_INTERACTION_FIELDS;
      try {
         final DgraphProto.Operation operation = DgraphProto.Operation.newBuilder().setSchema(schema).build();
         DgraphClient.getInstance().alter(operation);
         final var mySchema = DgraphProto.Operation.newBuilder().getSchema();
         LOGGER.trace("{}", mySchema);
         return Option.none();
      } catch (RuntimeException ex) {
         LOGGER.warn("{}", schema);
         LOGGER.error(ex.getLocalizedMessage(), ex);
         return Option.of(new MpiServiceError.GeneralError("Create Schema Error"));
      }
   }

   boolean setScore(
         final String interactionUid,
         final String goldenRecordUid,
         final float score) {
      if (LOGGER.isDebugEnabled()) {
         LOGGER.debug("{} {} {}", interactionUid, goldenRecordUid, score);
      }
      final var mutation = DgraphProto.Mutation.newBuilder()
                                               .setSetNquads(ByteString.copyFromUtf8(String.format(
                                                     "<%s> <GoldenRecord.interactions> <%s> (score=%f) .%n",
                                                     goldenRecordUid,
                                                     interactionUid,
                                                     score)))
                                               .build();
      final var result = DgraphClient.getInstance().doMutateTransaction(mutation);
      if (LOGGER.isTraceEnabled()) {
         LOGGER.trace("set score: {} {} {}", interactionUid, goldenRecordUid, score);
      }
      return result != null;
   }

   private record InsertInteractionResult(
         String interactionUID,
         String sourceUID) {
   }

}
