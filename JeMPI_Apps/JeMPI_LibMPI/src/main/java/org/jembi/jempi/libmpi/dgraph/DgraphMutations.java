package org.jembi.jempi.libmpi.dgraph;

import com.google.protobuf.ByteString;
import io.dgraph.DgraphProto;
import io.vavr.control.Either;
import io.vavr.control.Option;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.jembi.jempi.libmpi.LibMPIClientInterface;
import org.jembi.jempi.libmpi.MpiGeneralError;
import org.jembi.jempi.libmpi.MpiServiceError;
import org.jembi.jempi.shared.models.*;
import org.jembi.jempi.shared.utils.AppUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static org.jembi.jempi.shared.config.Config.DGRAPH_CONFIG;

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
                                                         new CustomUniqueGoldenRecordData(interaction.uniqueInteractionData()));
      if (grUID == null) {
         LOGGER.error("Failed to insert golden record");
         return null;
      }
      return new LinkInfo(grUID, result.interactionUID, result.sourceUID, 1.0F);
   }

   boolean updateGoldenRecordField(
         final String goldenId,
         final String fieldName,
         final String val) {
      String predicate = "GoldenRecord." + AppUtils.camelToSnake(fieldName);
      return updateGoldenRecordPredicate(goldenId, predicate, val);
   }

   private String createSourceIdTriple(final CustomSourceId sourceId) {
      final String uuid = UUID.randomUUID().toString();
      return String.format(Locale.ROOT,
                           """
                           _:%s  <SourceId.facility>                 %s          .
                           _:%s  <SourceId.patient>                  %s          .
                           _:%s  <dgraph.type>                      "SourceId"   .
                           """,
                           uuid,
                           AppUtils.quotedValue(sourceId.facility()),
                           uuid,
                           AppUtils.quotedValue(sourceId.patient()),
                           uuid);
   }

   private boolean updateGoldenRecordPredicate(
         final String goldenId,
         final String predicate,
         final String value) {
      final var mutation = DgraphProto.Mutation.newBuilder()
                                               .setSetNquads(ByteString.copyFromUtf8(String.format(Locale.ROOT,
                                                                                                   """
                                                                                                   <%s> <%s>          "%s"^^<xs:string>    .
                                                                                                   <%s> <dgraph.type> "GoldenRecord"       .
                                                                                                   """,
                                                                                                   goldenId,
                                                                                                   predicate,
                                                                                                   value,
                                                                                                   goldenId)))
                                               .build();
      return DgraphClient.getInstance().doMutateTransaction(mutation) != null;
   }

   //Use this when checking auto-update
   private boolean updateGoldenRecordPredicate(
         final String goldenId,
         final String predicate,
         final Boolean value) {
      final var mutation = DgraphProto.Mutation.newBuilder()
                                               .setSetNquads(ByteString.copyFromUtf8(String.format(Locale.ROOT,
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
      final var mutation = DgraphProto.Mutation.newBuilder()
                                               .setSetNquads(ByteString.copyFromUtf8(String.format(Locale.ROOT,
                                                                                                   """
                                                                                                   <%s> <%s>          "%f"^^<xs:double>    .
                                                                                                   <%s> <dgraph.type> "GoldenRecord"       .
                                                                                                   """,
                                                                                                   goldenId,
                                                                                                   predicate,
                                                                                                   value,
                                                                                                   goldenId)))
                                               .build();
      return DgraphClient.getInstance().doMutateTransaction(mutation) != null;
   }

   private boolean updateGoldenRecordPredicate(
         final String goldenId,
         final String predicate,
         final Long value) {
      final var mutation = DgraphProto.Mutation.newBuilder()
                                               .setSetNquads(ByteString.copyFromUtf8(String.format(Locale.ROOT,
                                                                                                   """
                                                                                                   <%s> <%s>          "%d"^^<xs:integer>    .
                                                                                                   <%s> <dgraph.type> "GoldenRecord"       .
                                                                                                   """,
                                                                                                   goldenId,
                                                                                                   predicate,
                                                                                                   value,
                                                                                                   goldenId)))
                                               .build();
      return DgraphClient.getInstance().doMutateTransaction(mutation) != null;
   }

   private boolean deletePredicate(
         final String uid,
         final String predicate,
         final String value) {
      final var mutation = DgraphProto.Mutation.newBuilder().setDelNquads(ByteString.copyFromUtf8(String.format(Locale.ROOT,
                                                                                                                """
                                                                                                                <%s>  <%s>  <%s>  .
                                                                                                                """,
                                                                                                                uid,
                                                                                                                predicate,
                                                                                                                value))).build();
      return DgraphClient.getInstance().doMutateTransaction(mutation) != null;
   }

   private void addScoreFacets(final List<DgraphPairWithScore> interactionScoreList) {
      StringBuilder simWeightFacet = new StringBuilder();
      for (DgraphPairWithScore interactionScore : interactionScoreList) {
         simWeightFacet.append(String.format(Locale.ROOT,
                                             "<%s> <GoldenRecord.interactions> <%s> (score=%f) .%n",
                                             interactionScore.goldenUID(),
                                             interactionScore.interactionUID(),
                                             interactionScore.score()));
      }

      final var s = simWeightFacet.toString();
      final DgraphProto.Mutation mu = DgraphProto.Mutation.newBuilder().setSetNquads(ByteString.copyFromUtf8(s)).build();

      DgraphClient.getInstance().doMutateTransaction(mu);
   }

   private void addSourceId(
         final String uid,
         final String sourceId) {
      final var mutation = String.format(Locale.ROOT, "<%s> <GoldenRecord.source_id> <%s> .%n", uid, sourceId);
      final DgraphProto.Mutation mu = DgraphProto.Mutation.newBuilder().setSetNquads(ByteString.copyFromUtf8(mutation)).build();
      DgraphClient.getInstance().doMutateTransaction(mu);
   }

   private InsertInteractionResult insertInteraction(final Interaction interaction) {
      final DgraphProto.Mutation sourceIdMutation = DgraphProto.Mutation.newBuilder()
                                                                        .setSetNquads(ByteString.copyFromUtf8(createSourceIdTriple(
                                                                              interaction.sourceId())))
                                                                        .build();
      final var sourceIdList =
            DgraphQueries.findSourceIdList(interaction.sourceId().facility(), interaction.sourceId().patient());
      final var sourceIdUid = !sourceIdList.isEmpty()
            ? sourceIdList.getFirst().uid()
            : DgraphClient.getInstance().doMutateTransaction(sourceIdMutation);
      final DgraphProto.Mutation mutation = DgraphProto
            .Mutation
            .newBuilder()
            .setSetNquads(ByteString.copyFromUtf8(CustomDgraphMutations.createInteractionTriple(
                  interaction.uniqueInteractionData(),
                  interaction.demographicData(),
                  sourceIdUid)))
            .build();
      return new InsertInteractionResult(DgraphClient.getInstance().doMutateTransaction(mutation), sourceIdUid);
   }

   private String cloneGoldenRecordFromInteraction(
         final DemographicData interaction,
         final String interactionUID,
         final String sourceUID,
         final float score,
         final CustomUniqueGoldenRecordData customUniqueGoldenRecordData) {
      final var command = CustomDgraphMutations.createLinkedGoldenRecordTriple(customUniqueGoldenRecordData,
                                                                               interaction,
                                                                               interactionUID,
                                                                               sourceUID,
                                                                               score);
      final DgraphProto.Mutation mutation =
            DgraphProto.Mutation.newBuilder().setSetNquads(ByteString.copyFromUtf8(command)).build();
      return DgraphClient.getInstance().doMutateTransaction(mutation);
   }

   private void deleteGoldenRecord(final String goldenId) {
      final var mutation = DgraphProto.Mutation.newBuilder()
                                               .setDelNquads(ByteString.copyFromUtf8(String.format(Locale.ROOT,
                                                                                                   """
                                                                                                    <%s> * *  .
                                                                                                   """,
                                                                                                   goldenId)))
                                               .build();
      DgraphClient.getInstance().doMutateTransaction(mutation);
   }

//   String camelToSnake(final String str) {
//      return str.replaceAll("([A-Z]+)", "\\_$1").toLowerCase();
//   }

   boolean updateGoldenRecordField(
         final String goldenId,
         final String fieldName,
         final Boolean val) {
      String predicate = "GoldenRecord." + AppUtils.camelToSnake(fieldName);
      return updateGoldenRecordPredicate(goldenId, predicate, val);
   }

   boolean updateGoldenRecordField(
         final String goldenId,
         final String fieldName,
         final Double val) {
      String predicate = "GoldenRecord." + AppUtils.camelToSnake(fieldName);
      return updateGoldenRecordPredicate(goldenId, predicate, val);
   }

   boolean updateGoldenRecordField(
         final String goldenId,
         final String fieldName,
         final Long val) {
      String predicate = "GoldenRecord." + AppUtils.camelToSnake(fieldName);
      return updateGoldenRecordPredicate(goldenId, predicate, val);
   }

   Either<MpiGeneralError, LinkInfo> linkToNewGoldenRecord(
         final String currentGoldenId,
         final String interactionId,
         final float score) {

      final var goldenUidInteractionUidList = DgraphQueries.findExpandedGoldenIds(currentGoldenId);
      if (goldenUidInteractionUidList.isEmpty() || !goldenUidInteractionUidList.contains(interactionId)) {
         return Either.left(new MpiServiceError.GoldenIdInteractionConflictError("Interaction not linked to GoldenRecord",
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
      final var newGoldenID = cloneGoldenRecordFromInteraction(interaction.demographicData(),
                                                               interaction.interactionId(),
                                                               interaction.sourceId().uid(),
                                                               score,
                                                               new CustomUniqueGoldenRecordData(interaction.uniqueInteractionData()));
      return Either.right(new LinkInfo(newGoldenID, interactionId, interaction.sourceId().uid(), score));
   }

   Either<MpiGeneralError, LinkInfo> updateLink(
         final String goldenId,
         final String newGoldenId,
         final String interactionId,
         final float score) {
      final var goldenUidInteractionUidList = DgraphQueries.findExpandedGoldenIds(goldenId);
      if (goldenUidInteractionUidList.isEmpty() || !goldenUidInteractionUidList.contains(interactionId)) {
         return Either.left(new MpiServiceError.GoldenIdInteractionConflictError("Interaction not linked to GoldenRecord",
                                                                                 goldenId,
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
      return Either.right(new LinkInfo(newGoldenId, interactionId, null, score));  // FIX: need to return the source id
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
      return new LinkInfo(grUID, result.interactionUID, result.sourceUID, theScore);
   }

   Option<MpiGeneralError> createSchema() {
      final var schema =
            DGRAPH_CONFIG.mutationCreateAdditionalNodeType
            + System.lineSeparator()
            + DGRAPH_CONFIG.mutationCreateGoldenRecordType
            + System.lineSeparator()
            + DGRAPH_CONFIG.mutationCreateInteractionType
            + System.lineSeparator()
            + DGRAPH_CONFIG.mutationCreateAdditionalNodeFields
            + System.lineSeparator()
            + DGRAPH_CONFIG.mutationCreateGoldenRecordFields
            + System.lineSeparator()
            + DGRAPH_CONFIG.mutationCreateInteractionFields;
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
      final var mutation = DgraphProto.Mutation.newBuilder()
                                               .setSetNquads(ByteString.copyFromUtf8(String.format(Locale.ROOT,
                                                                                                   "<%s> <GoldenRecord"
                                                                                                   + ".interactions> <%s> "
                                                                                                   + "(score=%f) .%n",
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
