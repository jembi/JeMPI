package org.jembi.jempi.libmpi;

import io.vavr.control.Either;
import io.vavr.control.Option;
import org.jembi.jempi.shared.models.CustomDemographicData;
import org.jembi.jempi.shared.models.CustomGoldenRecord;
import org.jembi.jempi.shared.models.CustomPatient;
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

   List<CustomGoldenRecord> getCandidates(
         final CustomDemographicData patient,
         boolean applyDeterministicFilter);

   List<MpiExpandedGoldenRecord> getMpiExpandedGoldenRecordList(final List<String> idList);

   List<MpiExpandedPatient> getMpiExpandedPatients(final List<String> idList);

   List<String> getGoldenIdListByPredicate(
         final String predicate,
         final String val);

   CustomGoldenRecord getGoldenRecordByUid(final String uid);

   CustomPatient getPatient(final String uid);

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

   LibMPIPaginatedResultSet<CustomPatient> simpleSearchPatientRecords(
         List<SimpleSearchRequestPayload.SearchParameter> params,
         Integer offset,
         Integer limit,
         String sortBy,
         Boolean sortAsc);

   LibMPIPaginatedResultSet<CustomPatient> customSearchPatientRecords(
         List<SimpleSearchRequestPayload> params,
         Integer offset,
         Integer limit,
         String sortBy,
         Boolean sortAsc);


   long countGoldenRecords();

   long countPatients();

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
         final CustomPatient patient,
         final GoldenUIDScore goldenUIDScore);

   LinkInfo createPatientAndLinkToClonedGoldenRecord(
         final CustomPatient patient,
         float score);

   record GoldenUIDScore(
         String goldenUID,
         float score) {
   }

}
