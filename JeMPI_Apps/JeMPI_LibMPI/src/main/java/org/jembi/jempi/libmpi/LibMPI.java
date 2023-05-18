package org.jembi.jempi.libmpi;

import io.vavr.control.Either;
import io.vavr.control.Option;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.libmpi.dgraph.LibDgraph;
import org.jembi.jempi.libmpi.postgresql.LibPostgresql;
import org.jembi.jempi.shared.models.*;

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

   public LibMPI(
         final String URL,
         final String USR,
         final String PSW) {
      LOGGER.info("{}", "LibMPI Constructor");
      client = new LibPostgresql(URL, USR, PSW);
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

   public long countInteractions() {
      return client.countInteractions();
   }

   public long countGoldenRecords() {
      return client.countGoldenRecords();
   }


   public PatientRecord findPatientRecord(final String patientId) {
      return client.findPatientRecord(patientId);
   }

   public List<PatientRecord> findPatientRecords(final List<String> patientIds) {
      return client.findPatientRecords(patientIds);
   }

   public List<ExpandedPatientRecord> findExpandedPatientRecords(final List<String> patientIds) {
      return client.findExpandedPatientRecords(patientIds);
   }

   public GoldenRecord findGoldenRecord(final String goldenId) {
      return client.findGoldenRecord(goldenId);
   }

   public List<GoldenRecord> findGoldenRecords(final List<String> goldenIds) {
      return client.findGoldenRecords(goldenIds);
   }

   public ExpandedGoldenRecord findExpandedGoldenRecord(final String goldenId) {
      final var records = client.findExpandedGoldenRecords(List.of(goldenId));
      if (!records.isEmpty()) {
         return records.get(0);
      }
      return null;
   }

   public List<ExpandedGoldenRecord> findExpandedGoldenRecords(final List<String> goldenIds) {
      return client.findExpandedGoldenRecords(goldenIds);
   }

   public List<String> findGoldenIds() {
      return client.findGoldenIds();
   }

   public List<GoldenRecord> getCandidates(
         final CustomDemographicData demographicData,
         final boolean applyDeterministicFilter) {
      return client.findCandidates(demographicData, applyDeterministicFilter);
   }

   public LibMPIPaginatedResultSet<ExpandedGoldenRecord> simpleSearchGoldenRecords(
         final List<SimpleSearchRequestPayload.SearchParameter> params,
         final Integer offset,
         final Integer limit,
         final String sortBy,
         final Boolean sortAsc) {
      return client.simpleSearchGoldenRecords(params, offset, limit, sortBy, sortAsc);
   }

   public LibMPIPaginatedResultSet<ExpandedGoldenRecord> customSearchGoldenRecords(
         final List<SimpleSearchRequestPayload> params,
         final Integer offset,
         final Integer limit,
         final String sortBy,
         final Boolean sortAsc) {
      return client.customSearchGoldenRecords(params, offset, limit, sortBy, sortAsc);
   }

   public LibMPIPaginatedResultSet<PatientRecord> simpleSearchPatientRecords(
         final List<SimpleSearchRequestPayload.SearchParameter> params,
         final Integer offset,
         final Integer limit,
         final String sortBy,
         final Boolean sortAsc) {
      return client.simpleSearchPatientRecords(params, offset, limit, sortBy, sortAsc);
   }

   public LibMPIPaginatedResultSet<PatientRecord> customSearchPatientRecords(
         final List<SimpleSearchRequestPayload> params,
         final Integer offset,
         final Integer limit,
         final String sortBy,
         final Boolean sortAsc) {
      return client.customSearchPatientRecords(params, offset, limit, sortBy, sortAsc);
   }

   /*
    * *****************************************************************************
    * *
    * Mutations
    * *****************************************************************************
    * *
    */

   public boolean setScore(
         final String patientuid,
         final String goldenRecordUid,
         final float score) {
      return client.setScore(patientuid, goldenRecordUid, score);
   }

   public boolean updateGoldenRecordField(
         final String goldenId,
         final String fieldName,
         final String value) {
      return client.updateGoldenRecordField(goldenId, fieldName, value);
   }

   public Either<MpiGeneralError, LinkInfo> linkToNewGoldenRecord(
         final String currentGoldenId,
         final String patientId,
         final float score) {
      return client.linkToNewGoldenRecord(currentGoldenId, patientId, score);
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
      return client.createPatientAndLinkToExistingGoldenRecord(patientRecord, goldenIdScore);
   }

   public LinkInfo createPatientAndLinkToClonedGoldenRecord(
         final PatientRecord patientRecord,
         final float score) {
      return client.createPatientAndLinkToClonedGoldenRecord(patientRecord, score);
   }

}
