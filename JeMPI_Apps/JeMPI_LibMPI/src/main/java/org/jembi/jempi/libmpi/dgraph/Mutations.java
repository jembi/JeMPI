package org.jembi.jempi.libmpi.dgraph;

import com.google.protobuf.ByteString;
import io.dgraph.DgraphProto;
import io.netty.util.internal.StringUtil;
import io.vavr.control.Either;
import io.vavr.control.Option;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.libmpi.LibMPIClientInterface;
import org.jembi.jempi.libmpi.MpiGeneralError;
import org.jembi.jempi.libmpi.MpiServiceError;
import org.jembi.jempi.shared.models.CustomEntity;
import org.jembi.jempi.shared.models.LinkInfo;
import org.jembi.jempi.shared.models.SourceId;
import org.jembi.jempi.shared.utils.AppUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.jembi.jempi.libmpi.dgraph.CustomLibMPIConstants.*;

final class Mutations {

    private static final Logger LOGGER = LogManager.getLogger(Mutations.class);

    private Mutations() {
    }

    private static String createSourceIdTriple(final SourceId sourceId) {
        final String uuid = UUID.randomUUID().toString();
        return String.format("""
                        _:%s  <SourceId.facility>                 %s          .
                        _:%s  <SourceId.patient>                  %s          .
                        _:%s  <dgraph.type>                      "SourceId"   .
                        """, uuid, AppUtils.quotedValue(sourceId.facility()), uuid, AppUtils.quotedValue(sourceId.patient()),
                uuid);
    }

