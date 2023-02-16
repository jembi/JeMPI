package org.jembi.jempi.libmpi;

import io.vavr.control.Either;
import io.vavr.control.Option;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.libmpi.dgraph.LibDgraph;
import org.jembi.jempi.shared.models.*;
import org.jembi.jempi.shared.utils.LibMPIPaginatedResultSet;
import org.jembi.jempi.shared.utils.SimpleSearchRequestPayload;

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

   public long countPatientRecords() {
      return client.countPatientRecords();
   }

   public long countGoldenRecords() {
      return client.countGoldenRecords();
   }

   public PatientRecord getPatientRecord(final String uid) {
      return client.getPatientRecord(uid);
   }

   public GoldenRecord getGoldenRecord(final String uid) {
      return client.getGoldenRecord(uid);
   }

   public ExpandedGoldenRecord getExpandedGoldenRecord(final String uid) {
      final var records = client.getExpandedGoldenRecords(List.of(uid));
      if (records.size() > 0) {
         return records.get(0);
      }
      return null;
   }

   public List<String> getGoldenIds() {
      return client.getGoldenIds();
   }
   public List<GoldenRecord> getCandidates(
         final CustomDemographicData demographicData,
         final boolean applyDeterministicFilter) {
      LOGGER.debug("get candidates <- {}", demographicData);
      final var candidates = client.getCandidates(demographicData, applyDeterministicFilter);
      candidates.forEach(candidate -> LOGGER.debug("get candidates -> {}", candidate));
      return candidates;
   }

   public List<ExpandedPatientRecord> getExpandedPatients(final List<String> ids) {
      return client.getExpandedPatients(ids);
   }

   public List<ExpandedGoldenRecord> getExpandedGoldenRecords(final List<String> ids) {
      return client.getExpandedGoldenRecords(ids);
   }

   public LibMPIPaginatedResultSet<ExpandedGoldenRecord> simpleSearchGoldenRecords(
         final List<SimpleSearchRequestPayload.SearchParameter> params,
         final Integer offset,
         final Integer limit,
         final String sortBy,
         final Boolean sortAsc
                                                                                  ) {
      return client.simpleSearchGoldenRecords(params, offset, limit, sortBy, sortAsc);
   }

   public LibMPIPaginatedResultSet<ExpandedGoldenRecord> customSearchGoldenRecords(
         final List<SimpleSearchRequestPayload> params,
         final Integer offset,
         final Integer limit,
         final String sortBy,
         final Boolean sortAsc
                                                                                  ) {
      return client.customSearchGoldenRecords(params, offset, limit, sortBy, sortAsc);
   }

   public LibMPIPaginatedResultSet<PatientRecord> simpleSearchPatientRecords(
         final List<SimpleSearchRequestPayload.SearchParameter> params,
         final Integer offset,
         final Integer limit,
         final String sortBy,
         final Boolean sortAsc
                                                                            ) {
      return client.simpleSearchPatientRecords(params, offset, limit, sortBy, sortAsc);
   }

   public LibMPIPaginatedResultSet<PatientRecord> customSearchPatientRecords(
         final List<SimpleSearchRequestPayload> params,
         final Integer offset,
         final Integer limit,
         final String sortBy,
         final Boolean sortAsc
                                                                            ) {
      return client.customSearchPatientRecords(params, offset, limit, sortBy, sortAsc);
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
         final PatientRecord patientRecord,
         final LibMPIClientInterface.GoldenUIDScore goldenUIDScore) {
      LOGGER.debug("link existing <- {}", patientRecord);
      LOGGER.debug("link existing <- {}", goldenUIDScore);
      final var linkInfo = client.createPatientAndLinkToExistingGoldenRecord(patientRecord, goldenUIDScore);
      LOGGER.debug("link existing -> {}", linkInfo);
      return linkInfo;
   }

   public LinkInfo createPatientAndLinkToClonedGoldenRecord(
         final PatientRecord patientRecord,
         final float score) {
      LOGGER.debug("link new <- {}", patientRecord);
      LOGGER.debug("link new <- {}", score);
      final var linkInfo = client.createPatientAndLinkToClonedGoldenRecord(patientRecord, score);
      LOGGER.debug("link new -> {}", linkInfo);
      return linkInfo;
   }

}
