package org.jembi.jempi.libmpi;

import io.vavr.control.Either;
import io.vavr.control.Option;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.libmpi.dgraph.LibDgraph;
import org.jembi.jempi.libmpi.postgresql.LibPostgresql;
import org.jembi.jempi.shared.models.*;

import java.util.List;

public final class LibMPI {

   private static final Logger LOGGER = LogManager.getLogger(LibMPI.class);
   private final LibMPIClientInterface client;

   public LibMPI(
         final String[] host,
         final int[] port) {
      LOGGER.info("{}", "LibMPI Constructor");
      client = new LibDgraph(host, port);
   }

   public LibMPI(
         final String URL,
         final String USR,
         final String PSW) {
      LOGGER.info("{}", "LibMPI Constructor");
      client = new LibPostgresql(URL, USR, PSW);
   }


   /*
    * *****************************************************************************
    * *
    * Database
    * *****************************************************************************
    * *
    */

   public void startTransaction() {
      client.startTransaction();
   }

   public void closeTransaction() {
      client.closeTransaction();
   }

   public Option<MpiGeneralError> dropAll() {
      return client.dropAll();
   }

   public Option<MpiGeneralError> dropAllData() {
      return client.dropAllData();
   }

   public Option<MpiGeneralError> createSchema() {
      return client.createSchema();
   }

   /*
    * *****************************************************************************
    * *
    * Queries
    * *****************************************************************************
    * *
    */

   public long countInteractions() {
      return client.countInteractions();
   }

   public long countGoldenRecords() {
      return client.countGoldenRecords();
   }


   public Interaction findInteraction(final String interactionID) {
      return client.findInteraction(interactionID);
   }

   public List<Interaction> findInteraction(final List<String> interactionIDs) {
      return client.findInteractions(interactionIDs);
   }

   public List<ExpandedInteraction> findExpandedInteractions(final List<String> interactionIDs) {
      return client.findExpandedInteractions(interactionIDs);
   }

   public GoldenRecord findGoldenRecord(final String goldenId) {
      return client.findGoldenRecord(goldenId);
   }

   public List<GoldenRecord> findGoldenRecords(final List<String> goldenIds) {
      return client.findGoldenRecords(goldenIds);
   }

   public ExpandedGoldenRecord findExpandedGoldenRecord(final String goldenId) {
      final var records = client.findExpandedGoldenRecords(List.of(goldenId));
      if (!records.isEmpty()) {
         return records.get(0);
      }
      return null;
   }

   public List<ExpandedGoldenRecord> findExpandedGoldenRecords(final List<String> goldenIds) {
      return client.findExpandedGoldenRecords(goldenIds);
   }

   public List<String> findGoldenIds() {
      return client.findGoldenIds();
   }

   public List<GoldenRecord> findCandidates(final CustomDemographicData demographicData) {
      return client.findCandidates(demographicData);
   }

   public LibMPIPaginatedResultSet<ExpandedGoldenRecord> simpleSearchGoldenRecords(
         final List<SimpleSearchRequestPayload.SearchParameter> params,
         final Integer offset,
         final Integer limit,
         final String sortBy,
         final Boolean sortAsc) {
      return client.simpleSearchGoldenRecords(params, offset, limit, sortBy, sortAsc);
   }

   public LibMPIPaginatedResultSet<ExpandedGoldenRecord> customSearchGoldenRecords(
         final List<SimpleSearchRequestPayload> params,
         final Integer offset,
         final Integer limit,
         final String sortBy,
         final Boolean sortAsc) {
      return client.customSearchGoldenRecords(params, offset, limit, sortBy, sortAsc);
   }

   public LibMPIPaginatedResultSet<Interaction> simpleSearchInteractions(
         final List<SimpleSearchRequestPayload.SearchParameter> params,
         final Integer offset,
         final Integer limit,
         final String sortBy,
         final Boolean sortAsc) {
      return client.simpleSearchInteractions(params, offset, limit, sortBy, sortAsc);
   }

   public LibMPIPaginatedResultSet<Interaction> customSearchInteractions(
         final List<SimpleSearchRequestPayload> params,
         final Integer offset,
         final Integer limit,
         final String sortBy,
         final Boolean sortAsc) {
      return client.customSearchInteractions(params, offset, limit, sortBy, sortAsc);
   }

   /*
    * *****************************************************************************
    * *
    * Mutations
    * *****************************************************************************
    * *
    */

   public boolean setScore(
         final String interactionID,
         final String goldenID,
         final float score) {
      return client.setScore(interactionID, goldenID, score);
   }

   public boolean updateGoldenRecordField(
         final String goldenId,
         final String fieldName,
         final String value) {
      return client.updateGoldenRecordField(goldenId, fieldName, value);
   }

   public Either<MpiGeneralError, LinkInfo> linkToNewGoldenRecord(
         final String currentGoldenId,
         final String interactionId,
         final float score) {
      return client.linkToNewGoldenRecord(currentGoldenId, interactionId, score);
   }

   public Either<MpiGeneralError, LinkInfo> updateLink(
         final String goldenID,
         final String newGoldenID,
         final String interactionID,
         final float score) {
      return client.updateLink(goldenID, newGoldenID, interactionID, score);
   }

   public LinkInfo createInteractionAndLinkToExistingGoldenRecord(
         final Interaction interaction,
         final LibMPIClientInterface.GoldenIdScore goldenIdScore) {
      return client.createInteractionAndLinkToExistingGoldenRecord(interaction, goldenIdScore);
   }

   public LinkInfo createInteractionAndLinkToClonedGoldenRecord(
         final Interaction interaction,
         final float score) {
      return client.createInteractionAndLinkToClonedGoldenRecord(interaction, score);
   }

}
