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

   long countPatientRecords();

   long countGoldenRecords();


   PatientRecord getPatientRecord(String patientId);

   List<PatientRecord> getPatientRecords(List<String> patientIds);

   List<ExpandedPatientRecord> getExpandedPatientRecords(List<String> patientIds);

   GoldenRecord getGoldenRecord(String goldenId);

   List<GoldenRecord> getGoldenRecords(List<String> goldenIds);

   List<ExpandedGoldenRecord> getExpandedGoldenRecords(List<String> goldenIds);

   List<String> getGoldenIds();

   List<GoldenRecord> getCandidates(
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

   LibMPIPaginatedResultSet<PatientRecord> simpleSearchPatientRecords(
         List<SimpleSearchRequestPayload.SearchParameter> params,
         Integer offset,
         Integer limit,
         String sortBy,
         Boolean sortAsc);

   LibMPIPaginatedResultSet<PatientRecord> customSearchPatientRecords(
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

   boolean updateGoldenRecordField(
         String goldenId,
         String fieldName,
         String value);

   Either<MpiGeneralError, LinkInfo> unLink(
         String goldenId,
         String patientId,
         float score);

   Either<MpiGeneralError, LinkInfo> updateLink(
         String goldenId,
         String newGoldenId,
         String patientId,
         float score);

   LinkInfo createPatientAndLinkToExistingGoldenRecord(
         PatientRecord patientRecord,
         GoldenIdScore goldenIdScore);

   LinkInfo createPatientAndLinkToClonedGoldenRecord(
         PatientRecord patientRecord,
         float score);

   record GoldenIdScore(
         String goldenId,
         float score) {
   }

}
