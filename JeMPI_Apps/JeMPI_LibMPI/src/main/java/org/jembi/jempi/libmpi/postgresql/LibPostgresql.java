package org.jembi.jempi.libmpi.postgresql;

import io.vavr.control.Either;
import io.vavr.control.Option;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.libmpi.LibMPIClientInterface;
import org.jembi.jempi.libmpi.MpiGeneralError;
import org.jembi.jempi.libmpi.MpiServiceError;
import org.jembi.jempi.shared.models.*;

import java.util.List;
import java.util.UUID;

public final class LibPostgresql implements LibMPIClientInterface {

   private static final Logger LOGGER = LogManager.getLogger(LibPostgresql.class);

   public LibPostgresql(
         final String URL,
         final String USR,
         final String PSW) {
      LOGGER.info("{}", "LibPostgresql Constructor");
      PostgresqlClient.getInstance().config(URL, USR, PSW);
   }

   /*
    * *******************************************************
    * QUERIES
    * *******************************************************
    *
    */

   public long countInteractions() {
      return PostgresqlQueries.countInteractions();
   }

   public long countGoldenRecords() {
      return PostgresqlQueries.countGoldenRecords();
   }

   public PatientRecord findPatientRecord(final String patientId) {
      final var encounter = PostgresqlQueries.getEncounter(UUID.fromString(patientId));
      final var sourceId = PostgresqlQueries.getEncounterSourceIds(encounter.uid());
      return new PatientRecord(encounter.uid().toString(),
                               new SourceId(sourceId.get(0).id().toString(),
                                            sourceId.get(0).data().facility(),
                                            sourceId.get(0).data().patient()),
                               encounter.data());
   }

   public List<PatientRecord> findPatientRecords(final List<String> patientIds) {
      return patientIds.stream().map(this::findPatientRecord).toList();
   }

   private ExpandedPatientRecord findExpandedPatientRecord(final String eid) {
      final var encounter = findPatientRecord(eid);
      final var goldenRecord = PostgresqlQueries.getGoldenRecordsOfEncounter(UUID.fromString(eid)).get(0);
      return new ExpandedPatientRecord(
            encounter,
            List.of(new GoldenRecordWithScore(new GoldenRecord(goldenRecord.uid().toString(),
                                                               PostgresqlQueries.getGoldenRecordSourceIds(goldenRecord.uid())
                                                                                .stream()
                                                                                .map(x -> new SourceId(x.id().toString(),
                                                                                                       x.data().facility(),
                                                                                                       x.data().patient()))
                                                                                .toList(),
                                                               goldenRecord.data()),
                                              PostgresqlQueries.getScore(goldenRecord.uid(), UUID.fromString(eid)))));
   }

   public List<ExpandedPatientRecord> findExpandedPatientRecords(final List<String> patientIds) {
      return patientIds.stream().map(this::findExpandedPatientRecord).toList();
   }

   public GoldenRecord findGoldenRecord(final String goldenId) {
      final var goldenRecord = PostgresqlQueries.getGoldenRecord(UUID.fromString(goldenId));
      final var sourceIds = PostgresqlQueries.getGoldenRecordSourceIds(UUID.fromString(goldenId));
      return new GoldenRecord(goldenId,
                              sourceIds.stream()
                                       .map(x -> new SourceId(x.id().toString(), x.data().facility(), x.data().patient()))
                                       .toList(),
                              goldenRecord.data());
   }

   public List<GoldenRecord> findGoldenRecords(final List<String> ids) {
      return ids.stream().map(this::findGoldenRecord).toList();
   }

   public List<ExpandedGoldenRecord> findExpandedGoldenRecords(final List<String> goldenIds) {
      return goldenIds.stream().map(gid -> {
         final var gidUUID = UUID.fromString(gid);
         final var goldenRecord = findGoldenRecord(gid);
         final var encounters = PostgresqlQueries.getGoldenRecordEncounters(gidUUID);
         return new ExpandedGoldenRecord(
               goldenRecord,
               encounters.stream()
                         .map(e -> {
                            final var sid = PostgresqlQueries.getEncounterSourceIds(e.uid()).get(0);
                            final var score = PostgresqlQueries.getScore(gidUUID, e.uid());
                            return new PatientRecordWithScore(new PatientRecord(e.uid().toString(),
                                                                                new SourceId(sid.id().toString(),
                                                                                             sid.data().facility(),
                                                                                             sid.data().patient()),
                                                                                e.data()),
                                                              score);
                         })
                         .toList());
      }).toList();
   }

   public List<String> findGoldenIds() {
      return PostgresqlQueries.getGoldenIds().stream().map(UUID::toString).toList();
   }

   public List<GoldenRecord> findCandidates(
         final CustomDemographicData demographicData,
         final boolean applyDeterministicFilter) {
      return PostgresqlQueries.findCandidates(demographicData);
   }

   public boolean setScore(
         final String patientUID,
         final String goldenRecordUid,
         final float score) {
      return PostgresqlMutations.setScore(patientUID, goldenRecordUid, score);
   }

