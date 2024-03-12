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

   public List<ExpandedInteraction> findExpandedInteractions(final List<String> interactionIds) {
      final var list = DgraphQueries.findExpandedInteractions(interactionIds);
      return list.stream().map(CustomDgraphExpandedInteraction::toExpandedInteraction).toList();
   }

   public GoldenRecord findGoldenRecord(final String goldenId) {
      final var rec = DgraphQueries.findDgraphGoldenRecord(goldenId);
      if (rec == null) {
         return null;
      }
      return rec.toGoldenRecord();
   }

   public List<GoldenRecord> findGoldenRecords(final List<String> ids) {
      final var list = DgraphQueries.findGoldenRecords(ids);
      return list.stream().map(CustomDgraphGoldenRecord::toGoldenRecord).toList();
   }

   public List<ExpandedGoldenRecord> findExpandedGoldenRecords(final List<String> goldenIds) {
      final var list = DgraphQueries.getExpandedGoldenRecords(goldenIds);
      return list.stream().map(CustomDgraphExpandedGoldenRecord::toExpandedGoldenRecord).toList();
   }

   public List<String> findGoldenIds() {
      return DgraphQueries.getGoldenIds();
   }

   public List<String> fetchGoldenIds(
         final long offset,
         final long length) {
      return DgraphQueries.fetchGoldenIds(offset, length);
   }

   public List<GoldenRecord> findLinkCandidates(final CustomDemographicData demographicData) {
      final var candidates = CustomDgraphQueries.findLinkCandidates(demographicData);
      return candidates.stream().map(CustomDgraphGoldenRecord::toGoldenRecord).toList();
   }

   public List<GoldenRecord> findMatchCandidates(final CustomDemographicData demographicData) {
      final var candidates = CustomDgraphQueries.findMatchCandidates(demographicData);
      return candidates.stream().map(CustomDgraphGoldenRecord::toGoldenRecord).toList();
   }

   public List<GoldenRecord> findGoldenRecords(final ApiModels.ApiCrFindRequest request) {
      final var goldenRecords = DgraphQueries.findGoldenRecords(request);
      return goldenRecords.all().stream().map(CustomDgraphGoldenRecord::toGoldenRecord).toList();
   }

   private LibMPIPaginatedResultSet<ExpandedGoldenRecord> paginatedExpandedGoldenRecords(
         final DgraphExpandedGoldenRecords list) {
      if (list == null) {
         return null;
      }
      final var data = list.all().stream().map(CustomDgraphExpandedGoldenRecord::toExpandedGoldenRecord).toList();
      final var pagination = list.pagination().get(0);
      return new LibMPIPaginatedResultSet<>(data, pagination);
   }

   private LibMPIPaginatedResultSet<Interaction> paginatedInteractions(final DgraphInteractions list) {
      if (list == null) {
         return null;
      }
      final var data = list.all().stream().map(CustomDgraphInteraction::toInteraction).toList();
      final var pagination = list.pagination().get(0);
      return new LibMPIPaginatedResultSet<>(data, pagination);
   }

   private LibMPIPaginatedResultSet<String> paginatedGids(final DgraphPaginatedUidList list) {
      if (list == null) {
         return null;
      }
      final var data = list.all().stream().map(DgraphUid::uid).toList();
      final var pagination = list.pagination().get(0);
      return new LibMPIPaginatedResultSet<>(data, pagination);
   }

   private PaginatedGIDsWithInteractionCount paginatedGidsWithInteractionCount(final DgraphPaginationUidListWithInteractionCount list) {
      if (list == null) {
         return null;
      }
      final var data = list.all().stream().map(DgraphUid::uid).toList();
      final var pagination = list.pagination().get(0);
      final var interactionCount = list.interactionCount().get(0);
      return new PaginatedGIDsWithInteractionCount(data, pagination, interactionCount);
   }

   public boolean setScore(
         final String interactionUID,
         final String goldenRecordUid,
         final float score) {
      return dgraphMutations.setScore(interactionUID, goldenRecordUid, score);
   }

   public LibMPIPaginatedResultSet<ExpandedGoldenRecord> simpleSearchGoldenRecords(
         final List<ApiModels.ApiSearchParameter> params,
         final Integer offset,
         final Integer limit,
         final String sortBy,
         final Boolean sortAsc) {
      final var list = DgraphQueries.simpleSearchGoldenRecords(params, offset, limit, sortBy, sortAsc);
      return paginatedExpandedGoldenRecords(list);
   }

   public LibMPIPaginatedResultSet<ExpandedGoldenRecord> customSearchGoldenRecords(
         final List<ApiModels.ApiSimpleSearchRequestPayload> params,
         final Integer offset,
         final Integer limit,
         final String sortBy,
         final Boolean sortAsc) {
      final var list = DgraphQueries.customSearchGoldenRecords(params, offset, limit, sortBy, sortAsc);
      return paginatedExpandedGoldenRecords(list);
   }

   public LibMPIPaginatedResultSet<Interaction> simpleSearchInteractions(
         final List<ApiModels.ApiSearchParameter> params,
         final Integer offset,
         final Integer limit,
         final String sortBy,
         final Boolean sortAsc) {
      final var list = DgraphQueries.simpleSearchInteractions(params, offset, limit, sortBy, sortAsc);
      return paginatedInteractions(list);
   }

   public LibMPIPaginatedResultSet<Interaction> customSearchInteractions(
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
         final float score) {
      return dgraphMutations.linkToNewGoldenRecord(goldenUID, interactionUID, score);
   }

   public Either<MpiGeneralError, LinkInfo> updateLink(
         final String goldenUID,
         final String newGoldenUID,
         final String interactionUID,
         final float score) {
      return dgraphMutations.updateLink(goldenUID, newGoldenUID, interactionUID, score);
   }

   public LinkInfo createInteractionAndLinkToExistingGoldenRecord(
         final Interaction interaction,
         final GoldenIdScore goldenIdScore) {
      return dgraphMutations.linkDGraphInteraction(interaction, goldenIdScore);
   }

   public LinkInfo createInteractionAndLinkToClonedGoldenRecord(
         final Interaction interaction,
         final float score) {
      return dgraphMutations.addNewDGraphInteraction(interaction);
   }

   @Override
   public Option<MpiGeneralError> deleteAllIndexes() {
      return dgraphMutations.deleteAllIndexes();
   }

   @Override
   public Option<MpiGeneralError> loadLinkingIndexes() {
      return dgraphMutations.loadLinkingIndexes();
   }

   @Override
   public Option<MpiGeneralError> loadDefaultIndexes() {
      return dgraphMutations.loadDefaultIndexes();
   }

   @Override
   public Boolean shouldUpdateLinkingIndexes() {
      return dgraphMutations.shouldUpdateLinkingIndexes();
   }

   public void startTransaction() {
      DgraphClient.getInstance().startTransaction();
   }

   public void closeTransaction() {
      DgraphClient.getInstance().closeTransaction();
   }

   /*
    * *******************************************************
    * DATABASE
    * *******************************************************
    */

   public Option<MpiGeneralError> dropAll() {
      try {
         DgraphClient.getInstance().alter(DgraphProto.Operation.newBuilder().setDropAll(true).build());
         return Option.none();
      } catch (RuntimeException e) {
         LOGGER.error(e.getMessage(), e);
         return Option.of(new MpiServiceError.GeneralError("Drop All Error"));
      }
   }

   public Option<MpiGeneralError> dropAllData() {
      try {
         DgraphClient.getInstance().alter(DgraphProto.Operation.newBuilder().setDropOp(DATA).build());
         return Option.none();
      } catch (RuntimeException e) {
         LOGGER.error(e.getMessage());
         return Option.of(new MpiServiceError.GeneralError("Drop All Data Error"));
      }
   }

   public Option<MpiGeneralError> createSchema() {
      return dgraphMutations.createSchema();
   }

}
