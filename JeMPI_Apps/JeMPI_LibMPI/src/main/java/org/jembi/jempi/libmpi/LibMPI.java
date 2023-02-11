package org.jembi.jempi.libmpi;

import io.vavr.control.Either;
import io.vavr.control.Option;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.libmpi.dgraph.LibDgraph;
import org.jembi.jempi.shared.models.CustomDemographicData;
import org.jembi.jempi.shared.models.CustomGoldenRecord;
import org.jembi.jempi.shared.models.CustomPatient;
import org.jembi.jempi.shared.models.LinkInfo;
import org.jembi.jempi.shared.utils.LibMPIPaginatedResultSet;
import org.jembi.jempi.shared.utils.SimpleSearchRequestPayload;

import java.util.List;

public class LibMPI {

   private static final Logger LOGGER = LogManager.getLogger(LibMPI.class);
   private final LibMPIClientInterface client;

   public LibMPI(
         final String[] host,
         final int[] port) {
      LOGGER.info("{}", "LibMPI Constructor");
      client = new LibDgraph(host, port);
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

   public LibMPIPaginatedResultSet<MpiExpandedGoldenRecord> simpleSearchGoldenRecords(
         List<SimpleSearchRequestPayload.SearchParameter> params,
         Integer offset,
         Integer limit,
         String sortBy,
         Boolean sortAsc
                                                                                     ) {
      return client.simpleSearchGoldenRecords(params, offset, limit, sortBy, sortAsc);
   }

   public LibMPIPaginatedResultSet<MpiExpandedGoldenRecord> customSearchGoldenRecords(
         List<SimpleSearchRequestPayload> params,
         Integer offset,
         Integer limit,
         String sortBy,
         Boolean sortAsc
                                                                                     ) {
      return client.customSearchGoldenRecords(params, offset, limit, sortBy, sortAsc);
   }

   public LibMPIPaginatedResultSet<CustomPatient> simpleSearchPatientRecords(
         List<SimpleSearchRequestPayload.SearchParameter> params,
         Integer offset,
         Integer limit,
         String sortBy,
         Boolean sortAsc
                                                                            ) {
      return client.simpleSearchPatientRecords(params, offset, limit, sortBy, sortAsc);
   }

   public LibMPIPaginatedResultSet<CustomPatient> customSearchPatientRecords(
         List<SimpleSearchRequestPayload> params,
         Integer offset,
         Integer limit,
         String sortBy,
         Boolean sortAsc
                                                                            ) {
      return client.customSearchPatientRecords(params, offset, limit, sortBy, sortAsc);
   }

   public List<CustomGoldenRecord> getCandidates(
         final CustomDemographicData patient,
         final boolean applyDeterministicFilter) {
      LOGGER.debug("get candidates <- {}", patient);
      final var candidates = client.getCandidates(patient, applyDeterministicFilter);
      candidates.forEach(candidate -> LOGGER.debug("get candidates -> {}", candidate));
      return candidates;
   }

   public List<MpiExpandedGoldenRecord> getMpiExpandedGoldenRecordList(final List<String> idList) {
      return client.getMpiExpandedGoldenRecordList(idList);
   }

   public List<MpiExpandedPatient> getMpiExpandedPatients(final List<String> idList) {
      return client.getMpiExpandedPatients(idList);
   }

   public List<String> getGoldenIdListByPredicate(
         final String predicate,
         final String val) {
      return client.getGoldenIdListByPredicate(predicate, val);
   }

   public CustomGoldenRecord getGoldenRecord(final String uid) {
      return client.getGoldenRecordByUid(uid);
   }

   public long countGoldenRecords() {
      return client.countGoldenRecords();
   }

   public List<String> getGoldenIdList() {
      return client.getGoldenIdList();
   }

   public CustomPatient getMpiPatient(final String uid) {
      return client.getPatient(uid);
   }

   public long countPatients() {
      return client.countPatients();
   }


   /*
    * *****************************************************************************
    * *
    * Mutations
    * *****************************************************************************
    * *
    */

   public boolean updateGoldenRecordField(
         final String goldenID,
         final String fieldName,
         final String value) {
      return client.updateGoldenRecordField(goldenID, fieldName, value);
   }

   public Either<MpiGeneralError, LinkInfo> unLink(
         final String goldenRecordUID,
         final String patientUID,
         final float score) {
      return client.unLink(goldenRecordUID, patientUID, score);
   }

   public Either<MpiGeneralError, LinkInfo> updateLink(
         final String goldenRecordUID,
         final String newGoldenRecordUID,
         final String patientUID,
         final float score) {
      return client.updateLink(goldenRecordUID, newGoldenRecordUID, patientUID, score);
   }

   public LinkInfo createPatientAndLinkToExistingGoldenRecord(
         final CustomPatient patient,
         final LibMPIClientInterface.GoldenUIDScore goldenUIDScore) {
      LOGGER.debug("link existing <- {}", patient);
      LOGGER.debug("link existing <- {}", goldenUIDScore);
      final var linkInfo = client.createPatientAndLinkToExistingGoldenRecord(patient, goldenUIDScore);
      LOGGER.debug("link existing -> {}", linkInfo);
      return linkInfo;
   }

   public LinkInfo createPatientAndLinkToClonedGoldenRecord(
         final CustomPatient patient,
         float score) {
      LOGGER.debug("link new <- {}", patient);
      LOGGER.debug("link new <- {}", score);
      final var linkInfo = client.createPatientAndLinkToClonedGoldenRecord(patient, score);
      LOGGER.debug("link new -> {}", linkInfo);
      return linkInfo;
   }

}
