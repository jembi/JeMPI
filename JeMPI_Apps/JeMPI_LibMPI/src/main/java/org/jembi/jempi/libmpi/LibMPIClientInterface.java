package org.jembi.jempi.libmpi;

import io.vavr.control.Either;
import io.vavr.control.Option;
import org.jembi.jempi.shared.models.CustomDemographicData;
import org.jembi.jempi.shared.models.GoldenRecord;
import org.jembi.jempi.shared.models.PatientRecord;
import org.jembi.jempi.shared.models.LinkInfo;
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
         final CustomDemographicData patient,
         boolean applyDeterministicFilter);

   List<MpiExpandedGoldenRecord> getMpiExpandedGoldenRecordList(final List<String> idList);

   List<MpiExpandedPatientRecord> getMpiExpandedPatients(final List<String> idList);

   List<String> getGoldenIdListByPredicate(
         final String predicate,
         final String val);


   List<String> getGoldenIdList();

   LibMPIPaginatedResultSet<MpiExpandedGoldenRecord> simpleSearchGoldenRecords(
         List<SimpleSearchRequestPayload.SearchParameter> params,
         Integer offset,
         Integer limit,
         String sortBy,
         Boolean sortAsc);

   LibMPIPaginatedResultSet<MpiExpandedGoldenRecord> customSearchGoldenRecords(
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
