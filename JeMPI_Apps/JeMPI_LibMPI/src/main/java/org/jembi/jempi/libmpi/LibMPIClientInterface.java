package org.jembi.jempi.libmpi;

import io.vavr.control.Either;
import io.vavr.control.Option;
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
   void startTransaction();

   void closeTransaction();

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

   List<CustomSourceId> findSourceId(
         String facility,
         String client);

   List<ExpandedSourceId> findExpandedSourceIdList(
         String facility,
         String client);

   Interaction findInteraction(String interactionID);

   List<Interaction> findInteractions(List<String> interactionIDs);

   List<ExpandedInteraction> findExpandedInteractions(List<String> interactionIDs);

   Either<MpiGeneralError, List<GoldenRecord>> findGoldenRecords(List<String> goldenIds);

   List<ExpandedGoldenRecord> findExpandedGoldenRecords(List<String> goldenIds);

   List<String> findGoldenIds();

   List<String> fetchGoldenIds(
         long offset,
         long length);

   List<GoldenRecord> findLinkCandidates(CustomDemographicData demographicData);

   List<GoldenRecord> findMatchCandidates(CustomDemographicData demographicData);

   LibMPIPaginatedResultSet<ExpandedGoldenRecord> simpleSearchGoldenRecords(
         List<ApiModels.ApiSearchParameter> params,
         Integer offset,
         Integer limit,
         String sortBy,
         Boolean sortAsc);

   LibMPIPaginatedResultSet<ExpandedGoldenRecord> customSearchGoldenRecords(
         List<ApiModels.ApiSimpleSearchRequestPayload> params,
         Integer offset,
         Integer limit,
         String sortBy,
         Boolean sortAsc);

   LibMPIPaginatedResultSet<Interaction> simpleSearchInteractions(
         List<ApiModels.ApiSearchParameter> params,
         Integer offset,
         Integer limit,
         String sortBy,
         Boolean sortAsc);

   LibMPIPaginatedResultSet<Interaction> customSearchInteractions(
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

   Either<MpiGeneralError, List<GoldenRecord>> apiCrFindGoldenRecords(ApiModels.ApiCrFindRequest request);

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
         float score);

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
         float score);

   Either<MpiGeneralError, LinkInfo> updateLink(
         String goldenId,
         String newGoldenId,
         String interactionId,
         float score);

   LinkInfo createInteractionAndLinkToExistingGoldenRecord(
         Interaction interaction,
         GoldenIdScore goldenIdScore);

   LinkInfo createInteractionAndLinkToClonedGoldenRecord(
         Interaction interaction,
         float score);

   record GoldenIdScore(
         String goldenId,
         float score) {
   }

}
