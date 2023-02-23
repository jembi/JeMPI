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
   public List<PatientRecord> getPatientRecords(final List<String> patientIds) {
      return client.getPatientRecords(patientIds);
   }

   public List<GoldenRecord> getGoldenRecords(final List<String> goldenIds) {
      return client.getGoldenRecords(goldenIds);
   }

   public ExpandedGoldenRecord getExpandedGoldenRecord(final String goldenId) {
      final var records = client.getExpandedGoldenRecords(List.of(goldenId));
      if (!records.isEmpty()) {
         return records.get(0);
      }
      return null;
   }

   public List<ExpandedPatientRecord> getExpandedPatientRecords(final List<String> patientIds) {
      return client.getExpandedPatientRecords(patientIds);
   }

   public List<ExpandedGoldenRecord> getExpandedGoldenRecords(final List<String> goldenIds) {
      return client.getExpandedGoldenRecords(goldenIds);
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
         final String goldenId,
         final String fieldName,
         final String value) {
      return client.updateGoldenRecordField(goldenId, fieldName, value);
   }

   public Either<MpiGeneralError, LinkInfo> unLink(
         final String goldenId,
         final String patientId,
         final float score) {
      return client.unLink(goldenId, patientId, score);
   }

   public Either<MpiGeneralError, LinkInfo> updateLink(
         final String goldenId,
         final String newGoldenId,
         final String patientId,
         final float score) {
      return client.updateLink(goldenId, newGoldenId, patientId, score);
   }

   public LinkInfo createPatientAndLinkToExistingGoldenRecord(
         final PatientRecord patientRecord,
         final LibMPIClientInterface.GoldenIdScore goldenIdScore) {
      LOGGER.debug("link existing <- {}", patientRecord);
      LOGGER.debug("link existing <- {}", goldenIdScore);
      final var linkInfo = client.createPatientAndLinkToExistingGoldenRecord(patientRecord, goldenIdScore);
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
