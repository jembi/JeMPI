package org.jembi.jempi.libmpi.dgraph;

import io.dgraph.DgraphProto;
import io.vavr.control.Either;
import io.vavr.control.Option;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.libmpi.LibMPIClientInterface;
import org.jembi.jempi.libmpi.MpiGeneralError;
import org.jembi.jempi.libmpi.MpiServiceError;
import org.jembi.jempi.libmpi.common.PaginatedResultSet;
import org.jembi.jempi.shared.models.*;

import java.time.LocalDateTime;
import java.util.List;

import static io.dgraph.DgraphProto.Operation.DropOp.DATA;

public final class LibDgraph implements LibMPIClientInterface {

   private static final Logger LOGGER = LogManager.getLogger(LibDgraph.class);

   private final DgraphMutations dgraphMutations;

   public LibDgraph(
         final Level level,
         final String[] host,
         final int[] port) {
      LOGGER.info("{}", "LibDgraph Constructor");
      LOGGER.info("{} {}", host, port);
      dgraphMutations = new DgraphMutations(level);
      DgraphClient.getInstance().config(host, port);
   }

   /*
    * *******************************************************
    * QUERIES
    * *******************************************************
    *
    */

   public long countInteractions() {
      return DgraphQueries.countInteractions();
   }

   public long countGoldenRecords() {
      return DgraphQueries.countGoldenRecords();
   }

   public Interaction findInteraction(final String interactionId) {
      return DgraphQueries.findInteraction(interactionId);
   }

   public List<Interaction> findInteractions(final List<String> interactionIds) {
      return List.of();
   }

   public List<SourceId> findSourceId(
         final String facility,
         final String patient) {
      return DgraphQueries.findSourceIdList(facility, patient);
   }

   public List<ExpandedSourceId> findExpandedSourceIdList(
         final String facility,
         final String patient) {
      return DgraphQueries.findExpandedSourceIdList(facility, patient);
   }

   public List<ExpandedInteraction> findExpandedInteractions(final List<String> interactionIds) {
      return DgraphQueries.findExpandedInteractions(interactionIds);
   }

   public Either<MpiGeneralError, PaginatedResultSet<GoldenRecord>> findGoldenRecords(final List<String> ids) {
      return DgraphQueries.findGoldenRecords(ids);
   }
   public PaginatedResultSet<ExpandedGoldenRecord> findExpandedGoldenRecords(final List<String> goldenIds) {
      return DgraphQueries.getExpandedGoldenRecords(goldenIds);
   }

   public String getFieldCount(final ApiModels.CountFields countFields) {
      return DgraphQueries.getFieldCount(countFields);
   }

   public long getAgeGroupCount(final String startDate, final String endDate) {
      return DgraphQueries.getAgeGroupCount(startDate, endDate);
   }

   public String restoreGoldenRecord(
           final RestoreGoldenRecords goldenRecord) {
      return dgraphMutations.restoreGoldenRecord(goldenRecord);
   }

   public List<String> findGoldenIds() {
      return DgraphQueries.getGoldenIds();
   }

   public List<String> fetchGoldenIds(
         final long offset,
         final long length) {
      return DgraphQueries.fetchGoldenIds(offset, length);
   }

   public List<GoldenRecord> findLinkCandidates(final DemographicData demographicData) {
      return DgraphQueries.findLinkCandidates(demographicData);
   }

   public List<GoldenRecord> findMatchCandidates(final DemographicData demographicData) {
      return DgraphQueries.findMatchCandidates(demographicData);
   }

   public Either<MpiGeneralError, PaginatedResultSet<GoldenRecord>> apiCrFindGoldenRecords(final ApiModels.ApiCrFindRequest request) {
      final var goldenRecords = DgraphQueries.findGoldenRecords(request);
      if (goldenRecords.isRight()) {
         return Either.right(goldenRecords.get()); // .all().stream().map(CustomDgraphGoldenRecord::toGoldenRecord).toList());
      } else {
         return Either.left(goldenRecords.getLeft());
      }
   }

   private PaginatedResultSet<ExpandedGoldenRecord> paginatedExpandedGoldenRecords(final PaginatedResultSet<ExpandedGoldenRecord> list) {
      return list;
   }

   private PaginatedResultSet<Interaction> paginatedInteractions(final PaginatedResultSet<InteractionWithScore> list) {
      return new PaginatedResultSet<>(list.data().stream().map(InteractionWithScore::interaction).toList(), list.pagination());
   }

   private LibMPIPaginatedResultSet<String> paginatedGids(final DgraphPaginatedUidList list) {
      if (list == null) {
         return null;
      }
      final var data = list.all().stream().map(DgraphUid::uid).toList();
      final var pagination = list.pagination().getFirst();
      return new LibMPIPaginatedResultSet<>(data, pagination);
   }

   private PaginatedGIDsWithInteractionCount paginatedGidsWithInteractionCount(final DgraphPaginationUidListWithInteractionCount list) {
      if (list == null) {
         return null;
      }
      final var data = list.all().stream().map(DgraphUid::uid).toList();
      final var pagination = list.pagination().getFirst();
      final var interactionCount = list.interactionCount().getFirst();
      return new PaginatedGIDsWithInteractionCount(data, pagination, interactionCount);
   }