    private static LibMPISourceIdList getSourceId(final SourceId sourceId) {
        if (StringUtils.isBlank(sourceId.facility()) ||
                StringUtils.isBlank(sourceId.patient())) {
            return new LibMPISourceIdList(List.of());
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
        return Queries.runSourceIdQuery(query);
    }

    private static boolean updateGoldenRecordPredicate(final String uid, final String predicate, final String value) {
        final var mutation = DgraphProto.Mutation.newBuilder()
                .setSetNquads(ByteString.copyFromUtf8(String.format(
                        """
                                <%s> <%s>          "%s"^^<xs:string>    .
                                <%s> <dgraph.type> "GoldenRecord"       .
                                """, uid, predicate, value, uid)))
                .build();
        final var result = Client.getInstance().doMutateTransaction(mutation);
        return StringUtil.isNullOrEmpty(result);
    }

    private static boolean deletePredicate(final String uid, final String predicate, final String value) {
        final var mutation = DgraphProto.Mutation.newBuilder()
                .setDelNquads(ByteString.copyFromUtf8(String.format(
                        """
                                <%s>  <%s>  <%s>  .
                                """, uid, predicate, value)))
                .build();
        final var result = Client.getInstance().doMutateTransaction(mutation);
        return result != null;
    }

    private static void addScoreFacets(final List<LibMPIEntityScore> entityScoreList) {
        StringBuilder simWeightFacet = new StringBuilder();
        for (LibMPIEntityScore entityScore : entityScoreList) {
            simWeightFacet.append(
                    String.format("<%s> <GoldenRecord.entity_list> <%s> (score=%f) .%n",
                            entityScore.goldenUid(), entityScore.entityUid(), entityScore.score()));
        }

        final var s = simWeightFacet.toString();
        final DgraphProto.Mutation mu = DgraphProto.Mutation.newBuilder().setSetNquads(ByteString.copyFromUtf8(s)).build();

        Client.getInstance().doMutateTransaction(mu);
    }

    private static void addSourceId(final String uid, final String sourceId) {
        var mutation = String.format("<%s> <GoldenRecord.source_id> <%s> .%n", uid, sourceId);
        final DgraphProto.Mutation mu = DgraphProto.Mutation.newBuilder().setSetNquads(ByteString.copyFromUtf8(mutation)).build();
        Client.getInstance().doMutateTransaction(mu);
    }

    private static InsertEntityResult insertEntity(final CustomEntity customEntity) {
        final DgraphProto.Mutation sourceIdMutation =
                DgraphProto.Mutation.newBuilder()
                        .setSetNquads(
                                ByteString.copyFromUtf8(createSourceIdTriple(customEntity.sourceId())))
                        .build();
        final var sourceId = getSourceId(customEntity.sourceId()).all();
        final var sourceIdUid = !sourceId.isEmpty()
                ? sourceId.get(0).uid()
                : Client.getInstance().doMutateTransaction(sourceIdMutation);
        final DgraphProto.Mutation mutation = DgraphProto.Mutation.newBuilder().setSetNquads(
                ByteString.copyFromUtf8(CustomLibMPIMutations.createEntityTriple(customEntity, sourceIdUid))).build();
        return new InsertEntityResult(Client.getInstance().doMutateTransaction(mutation), sourceIdUid);
    }

    private static String cloneGoldenRecordFromEntity(final CustomEntity customEntity,
                                                      final String entityUid,
                                                      final String sourceUid,
                                                      final float score) {
        final var command = CustomLibMPIMutations.createLinkedGoldenRecordTriple(customEntity, entityUid, sourceUid, score);
        final DgraphProto.Mutation mutation = DgraphProto.Mutation.newBuilder().setSetNquads(
                ByteString.copyFromUtf8(command)).build();
        return Client.getInstance().doMutateTransaction(mutation);
    }

    private static void deleteGoldenRecord(final String uid) {
        final var mutation = DgraphProto.Mutation.newBuilder()
                .setDelNquads(ByteString.copyFromUtf8(
                        String.format("""
                                         <%s> * *  .
                                        """,
                                uid)))
                .build();
        Client.getInstance().doMutateTransaction(mutation);
    }

    static LinkInfo addNewDGraphEntity(final CustomEntity customEntity) {
        final var result = insertEntity(customEntity);
        if (result.entityUid == null) {
            LOGGER.error("Failed to insert dgraphEntity");
            return null;
        }
        final var grUID = cloneGoldenRecordFromEntity(customEntity, result.entityUid, result.sourceUid, 1.0F);
        if (grUID == null) {
            LOGGER.error("Failed to insert golden record");
            return null;
        }
        return new LinkInfo(grUID, result.entityUid, 1.0F);
    }

    static String camelToSnake(String str) {
        return str.replaceAll("([A-Z]+)", "\\_$1").toLowerCase();
    }

    static boolean updateGoldenRecordField(final String uid, final String fieldName, final String val) {
        String predicate = "GoldenRecord." + camelToSnake(fieldName);
        return updateGoldenRecordPredicate(uid, predicate, val);
    }

    static Either<MpiGeneralError, LinkInfo> unLink(final String goldenID,
                                                    final String entityID,
                                                    final float score) {

        final var goldenIdEntityIdList = Queries.getGoldenIdEntityIdList(goldenID);
        if (goldenIdEntityIdList.isEmpty() || !goldenIdEntityIdList.contains(entityID)) {
            return Either.left(
                    new MpiServiceError.GoldenIDEntityConflictError("Entity not linked to GoldenRecord",
                            goldenID,
                            entityID));
        }
        final var count = goldenIdEntityIdList.size();

        final var dGraphEntity = Queries.getDGraphEntity(entityID);
        if (dGraphEntity == null) {
            LOGGER.warn("entity {} not found", entityID);
            return Either.left(new MpiServiceError.EntityIDDoesNotExistError("Entity not found", entityID));
        }
        final var grec = Queries.getGoldenRecordByUid(goldenID);
        if (grec == null) {
            return Either.left(new MpiServiceError.GoldenIDDoesNotExistError("Golden Record not found", goldenID));
        }
        if (!deletePredicate(goldenID, PREDICATE_GOLDEN_RECORD_ENTITY_LIST, entityID)) {
            return Either.left(new MpiServiceError.DeletePredicateError(entityID, PREDICATE_GOLDEN_RECORD_ENTITY_LIST));
        }
        if (count == 1) {
            deleteGoldenRecord(goldenID);
        }
        final var newGoldenID = cloneGoldenRecordFromEntity(dGraphEntity, dGraphEntity.uid(), dGraphEntity.sourceId().uid(), score);
        return Either.right(new LinkInfo(newGoldenID, entityID, score));
    }

    static Either<MpiGeneralError, LinkInfo> updateLink(final String goldenID, final String newGoldenID,
                                                        final String entityID, float score) {

        final var goldenIdEntityIdList = Queries.getGoldenIdEntityIdList(goldenID);
        if (goldenIdEntityIdList.isEmpty() || !goldenIdEntityIdList.contains(entityID)) {
            return Either.left(
                    new MpiServiceError.GoldenIDEntityConflictError("Entity not linked to GoldenRecord", goldenID, entityID));
        }

        final var count = Queries.countGoldenRecordEntities(goldenID);
        deletePredicate(goldenID, "GoldenRecord.entity_list", entityID);
        if (count == 1) {
            deleteGoldenRecord(goldenID);
        }

        final var scoreList = new ArrayList<LibMPIEntityScore>();
        scoreList.add(new LibMPIEntityScore(newGoldenID, entityID, score));
        addScoreFacets(scoreList);
        return Either.right(new LinkInfo(newGoldenID, entityID, score));
    }

    static LinkInfo linkDGraphEntity(final CustomEntity customEntity,
                                     final LibMPIClientInterface.GoldenIdScore goldenIdScore) {
        final var result = insertEntity(customEntity);

        if (result.entityUid == null) {
            LOGGER.error("Failed to insert dgraphEntity");
            return null;
        }
        final List<LibMPIEntityScore> entityScoreList = new ArrayList<>();
        entityScoreList.add(new LibMPIEntityScore(goldenIdScore.goldenId(), result.entityUid, goldenIdScore.score()));
        addScoreFacets(entityScoreList);
        addSourceId(entityScoreList.get(0).goldenUid(), result.sourceUid);
        final var grUID = entityScoreList.get(0).goldenUid();
        final var theScore = entityScoreList.get(0).score();
        return new LinkInfo(grUID, result.entityUid, theScore);
    }

    static Option<MpiGeneralError> createSchema() {
        final var schema =
                MUTATION_CREATE_SOURCE_ID_TYPE
                        + MUTATION_CREATE_GOLDEN_RECORD_TYPE
                        + MUTATION_CREATE_ENTITY_TYPE
                        + MUTATION_CREATE_SOURCE_ID_FIELDS
                        + MUTATION_CREATE_GOLDEN_RECORD_FIELDS
                        + MUTATION_CREATE_ENTITY_FIELDS;
        try {
            final DgraphProto.Operation operation = DgraphProto.Operation.newBuilder().setSchema(schema).build();
            Client.getInstance().alter(operation);
            return Option.none();
        } catch (RuntimeException ex) {
            LOGGER.warn("{}", schema);
            LOGGER.error(ex.getLocalizedMessage(), ex);
            return Option.of(new MpiServiceError.GeneralError("Create Schema Error"));
        }
    }

    private record InsertEntityResult(String entityUid, String sourceUid) {
    }

}