   public LibMPIPaginatedResultSet<ExpandedGoldenRecord> simpleSearchGoldenRecords(
         final List<SimpleSearchRequestPayload.SearchParameter> params,
         final Integer offset,
         final Integer limit,
         final String sortBy,
         final Boolean sortAsc) {
      LOGGER.error("Not implemented");
      return null;
   }

   public LibMPIPaginatedResultSet<ExpandedGoldenRecord> customSearchGoldenRecords(
         final List<SimpleSearchRequestPayload> params,
         final Integer offset,
         final Integer limit,
         final String sortBy,
         final Boolean sortAsc) {
      LOGGER.error("Not implemented");
      return null;
   }

   public LibMPIPaginatedResultSet<PatientRecord> simpleSearchPatientRecords(
         final List<SimpleSearchRequestPayload.SearchParameter> params,
         final Integer offset,
         final Integer limit,
         final String sortBy,
         final Boolean sortAsc) {
      LOGGER.error("Not implemented");
      return null;
   }

   public LibMPIPaginatedResultSet<PatientRecord> customSearchPatientRecords(
         final List<SimpleSearchRequestPayload> params,
         final Integer offset,
         final Integer limit,
         final String sortBy,
         final Boolean sortAsc) {
      LOGGER.error("Not implemented");
      return null;
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
      return PostgresqlMutations.updateGoldenRecordField(goldenId, fieldName, val);
   }

   public Either<MpiGeneralError, LinkInfo> linkToNewGoldenRecord(
         final String goldenUID,
         final String patientUID,
         final float score) {
      LOGGER.error("Not implemented");
      return null;
   }

   public Either<MpiGeneralError, LinkInfo> updateLink(
         final String goldenUID,
         final String newGoldenUID,
         final String patientUID,
         final float score) {
      LOGGER.error("Not implemented");
      return null;
   }

   public LinkInfo createPatientAndLinkToExistingGoldenRecord(
         final PatientRecord patientRecord,
         final GoldenIdScore goldenIdScore) {
      final var nodeSourceIds = PostgresqlQueries.findSourceId(patientRecord.sourceId().facility(),
                                                               patientRecord.sourceId().patient());
      final var goldenRecordSourceIds = PostgresqlQueries.getGoldenRecordSourceIds(UUID.fromString(goldenIdScore.goldenId()));
      final var sid = nodeSourceIds.isEmpty()
            ? new NodeSourceId(patientRecord.sourceId().facility(),
                               patientRecord.sourceId().patient()).createNode()
            : nodeSourceIds.get(0).id();
      final var eid = new NodeEncounter(patientRecord.demographicData()).createNode();
      Edge.createEdge(eid, sid, Edge.EdgeName.EID2SID);
      if (nodeSourceIds.isEmpty() || goldenRecordSourceIds.stream().noneMatch(p -> p.id().equals(nodeSourceIds.get(0).id()))) {
         Edge.createEdge(UUID.fromString(goldenIdScore.goldenId()), sid, Edge.EdgeName.GID2SID);
      }
      Edge.createEdge(UUID.fromString(goldenIdScore.goldenId()),
                      eid,
                      Edge.EdgeName.GID2EID,
                      new FacetScore(goldenIdScore.score()));
      return new LinkInfo(goldenIdScore.goldenId(), eid.toString(), goldenIdScore.score());
   }

   public LinkInfo createPatientAndLinkToClonedGoldenRecord(
         final PatientRecord patientRecord,
         final float score) {
      final var sid = new NodeSourceId(patientRecord.sourceId().facility(),
                                       patientRecord.sourceId().patient()).createNode();
      final var eid = new NodeEncounter(patientRecord.demographicData()).createNode();
      final var gid = new NodeGoldenRecord(patientRecord.demographicData()).createNode();
      Edge.createEdge(eid, sid, Edge.EdgeName.EID2SID);
      Edge.createEdge(gid, sid, Edge.EdgeName.GID2SID);
      Edge.createEdge(gid, eid, Edge.EdgeName.GID2EID, new FacetScore(score));

      return new LinkInfo(gid.toString(), eid.toString(), score);
   }

   public void startTransaction() {
      PostgresqlClient.getInstance().startTransaction();
   }

   public void closeTransaction() {
      PostgresqlClient.getInstance().closeTransaction();
   }

   /*
    * *******************************************************
    * DATABASE
    * *******************************************************
    */


   public Option<MpiGeneralError> dropAll() {
      LOGGER.debug("{}", "drop all");
      startTransaction();
      if (!PostgresqlMutations.dropAll()) {
         return Option.of(new MpiServiceError.GeneralError("Drop All Error"));
      }
      return Option.of(null);
   }

   public Option<MpiGeneralError> dropAllData() {
      return Option.of(new MpiServiceError.GeneralError("Drop All Data Error"));
   }

   public Option<MpiGeneralError> createSchema() {
      LOGGER.debug("{}", "create schema");
      startTransaction();
      if (!PostgresqlMutations.createSchema()) {
         return Option.of(new MpiServiceError.GeneralError("Create Schema"));
      } else {
         return Option.of(null);
      }
   }


}
