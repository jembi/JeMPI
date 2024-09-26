package org.jembi.jempi.libmpi;

import io.vavr.control.Either;
import io.vavr.control.Option;
import org.jembi.jempi.libmpi.common.PaginatedResultSet;
import org.jembi.jempi.shared.models.*;

import java.time.LocalDateTime;
import java.util.List;

public interface LibMPIClientInterface {

   /*
    * *****************************************************************************
    * *
    * Database
    * *****************************************************************************
    * *
    */
   void connect();

   Option<MpiGeneralError> dropAll();

   Option<MpiGeneralError> dropAllData();

   Option<MpiGeneralError> createSchema();

   /*
    * *****************************************************************************
    * *
    * Queries
    * *****************************************************************************
    * *
    */

   long countInteractions();

   long countGoldenRecords();

   List<SourceId> findSourceId(
         String facility,
         String client);

   List<ExpandedSourceId> findExpandedSourceIdList(
         String facility,
         String client);

   Interaction findInteraction(String interactionID);

   List<Interaction> findInteractions(List<String> interactionIDs);

   List<ExpandedInteraction> findExpandedInteractions(List<String> interactionIDs);

   Either<MpiGeneralError, PaginatedResultSet<GoldenRecord>> findGoldenRecords(List<String> goldenIds);

   PaginatedResultSet<ExpandedGoldenRecord> findExpandedGoldenRecords(List<String> goldenIds);

   String getFieldCount(ApiModels.CountFields countFields);

   List<String> findGoldenIds();

   List<String> fetchGoldenIds(
         long offset,
         long length);

   List<GoldenRecord> findLinkCandidates(DemographicData demographicData);

   String restoreGoldenRecord(RestoreGoldenRecords goldenRecord);

   List<GoldenRecord> findMatchCandidates(DemographicData demographicData);

   PaginatedResultSet<ExpandedGoldenRecord> simpleSearchGoldenRecords(
         List<ApiModels.ApiSearchParameter> params,
         Integer offset,
         Integer limit,
         String sortBy,
         Boolean sortAsc);

   PaginatedResultSet<ExpandedGoldenRecord> customSearchGoldenRecords(
         List<ApiModels.ApiSimpleSearchRequestPayload> params,
         Integer offset,
         Integer limit,
         String sortBy,
         Boolean sortAsc);

   PaginatedResultSet<Interaction> simpleSearchInteractions(
         List<ApiModels.ApiSearchParameter> params,
         Integer offset,
         Integer limit,
         String sortBy,
         Boolean sortAsc);

   PaginatedResultSet<Interaction> customSearchInteractions(
         List<ApiModels.ApiSimpleSearchRequestPayload> params,
         Integer offset,
         Integer limit,
         String sortBy,
         Boolean sortAsc);

   LibMPIPaginatedResultSet<String> filterGids(
         List<ApiModels.ApiSearchParameter> params,
         LocalDateTime createdAt,
         PaginationOptions paginationOptions);

   PaginatedGIDsWithInteractionCount filterGidsWithInteractionCount(
         List<ApiModels.ApiSearchParameter> params,
         LocalDateTime createdAt,
         PaginationOptions paginationOptions);

   Either<MpiGeneralError, PaginatedResultSet<GoldenRecord>> apiCrFindGoldenRecords(ApiModels.ApiCrFindRequest request);

   /*
    * *****************************************************************************
    * *
    * Mutations
    * *****************************************************************************
    * *
    */
   boolean setScore(
         String interactionUID,
         String goldenRecordUid,
         Float score);

   boolean updateGoldenRecordField(
         String goldenId,
         String fieldName,
         String value);

   boolean updateGoldenRecordField(
         String goldenId,
         String fieldName,
         Boolean value);

   boolean updateGoldenRecordField(
         String goldenId,
         String fieldName,
         Double value);

   boolean updateGoldenRecordField(
         String goldenId,
         String fieldName,
         Long value);

   Either<MpiGeneralError, LinkInfo> linkToNewGoldenRecord(
         String currentGoldenId,
         String interactionId,
         Float score);

   Either<MpiGeneralError, LinkInfo> updateLink(
         String goldenId,
         String newGoldenId,
         String interactionId,
         Float score);

   LinkInfo createInteractionAndLinkToExistingGoldenRecord(
         Interaction interaction,
         GoldenIdScore goldenIdScore);

   LinkInfo createInteractionAndLinkToClonedGoldenRecord(
         Interaction interaction,
         Float score);

   record GoldenIdScore(
         String goldenId,
         Float score) {
   }

}
