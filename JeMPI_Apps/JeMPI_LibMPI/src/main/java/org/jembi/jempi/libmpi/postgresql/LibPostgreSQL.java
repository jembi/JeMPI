package org.jembi.jempi.libmpi.postgresql;

import io.vavr.control.Either;
import io.vavr.control.Option;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.libmpi.LibMPIClientInterface;
import org.jembi.jempi.libmpi.MpiGeneralError;
import org.jembi.jempi.libmpi.common.PaginatedResultSet;
import org.jembi.jempi.shared.models.*;

import java.time.LocalDateTime;
import java.util.List;

public final class LibPostgreSQL implements LibMPIClientInterface {

   private static final Logger LOGGER = LogManager.getLogger(LibPostgreSQL.class);

   public LibPostgreSQL(
         final Level level,
         final String[] host,
         final int[] port) {
      LOGGER.info("{}", "LibPostgreSQL Constructor");
      LOGGER.info("{} {}", host, port);
      PsqlQueries.connect();
      PsqlMutations.connect();
   }

   @Override
   public void connect() {
      PsqlQueries.connect();
   }

   @Override
   public Option<MpiGeneralError> dropAll() {
      LOGGER.error("LibPostgreSQL dropAll error");
      return null;
   }

   @Override
   public Option<MpiGeneralError> dropAllData() {
      LOGGER.error("LibPostgreSQL dropAllData error");
      return null;
   }

   @Override
   public Option<MpiGeneralError> createSchema() {
      LOGGER.error("LibPostgreSQL createSchema error");
      return null;
   }

   @Override
   public long countInteractions() {
      LOGGER.debug("countInteractions");
      return PsqlQueries.countInteractions();
   }

   @Override
   public long countGoldenRecords() {
      LOGGER.debug("countGoldenRecords");
      return PsqlQueries.countGoldenRecords();
   }

   @Override
   public List<SourceId> findSourceId(
         final String facility,
         final String client) {
      LOGGER.error("LibPostgreSQL findSourceId error");
      return List.of();
   }

   @Override
   public List<ExpandedSourceId> findExpandedSourceIdList(
         final String facility,
         final String client) {
      LOGGER.error("LibPostgreSQL findExpandedSourceIdList error");
      return List.of();
   }

   @Override
   public Interaction findInteraction(final String interactionID) {
      LOGGER.error("LibPostgreSQL findInteraction error");
      return null;
   }

   @Override
   public List<Interaction> findInteractions(final List<String> interactionIDs) {
      LOGGER.error("LibPostgreSQL findInteractions error");
      return List.of();
   }

   @Override
   public List<ExpandedInteraction> findExpandedInteractions(final List<String> interactionIDs) {
      LOGGER.error("LibPostgreSQL findExpandedInteractions error");
      return List.of();
   }

   @Override
   public Either<MpiGeneralError, PaginatedResultSet<GoldenRecord>> findGoldenRecords(final List<String> goldenIds) {
      LOGGER.error("LibPostgreSQL findGoldenRecords error");
      return null;
   }

   @Override
   public PaginatedResultSet<ExpandedGoldenRecord> findExpandedGoldenRecords(final List<String> goldenIds) {
      LOGGER.debug("findExpandedGoldenRecords");
      return PsqlQueries.findExpandedGoldenRecords(goldenIds);
   }

   @Override
   public List<String> findGoldenIds() {
      LOGGER.debug("findGoldenIds");
      return PsqlQueries.findGoldenIds();
   }

   @Override
   public List<String> fetchGoldenIds(
         final long offset,
         final long length) {
      LOGGER.error("LibPostgreSQL fetchGoldenIds error");
      return List.of();
   }

   @Override
   public List<GoldenRecord> findLinkCandidates(final DemographicData demographicData) {
      LOGGER.debug("findLinkCandidates");
      return PsqlQueries.findLinkCandidates(demographicData);
   }

   @Override
   public String restoreGoldenRecord(final RestoreGoldenRecords goldenRecord) {
      LOGGER.error("LibPostgreSQL restoreGoldenRecord error");
      return "";
   }

   @Override
   public List<GoldenRecord> findMatchCandidates(final DemographicData demographicData) {
      LOGGER.error("LibPostgreSQL findMatchCandidates error");
      return List.of();
   }

   @Override
   public PaginatedResultSet<ExpandedGoldenRecord> simpleSearchGoldenRecords(
         final List<ApiModels.ApiSearchParameter> params,
         final Integer offset,
         final Integer limit,
         final String sortBy,
         final Boolean sortAsc) {
      LOGGER.error("LibPostgreSQL simpleSearchGoldenRecords error");
      return null;
   }

