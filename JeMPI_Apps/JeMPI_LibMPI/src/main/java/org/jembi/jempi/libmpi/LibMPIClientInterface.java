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

   PatientRecord getPatientRecord(final String uid);

   GoldenRecord getGoldenRecord(final String uid);

   List<GoldenRecord> getCandidates(
         final CustomDemographicData demographicData,
         boolean applyDeterministicFilter);

   List<ExpandedPatientRecord> getExpandedPatients(final List<String> ids);

   List<ExpandedGoldenRecord> getExpandedGoldenRecords(final List<String> ids);

   List<String> getGoldenIds();

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
         final String uid,
         final String fieldName,
         final String value);

   Either<MpiGeneralError, LinkInfo> unLink(
         final String goldenUID,
         final String patientUID,
         final float score);

   Either<MpiGeneralError, LinkInfo> updateLink(
         final String goldenUID,
         final String newGoldenUID,
         final String patientUID,
         final float score);

   LinkInfo createPatientAndLinkToExistingGoldenRecord(
         final PatientRecord patientRecord,
         final GoldenUIDScore goldenUIDScore);

   LinkInfo createPatientAndLinkToClonedGoldenRecord(
         final PatientRecord patientRecord,
         float score);

   record GoldenUIDScore(
         String goldenUID,
         float score) {
   }

}
