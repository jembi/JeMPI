package org.jembi.jempi.libmpi.postgresql;

import io.vavr.control.Either;
import io.vavr.control.Option;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.libmpi.LibMPIClientInterface;
import org.jembi.jempi.libmpi.MpiGeneralError;
import org.jembi.jempi.libmpi.MpiServiceError;
import org.jembi.jempi.shared.models.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public final class LibPostgresql implements LibMPIClientInterface {

   private static final Logger LOGGER = LogManager.getLogger(LibPostgresql.class);

   public LibPostgresql(
         final String URL,
         final String USR,
         final String PSW) {
      LOGGER.info("{}", "LibPostgresql Constructor");
      PostgresqlClient.getInstance().config(URL, USR, PSW);
   }

   /*
    * *******************************************************
    * QUERIES
    * *******************************************************
    *
    */

   public long countInteractions() {
      return PostgresqlQueries.countInteractions();
   }

   public long countGoldenRecords() {
      return PostgresqlQueries.countGoldenRecords();
   }

   public Interaction findInteraction(final String interactionId) {
      final var interaction = PostgresqlQueries.getInteraction(UUID.fromString(interactionId));
      final var sourceId = PostgresqlQueries.getInteractionSourceIds(interaction.uid());
      return new Interaction(interaction.uid().toString(),
                             new CustomSourceId(sourceId.get(0).id().toString(),
                                                sourceId.get(0).data().facility(),
                                                sourceId.get(0).data().patient()),
                             null,
                             interaction.data());
   }

   public List<Interaction> findInteractions(final List<String> interactionIds) {
      return interactionIds.stream().map(this::findInteraction).toList();
   }

   private ExpandedInteraction findExpandedInteraction(final String eid) {
      final var interaction = findInteraction(eid);
      final var goldenRecord = PostgresqlQueries.getGoldenRecordsOfInteraction(UUID.fromString(eid)).get(0);
      return new ExpandedInteraction(interaction,
                                     List.of(new GoldenRecordWithScore(new GoldenRecord(goldenRecord.uid().toString(),
                                                                                        PostgresqlQueries.getGoldenRecordSourceIds(
                                                                                                               goldenRecord.uid())
                                                                                                         .stream()
                                                                                                         .map(x -> new CustomSourceId(
                                                                                                               x.id().toString(),
                                                                                                               x.data()
                                                                                                                .facility(),
                                                                                                               x.data()
                                                                                                                .patient()))
                                                                                                         .toList(),
                                                                                        new CustomUniqueGoldenRecordData(
                                                                                              LocalDateTime.now(),
                                                                                              true,
                                                                                              interaction.uniqueInteractionData()
                                                                                                         .auxId()),
                                                                                        goldenRecord.data()),
                                                                       PostgresqlQueries.getScore(goldenRecord.uid(),
                                                                                                  UUID.fromString(eid)))));
   }

   public List<ExpandedInteraction> findExpandedInteractions(final List<String> interactionIds) {
      return interactionIds.stream().map(this::findExpandedInteraction).toList();
   }

   public GoldenRecord findGoldenRecord(final String goldenId) {
      final var goldenRecord = PostgresqlQueries.getGoldenRecord(UUID.fromString(goldenId));
      final var sourceIds = PostgresqlQueries.getGoldenRecordSourceIds(UUID.fromString(goldenId));
      return new GoldenRecord(goldenId,
                              sourceIds.stream()
                                       .map(x -> new CustomSourceId(x.id().toString(), x.data().facility(), x.data().patient()))
                                       .toList(),
                              new CustomUniqueGoldenRecordData(LocalDateTime.now(), true, "AUX_ID"),
                              goldenRecord.data());
   }

   public List<GoldenRecord> findGoldenRecords(final List<String> ids) {
      return ids.stream().map(this::findGoldenRecord).toList();
   }

   public List<ExpandedGoldenRecord> findExpandedGoldenRecords(final List<String> goldenIds) {
      return goldenIds.stream().map(gid -> {
         final var gidUUID = UUID.fromString(gid);
         final var goldenRecord = findGoldenRecord(gid);
         final var interactions = PostgresqlQueries.getGoldenRecordInteractions(gidUUID);
         return new ExpandedGoldenRecord(goldenRecord, interactions.stream().map(e -> {
            final var sid = PostgresqlQueries.getInteractionSourceIds(e.uid()).get(0);
            final var score = PostgresqlQueries.getScore(gidUUID, e.uid());
            return new InteractionWithScore(new Interaction(e.uid().toString(),
                                                            new CustomSourceId(sid.id().toString(),
                                                                               sid.data().facility(),
                                                                               sid.data().patient()),
                                                            null,
                                                            e.data()), score);
         }).toList());
      }).toList();
   }

   public List<String> findGoldenIds() {
      return PostgresqlQueries.getGoldenIds().stream().map(UUID::toString).toList();
   }

   public List<String> fetchGoldenIds(
         final long offset,
         final long length) {
      LOGGER.error("Not implemented");
      return Collections.emptyList();
   }

   public List<GoldenRecord> findCandidates(final CustomDemographicData demographicData) {
      return PostgresqlQueries.findCandidates(demographicData);
   }

   public List<GoldenRecord> findGoldenRecords(final ApiModels.ApiCrFindRequest request) {
      LOGGER.error("Not implemented");
      return null;
   }

   public LibMPIPaginatedResultSet<ExpandedGoldenRecord> simpleSearchGoldenRecords(
         final List<ApiModels.ApiSearchParameter> params,
         final Integer offset,
         final Integer limit,
         final String sortBy,
         final Boolean sortAsc) {
      LOGGER.error("Not implemented");
      return null;
   }

   public LibMPIPaginatedResultSet<ExpandedGoldenRecord> customSearchGoldenRecords(
         final List<ApiModels.ApiSimpleSearchRequestPayload> params,
         final Integer offset,
         final Integer limit,
         final String sortBy,
         final Boolean sortAsc) {
      LOGGER.error("Not implemented");
      return null;
   }

   public LibMPIPaginatedResultSet<Interaction> simpleSearchInteractions(
         final List<ApiModels.ApiSearchParameter> params,
         final Integer offset,
         final Integer limit,
         final String sortBy,
         final Boolean sortAsc) {
      LOGGER.error("Not implemented");
      return null;
   }

   public LibMPIPaginatedResultSet<Interaction> customSearchInteractions(
         final List<ApiModels.ApiSimpleSearchRequestPayload> params,
         final Integer offset,
         final Integer limit,
         final String sortBy,
         final Boolean sortAsc) {
      LOGGER.error("Not implemented");
      return null;
   }

   @Override
   public LibMPIPaginatedResultSet<String> filterGids(
         final List<ApiModels.ApiSearchParameter> params,
         final LocalDateTime createdAt,
         final PaginationOptions paginationOptions) {
      LOGGER.error("Not implemented");
      return null;
   }

   public PaginatedGIDsWithInteractionCount filterGidsWithInteractionCount(
         final List<ApiModels.ApiSearchParameter> params,
         final LocalDateTime createdAt,
         final PaginationOptions paginationOptions) {
      LOGGER.error("filterGidsWithInteractionCount Not implemented");
      return null;
   }

   /*
    * *******************************************************
    * MUTATIONS
    * *******************************************************
    */

   public boolean setScore(
         final String interactionUID,
         final String goldenRecordUid,
         final float score) {
      return PostgresqlMutations.setScore(interactionUID, goldenRecordUid, score);
   }

   public boolean updateGoldenRecordField(
         final String goldenId,
         final String fieldName,
         final String val) {
      return PostgresqlMutations.updateGoldenRecordField(goldenId, fieldName, val);
   }

   @Override
   public boolean updateGoldenRecordField(
         final String goldenId,
         final String fieldName,
         final Boolean value) {
      return false;
   }

   @Override
   public boolean updateGoldenRecordField(
         final String goldenId,
         final String fieldName,
         final Double value) {
      return false;
   }

   @Override
   public boolean updateGoldenRecordField(
         final String goldenId,
         final String fieldName,
         final Long value) {
      return false;
   }

   public Either<MpiGeneralError, LinkInfo> linkToNewGoldenRecord(
         final String goldenUID,
         final String interactionUID,
         final float score) {
      LOGGER.error("Not implemented");
      return null;
   }

   public Either<MpiGeneralError, LinkInfo> updateLink(
         final String goldenUID,
         final String newGoldenUID,
         final String interactionUID,
         final float score) {
      LOGGER.error("Not implemented");
      return null;
   }

   public LinkInfo createInteractionAndLinkToExistingGoldenRecord(
         final Interaction interaction,
         final GoldenIdScore goldenIdScore) {
      final var nodeSourceIds =
            PostgresqlQueries.findSourceId(interaction.sourceId().facility(), interaction.sourceId().patient());
      final var goldenRecordSourceIds = PostgresqlQueries.getGoldenRecordSourceIds(UUID.fromString(goldenIdScore.goldenId()));
      final var sid = nodeSourceIds.isEmpty()
            ? new NodeSourceId(interaction.sourceId().facility(), interaction.sourceId().patient()).createNode()
            : nodeSourceIds.get(0).id();
      final var eid = new NodeInteraction(interaction.demographicData()).createNode();
      Edge.createEdge(eid, sid, Edge.EdgeName.IID2SID);
      if (nodeSourceIds.isEmpty() || goldenRecordSourceIds.stream().noneMatch(p -> p.id().equals(nodeSourceIds.get(0).id()))) {
         Edge.createEdge(UUID.fromString(goldenIdScore.goldenId()), sid, Edge.EdgeName.GID2SID);
      }
      Edge.createEdge(UUID.fromString(goldenIdScore.goldenId()),
                      eid,
                      Edge.EdgeName.GID2IID,
                      new FacetScore(goldenIdScore.score()));
      return new LinkInfo(goldenIdScore.goldenId(), eid.toString(), goldenIdScore.score());
   }

   public LinkInfo createInteractionAndLinkToClonedGoldenRecord(
         final Interaction interaction,
         final float score) {
      final var sid = new NodeSourceId(interaction.sourceId().facility(), interaction.sourceId().patient()).createNode();
      final var iid = new NodeInteraction(interaction.demographicData()).createNode();
      final var gid = new NodeGoldenRecord(interaction.demographicData()).createNode();
      Edge.createEdge(iid, sid, Edge.EdgeName.IID2SID);
      Edge.createEdge(gid, sid, Edge.EdgeName.GID2SID);
      Edge.createEdge(gid, iid, Edge.EdgeName.GID2IID, new FacetScore(score));

      return new LinkInfo(gid.toString(), iid.toString(), score);
   }

   public void startTransaction() {
      PostgresqlClient.getInstance().startTransaction();
   }

   public void closeTransaction() {
      PostgresqlClient.getInstance().closeTransaction();
   }

   /*
    * *******************************************************
    * DATABASE
    * *******************************************************
    */


   public Option<MpiGeneralError> dropAll() {
      LOGGER.debug("{}", "drop all");
      startTransaction();
      if (!PostgresqlMutations.dropAll()) {
         return Option.of(new MpiServiceError.GeneralError("Drop All Error"));
      }
      return Option.of(null);
   }

   public Option<MpiGeneralError> dropAllData() {
      return Option.of(new MpiServiceError.GeneralError("Drop All Data Error"));
   }

   public Option<MpiGeneralError> createSchema() {
      LOGGER.debug("{}", "create schema");
      startTransaction();
      if (!PostgresqlMutations.createSchema()) {
         return Option.of(new MpiServiceError.GeneralError("Create Schema"));
      } else {
         return Option.of(null);
      }
   }


}
