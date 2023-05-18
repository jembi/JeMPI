package org.jembi.jempi.libmpi.dgraph;

import io.dgraph.DgraphProto;
import io.vavr.control.Either;
import io.vavr.control.Option;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.libmpi.LibMPIClientInterface;
import org.jembi.jempi.libmpi.MpiGeneralError;
import org.jembi.jempi.libmpi.MpiServiceError;
import org.jembi.jempi.shared.models.*;
import org.jembi.jempi.shared.models.LibMPIPaginatedResultSet;
import org.jembi.jempi.shared.models.SimpleSearchRequestPayload;

import java.util.List;

import static io.dgraph.DgraphProto.Operation.DropOp.DATA;

public final class LibDgraph implements LibMPIClientInterface {

   private static final Logger LOGGER = LogManager.getLogger(LibDgraph.class);

   public LibDgraph(
         final String[] host,
         final int[] port) {
      LOGGER.info("{}", "LibDgraph Constructor");

      DgraphClient.getInstance().config(host, port);
   }

   /*
    * *******************************************************
    * QUERIES
    * *******************************************************
    *
    */

   public long countInteractions() {
      return DgraphQueries.countInteractions();
   }

   public long countGoldenRecords() {
      return DgraphQueries.countGoldenRecords();
   }

   public PatientRecord findPatientRecord(final String patientId) {
      return DgraphQueries.getPatientRecord(patientId);
   }

   public List<PatientRecord> findPatientRecords(final List<String> patientIds) {
      return List.of();
   }

   public List<ExpandedPatientRecord> findExpandedPatientRecords(final List<String> patientIds) {
      final var list = DgraphQueries.findExpandedPatientRecords(patientIds);
      return list.stream().map(CustomDgraphExpandedPatientRecord::toExpandedPatientRecord).toList();
   }

   public GoldenRecord findGoldenRecord(final String goldenId) {
      final var rec = DgraphQueries.findDgraphGoldenRecord(goldenId);
      if (rec == null) {
         return null;
      }
      return rec.toGoldenRecord();
   }

   public List<GoldenRecord> findGoldenRecords(final List<String> ids) {
      final var list = DgraphQueries.findGoldenRecords(ids);
      return list.stream().map(CustomDgraphGoldenRecord::toGoldenRecord).toList();
   }

   public List<ExpandedGoldenRecord> findExpandedGoldenRecords(final List<String> goldenIds) {
      final var list = DgraphQueries.getExpandedGoldenRecords(goldenIds);
      return list.stream().map(CustomDgraphExpandedGoldenRecord::toExpandedGoldenRecord).toList();
   }

   public List<String> findGoldenIds() {
      return DgraphQueries.getGoldenIds();
   }

   public List<GoldenRecord> findCandidates(
         final CustomDemographicData demographicData,
         final boolean applyDeterministicFilter) {
      final var candidates = CustomDgraphQueries.getCandidates(demographicData, applyDeterministicFilter);
      return candidates.stream().map(CustomDgraphGoldenRecord::toGoldenRecord).toList();
   }

   private LibMPIPaginatedResultSet<ExpandedGoldenRecord> paginatedExpandedGoldenRecords(
         final DgraphExpandedGoldenRecords list) {
      if (list == null) {
         return null;
      }
      final var data = list.all().stream().map(CustomDgraphExpandedGoldenRecord::toExpandedGoldenRecord).toList();
      final var pagination = list.pagination().get(0);
      return new LibMPIPaginatedResultSet<>(data, pagination);
   }

   private LibMPIPaginatedResultSet<PatientRecord> paginatedPatientRecords(final DgraphPatientRecords list) {
      if (list == null) {
         return null;
      }
      final var data = list.all().stream().map(CustomDgraphPatientRecord::toPatientRecord).toList();
      final var pagination = list.pagination().get(0);
      return new LibMPIPaginatedResultSet<>(data, pagination);
   }

   public boolean setScore(
         final String patientUID,
         final String goldenRecordUid,
         final float score) {
      return DgraphMutations.setScore(patientUID, goldenRecordUid, score);
   }

