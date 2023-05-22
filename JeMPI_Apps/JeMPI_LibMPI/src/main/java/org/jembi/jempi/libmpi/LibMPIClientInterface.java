package org.jembi.jempi.libmpi;

import io.vavr.control.Either;
import io.vavr.control.Option;
import org.jembi.jempi.shared.models.*;
import org.jembi.jempi.shared.models.LibMPIPaginatedResultSet;
import org.jembi.jempi.shared.models.SimpleSearchRequestPayload;

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

   Interaction findInteraction(String patientId);

   List<Interaction> findInteractions(List<String> patientIds);

   List<ExpandedInteraction> findExpandedInteractions(List<String> patientIds);

   GoldenRecord findGoldenRecord(String goldenId);

   List<GoldenRecord> findGoldenRecords(List<String> goldenIds);

   List<ExpandedGoldenRecord> findExpandedGoldenRecords(List<String> goldenIds);

   List<String> findGoldenIds();

   List<GoldenRecord> findCandidates(
         CustomDemographicData demographicData,
         boolean applyDeterministicFilter);

   LibMPIPaginatedResultSet<ExpandedGoldenRecord> simpleSearchGoldenRecords(
         List<SimpleSearchRequestPayload.SearchParameter> params,
         Integer offset,
         Integer limit,
         String sortBy,
         Boolean sortAsc);

   LibMPIPaginatedResultSet<ExpandedGoldenRecord> customSearchGoldenRecords(
         List<SimpleSearchRequestPayload> params,
         Integer offset,
         Integer limit,
         String sortBy,
         Boolean sortAsc);

   LibMPIPaginatedResultSet<Interaction> simpleSearchInteractions(
         List<SimpleSearchRequestPayload.SearchParameter> params,
         Integer offset,
         Integer limit,
         String sortBy,
         Boolean sortAsc);

   LibMPIPaginatedResultSet<Interaction> customSearchInteractions(
         List<SimpleSearchRequestPayload> params,
         Integer offset,
         Integer limit,
         String sortBy,
         Boolean sortAsc);

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

   Either<MpiGeneralError, LinkInfo> linkToNewGoldenRecord(
         String currentGoldenId,
         String interactionId,
         float score);

   Either<MpiGeneralError, LinkInfo> updateLink(
         String goldenId,
         String newGoldenId,
         String interactionId,
         float score);

   LinkInfo createPatientAndLinkToExistingGoldenRecord(
         Interaction interaction,
         GoldenIdScore goldenIdScore);

   LinkInfo createPatientAndLinkToClonedGoldenRecord(
         Interaction interaction,
         float score);

   record GoldenIdScore(
         String goldenId,
         float score) {
   }

}
