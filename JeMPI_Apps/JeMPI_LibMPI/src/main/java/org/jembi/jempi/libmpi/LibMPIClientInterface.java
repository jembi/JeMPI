package org.jembi.jempi.libmpi;

import io.vavr.control.Either;
import io.vavr.control.Option;
import org.jembi.jempi.shared.models.*;
import org.jembi.jempi.shared.utils.LibMPIPaginatedResultSet;
import org.jembi.jempi.shared.utils.SimpleSearchRequestPayload;

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

   PatientRecord getPatientRecord(String uid);

   GoldenRecord getGoldenRecord(String uid);

   List<GoldenRecord> getCandidates(
         CustomDemographicData demographicData,
         boolean applyDeterministicFilter);

   List<ExpandedPatientRecord> getExpandedPatients(List<String> idList);

   List<ExpandedGoldenRecord> getExpandedGoldenRecords(List<String> idList);

   List<String> getGoldenIdList();

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
         String uid,
         String fieldName,
         String value);

   Either<MpiGeneralError, LinkInfo> unLink(
         String goldenUID,
         String patientUID,
         float score);

   Either<MpiGeneralError, LinkInfo> updateLink(
         String goldenUID,
         String newGoldenUID,
         String patientUID,
         float score);

   LinkInfo createPatientAndLinkToExistingGoldenRecord(
         PatientRecord patientRecord,
         GoldenUIDScore goldenUIDScore);

   LinkInfo createPatientAndLinkToClonedGoldenRecord(
         PatientRecord patientRecord,
         float score);

   record GoldenUIDScore(
         String goldenUID,
         float score) {
   }

}