   @Override
   public PaginatedResultSet<ExpandedGoldenRecord> customSearchGoldenRecords(
         final List<ApiModels.ApiSimpleSearchRequestPayload> params,
         final Integer offset,
         final Integer limit,
         final String sortBy,
         final Boolean sortAsc) {
      LOGGER.error("LibPostgreSQL customSearchGoldenRecords error");
      return null;
   }

   @Override
   public PaginatedResultSet<Interaction> simpleSearchInteractions(
         final List<ApiModels.ApiSearchParameter> params,
         final Integer offset,
         final Integer limit,
         final String sortBy,
         final Boolean sortAsc) {
      LOGGER.error("LibPostgreSQL simpleSearchInteractions error");
      return null;
   }

   @Override
   public PaginatedResultSet<Interaction> customSearchInteractions(
         final List<ApiModels.ApiSimpleSearchRequestPayload> params,
         final Integer offset,
         final Integer limit,
         final String sortBy,
         final Boolean sortAsc) {
      LOGGER.error("LibPostgreSQL customSearchInteractions error");
      return null;
   }

   @Override
   public LibMPIPaginatedResultSet<String> filterGids(
         final List<ApiModels.ApiSearchParameter> params,
         final LocalDateTime createdAt,
         final PaginationOptions paginationOptions) {
      LOGGER.error("LibPostgreSQL filterGids error");
      return null;
   }

   @Override
   public PaginatedGIDsWithInteractionCount filterGidsWithInteractionCount(
         final List<ApiModels.ApiSearchParameter> params,
         final LocalDateTime createdAt,
         final PaginationOptions paginationOptions) {
      LOGGER.error("LibPostgreSQL filterGidsWithInteractionCount error");
      return null;
   }

   @Override
   public Either<MpiGeneralError, PaginatedResultSet<GoldenRecord>> apiCrFindGoldenRecords(final ApiModels.ApiCrFindRequest request) {
      LOGGER.error("LibPostgreSQL apiCrFindGoldenRecords error");
      return null;
   }

   @Override
   public boolean setScore(
         final String interactionUID,
         final String goldenRecordUid,
         final Float score) {
      LOGGER.debug("Set Score");
      return PsqlMutations.setScore(interactionUID, goldenRecordUid, score);
   }

   @Override
   public boolean updateGoldenRecordField(
         final String goldenId,
         final String fieldName,
         final String value) {
      LOGGER.debug("updateGoldenRecordField");
      return PsqlMutations.updateField(goldenId, fieldName, value);
   }

   @Override
   public boolean updateGoldenRecordField(
         final String goldenId,
         final String fieldName,
         final Boolean value) {
      LOGGER.error("LibPostgreSQL updateGoldenRecordField error");
      return false;
   }

   @Override
   public boolean updateGoldenRecordField(
         final String goldenId,
         final String fieldName,
         final Double value) {
      LOGGER.error("LibPostgreSQL updateGoldenRecordField error");
      return false;
   }

   @Override
   public boolean updateGoldenRecordField(
         final String goldenId,
         final String fieldName,
         final Long value) {
      LOGGER.error("LibPostgreSQL updateGoldenRecordField error");
      return false;
   }

   @Override
   public Either<MpiGeneralError, LinkInfo> linkToNewGoldenRecord(
         final String currentGoldenId,
         final String interactionId,
         final Float score) {
      LOGGER.error("LibPostgreSQL linkToNewGoldenRecord error");
      return null;
   }

   @Override
   public Either<MpiGeneralError, LinkInfo> updateLink(
         final String goldenId,
         final String newGoldenId,
         final String interactionId,
         final Float score) {
      LOGGER.error("LibPostgreSQL updateLink error");
      return null;
   }

   @Override
   public LinkInfo createInteractionAndLinkToExistingGoldenRecord(
         final Interaction interaction,
         final GoldenIdScore goldenIdScore) {
      LOGGER.debug("createInteractionAndLinkToExistingGoldenRecord ");
      return PsqlMutations.createInteractionAndLinkToExistingGoldenRecord(interaction, goldenIdScore);
   }

   @Override
   public LinkInfo createInteractionAndLinkToClonedGoldenRecord(
         final Interaction interaction,
         final Float score) {
      LOGGER.debug("createInteractionAndLinkToClonedGoldenRecord");
      return PsqlMutations.createInteractionAndLinkToClonedGoldenRecord(interaction, score);
   }
}