   public LibMPIPaginatedResultSet<ExpandedGoldenRecord> simpleSearchGoldenRecords(
         final List<SimpleSearchRequestPayload.SearchParameter> params,
         final Integer offset,
         final Integer limit,
         final String sortBy,
         final Boolean sortAsc) {
      final var list = DgraphQueries.simpleSearchGoldenRecords(params, offset, limit, sortBy, sortAsc);
      return paginatedExpandedGoldenRecords(list);
   }

   public LibMPIPaginatedResultSet<ExpandedGoldenRecord> customSearchGoldenRecords(
         final List<SimpleSearchRequestPayload> params,
         final Integer offset,
         final Integer limit,
         final String sortBy,
         final Boolean sortAsc) {
      final var list = DgraphQueries.customSearchGoldenRecords(params, offset, limit, sortBy, sortAsc);
      return paginatedExpandedGoldenRecords(list);
   }

   public LibMPIPaginatedResultSet<PatientRecord> simpleSearchPatientRecords(
         final List<SimpleSearchRequestPayload.SearchParameter> params,
         final Integer offset,
         final Integer limit,
         final String sortBy,
         final Boolean sortAsc) {
      final var list = DgraphQueries.simpleSearchPatientRecords(params, offset, limit, sortBy, sortAsc);
      return paginatedPatientRecords(list);
   }

   public LibMPIPaginatedResultSet<PatientRecord> customSearchPatientRecords(
         final List<SimpleSearchRequestPayload> params,
         final Integer offset,
         final Integer limit,
         final String sortBy,
         final Boolean sortAsc) {
      final var list = DgraphQueries.customSearchPatientRecords(params, offset, limit, sortBy, sortAsc);
      return paginatedPatientRecords(list);
   }

   /*
    * *******************************************************
    * MUTATIONS
    * *******************************************************
    */

   public boolean updateGoldenRecordField(
         final String goldenId,
         final String fieldName,
         final String val) {
      return DgraphMutations.updateGoldenRecordField(goldenId, fieldName, val);
   }

   public Either<MpiGeneralError, LinkInfo> linkToNewGoldenRecord(
         final String goldenUID,
         final String patientUID,
         final float score) {
      return DgraphMutations.linkToNewGoldenRecord(goldenUID, patientUID, score);
   }

   public Either<MpiGeneralError, LinkInfo> updateLink(
         final String goldenUID,
         final String newGoldenUID,
         final String patientUID,
         final float score) {
      return DgraphMutations.updateLink(goldenUID, newGoldenUID, patientUID, score);
   }

   public LinkInfo createPatientAndLinkToExistingGoldenRecord(
         final PatientRecord patientRecord,
         final GoldenIdScore goldenIdScore) {
      return DgraphMutations.linkDGraphPatient(patientRecord, goldenIdScore);
   }

   public LinkInfo createPatientAndLinkToClonedGoldenRecord(
         final PatientRecord patientRecord,
         final float score) {
      return DgraphMutations.addNewDGraphPatient(patientRecord);
   }

   public void startTransaction() {
      DgraphClient.getInstance().startTransaction();
   }

   public void closeTransaction() {
      DgraphClient.getInstance().closeTransaction();
   }

   /*
    * *******************************************************
    * DATABASE
    * *******************************************************
    */

   public Option<MpiGeneralError> dropAll() {
      try {
         DgraphClient.getInstance().alter(DgraphProto.Operation.newBuilder().setDropAll(true).build());
         return Option.none();
      } catch (RuntimeException e) {
         LOGGER.error(e.getMessage(), e);
         return Option.of(new MpiServiceError.GeneralError("Drop All Error"));
      }
   }

   public Option<MpiGeneralError> dropAllData() {
      try {
         DgraphClient.getInstance().alter(DgraphProto.Operation.newBuilder().setDropOp(DATA).build());
         return Option.none();
      } catch (RuntimeException e) {
         LOGGER.error(e.getMessage());
         return Option.of(new MpiServiceError.GeneralError("Drop All Data Error"));
      }
   }

   public Option<MpiGeneralError> createSchema() {
      return DgraphMutations.createSchema();
   }

}