   public PaginatedResultSet<ExpandedGoldenRecord> simpleSearchGoldenRecords(
         final List<ApiModels.ApiSearchParameter> params,
         final Integer offset,
         final Integer limit,
         final String sortBy,
         final Boolean sortAsc) {
      final var list = DgraphQueries.simpleSearchGoldenRecords(params, offset, limit, sortBy, sortAsc);
      return paginatedExpandedGoldenRecords(list);
   }

   public PaginatedResultSet<ExpandedGoldenRecord> customSearchGoldenRecords(
         final List<ApiModels.ApiSimpleSearchRequestPayload> params,
         final Integer offset,
         final Integer limit,
         final String sortBy,
         final Boolean sortAsc) {
      final var list = DgraphQueries.customSearchGoldenRecords(params, offset, limit, sortBy, sortAsc);
      return paginatedExpandedGoldenRecords(list);
   }

   public PaginatedResultSet<Interaction> simpleSearchInteractions(
         final List<ApiModels.ApiSearchParameter> params,
         final Integer offset,
         final Integer limit,
         final String sortBy,
         final Boolean sortAsc) {
      final var list = DgraphQueries.simpleSearchInteractions(params, offset, limit, sortBy, sortAsc);
      return paginatedInteractions(list);
   }

   public PaginatedResultSet<Interaction> customSearchInteractions(
         final List<ApiModels.ApiSimpleSearchRequestPayload> params,
         final Integer offset,
         final Integer limit,
         final String sortBy,
         final Boolean sortAsc) {
      final var list = DgraphQueries.customSearchInteractions(params, offset, limit, sortBy, sortAsc);
      return paginatedInteractions(list);
   }

   public LibMPIPaginatedResultSet<String> filterGids(
         final List<ApiModels.ApiSearchParameter> params,
         final LocalDateTime createdAt,
         final PaginationOptions paginationOptions) {
      final var list = DgraphQueries.filterGidsWithParams(params, createdAt, paginationOptions, false);
      return paginatedGids(list.getLeft());
   }

   public PaginatedGIDsWithInteractionCount filterGidsWithInteractionCount(
         final List<ApiModels.ApiSearchParameter> params,
         final LocalDateTime createdAt,
         final PaginationOptions paginationOptions) {
      final var list = DgraphQueries.filterGidsWithParams(params, createdAt, paginationOptions, true);
      return paginatedGidsWithInteractionCount(list.get());
   }


   /*
    * *******************************************************
    * MUTATIONS
    * *******************************************************
    */

   public boolean setScore(
         final String interactionUID,
         final String goldenRecordUid,
         final Float score) {
      return dgraphMutations.setScore(interactionUID, goldenRecordUid, score);
   }

   public boolean updateGoldenRecordField(
         final String goldenId,
         final String fieldName,
         final String val) {
      return dgraphMutations.updateGoldenRecordField(goldenId, fieldName, val);
   }

   public boolean updateGoldenRecordField(
         final String goldenId,
         final String fieldName,
         final Boolean val) {
      return dgraphMutations.updateGoldenRecordField(goldenId, fieldName, val);
   }

   public boolean updateGoldenRecordField(
         final String goldenId,
         final String fieldName,
         final Double val) {
      return dgraphMutations.updateGoldenRecordField(goldenId, fieldName, val);
   }

   public boolean updateGoldenRecordField(
         final String goldenId,
         final String fieldName,
         final Long val) {
      return dgraphMutations.updateGoldenRecordField(goldenId, fieldName, val);
   }

   public Either<MpiGeneralError, LinkInfo> linkToNewGoldenRecord(
         final String goldenUID,
         final String interactionUID,
         final Float score) {
      return dgraphMutations.linkToNewGoldenRecord(goldenUID, interactionUID, score);
   }

   public Either<MpiGeneralError, LinkInfo> updateLink(
         final String goldenUID,
         final String newGoldenUID,
         final String interactionUID,
         final Float score) {
      return dgraphMutations.updateLink(goldenUID, newGoldenUID, interactionUID, score);
   }

   public LinkInfo createInteractionAndLinkToExistingGoldenRecord(
         final Interaction interaction,
         final GoldenIdScore goldenIdScore) {
      return dgraphMutations.linkDGraphInteraction(interaction, goldenIdScore);
   }

   public LinkInfo createInteractionAndLinkToClonedGoldenRecord(
         final Interaction interaction,
         final Float score) {
      return dgraphMutations.addNewDGraphInteraction(interaction);
   }

   /*
    * *******************************************************
    * DATABASE
    * *******************************************************
    */

   public void connect() {
      DgraphClient.getInstance().connect();
   }

   public Option<MpiGeneralError> dropAll() {
      connect();
      try {
         DgraphClient.getInstance().alter(DgraphProto.Operation.newBuilder().setDropAll(true).build());
         return Option.none();
      } catch (RuntimeException e) {
         LOGGER.error(e.getMessage(), e);
         return Option.of(new MpiServiceError.GeneralError("Drop All Error"));
      }
   }

   public Option<MpiGeneralError> dropAllData() {
      connect();
      try {
         DgraphClient.getInstance().alter(DgraphProto.Operation.newBuilder().setDropOp(DATA).build());
         return Option.none();
      } catch (RuntimeException e) {
         LOGGER.error(e.getMessage());
         return Option.of(new MpiServiceError.GeneralError("Drop All Data Error"));
      }
   }

   public Option<MpiGeneralError> createSchema() {
      connect();
      return dgraphMutations.createSchema();
   }

}
