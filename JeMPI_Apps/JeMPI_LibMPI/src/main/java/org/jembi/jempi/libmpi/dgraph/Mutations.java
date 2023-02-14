package org.jembi.jempi.libmpi.dgraph;

import com.google.protobuf.ByteString;
import io.dgraph.DgraphProto;
import io.netty.util.internal.StringUtil;
import io.vavr.control.Either;
import io.vavr.control.Option;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.libmpi.LibMPIClientInterface;
import org.jembi.jempi.libmpi.MpiGeneralError;
import org.jembi.jempi.libmpi.MpiServiceError;
import org.jembi.jempi.shared.models.CustomDemographicData;
import org.jembi.jempi.shared.models.LinkInfo;
import org.jembi.jempi.shared.models.PatientRecord;
import org.jembi.jempi.shared.models.SourceId;
import org.jembi.jempi.shared.utils.AppUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.jembi.jempi.libmpi.dgraph.CustomLibMPIConstants.*;

final class Mutations {

   private static final Logger LOGGER = LogManager.getLogger(Mutations.class);

   private Mutations() {
   }

   private static String createSourceIdTriple(final SourceId sourceId) {
      final String uuid = UUID.randomUUID().toString();
      return String.format("""
                           _:%s  <SourceId.facility>                 %s          .
                           _:%s  <SourceId.patient>                  %s          .
                           _:%s  <dgraph.type>                      "SourceId"   .
                           """, uuid, AppUtils.quotedValue(sourceId.facility()), uuid, AppUtils.quotedValue(sourceId.patient()),
                           uuid);
   }

   private static LibMPISourceIdList getSourceId(final SourceId sourceId) {
      if (StringUtils.isBlank(sourceId.facility())
          || StringUtils.isBlank(sourceId.patient())) {
         return new LibMPISourceIdList(List.of());
      }
      final String query = String.format(
            """
            query query_source_id() {
               var(func: eq(SourceId.facility, "%s")) {
                  A as uid
               }
               var(func: eq(SourceId.patient, "%s")) {
                  B as uid
               }
               all(func: uid(A,B)) @filter (uid(A) AND uid(B)) {
                  uid
                  expand(SourceId)
               }
            }
            """, sourceId.facility(), sourceId.patient());
      return Queries.runSourceIdQuery(query);
   }

   private static boolean updateGoldenRecordPredicate(
         final String uid,
         final String predicate,
         final String value) {
      final var mutation = DgraphProto.Mutation.newBuilder()
                                               .setSetNquads(ByteString.copyFromUtf8(String.format(
                                                     """
                                                     <%s> <%s>          "%s"^^<xs:string>    .
                                                     <%s> <dgraph.type> "GoldenRecord"       .
                                                     """, uid, predicate, value, uid)))
                                               .build();
      final var result = Client.getInstance().doMutateTransaction(mutation);
      return StringUtil.isNullOrEmpty(result);
   }

   private static boolean deletePredicate(
         final String uid,
         final String predicate,
         final String value) {
      final var mutation = DgraphProto.Mutation.newBuilder()
                                               .setDelNquads(ByteString.copyFromUtf8(String.format(
                                                     """
                                                     <%s>  <%s>  <%s>  .
                                                     """, uid, predicate, value)))
                                               .build();
      final var result = Client.getInstance().doMutateTransaction(mutation);
      return result != null;
   }

   private static void addScoreFacets(final List<LibMPIPatientScore> patientScoreList) {
      StringBuilder simWeightFacet = new StringBuilder();
      for (LibMPIPatientScore patientScore : patientScoreList) {
         simWeightFacet.append(
               String.format("<%s> <GoldenRecord.patients> <%s> (score=%f) .%n",
                             patientScore.goldenUID(), patientScore.patientUID(), patientScore.score()));
      }

      final var s = simWeightFacet.toString();
      final DgraphProto.Mutation mu = DgraphProto.Mutation.newBuilder().setSetNquads(ByteString.copyFromUtf8(s)).build();

      Client.getInstance().doMutateTransaction(mu);
   }

   private static void addSourceId(
         final String uid,
         final String sourceId) {
      var mutation = String.format("<%s> <GoldenRecord.source_id> <%s> .%n", uid, sourceId);
      final DgraphProto.Mutation mu = DgraphProto.Mutation.newBuilder().setSetNquads(ByteString.copyFromUtf8(mutation)).build();
      Client.getInstance().doMutateTransaction(mu);
   }

   private static InsertPatientResult insertPatientRecord(final PatientRecord patientRecord) {
      final DgraphProto.Mutation sourceIdMutation =
            DgraphProto.Mutation.newBuilder()
                                .setSetNquads(ByteString.copyFromUtf8(createSourceIdTriple(patientRecord.sourceId())))
                                .build();
      final var sourceId = getSourceId(patientRecord.sourceId()).all();
      final var sourceIdUid = !sourceId.isEmpty()
            ? sourceId.get(0).uid()
            : Client.getInstance().doMutateTransaction(sourceIdMutation);
      final DgraphProto.Mutation mutation = DgraphProto.Mutation.newBuilder()
                                                                .setSetNquads(
                                                                      ByteString.copyFromUtf8(CustomLibMPIMutations.createPatientTriple(
                                                                            patientRecord.demographicData(),
                                                                            sourceIdUid)))
                                                                .build();
      return new InsertPatientResult(Client.getInstance().doMutateTransaction(mutation), sourceIdUid);
   }

   private static String cloneGoldenRecordFromPatient(
         final CustomDemographicData patient,
         final String patientUID,
         final String sourceUID,
         final float score) {
      final var command = CustomLibMPIMutations.createLinkedGoldenRecordTriple(patient, patientUID, sourceUID, score);
      final DgraphProto.Mutation mutation = DgraphProto.Mutation.newBuilder()
                                                                .setSetNquads(ByteString.copyFromUtf8(command))
                                                                .build();
      return Client.getInstance().doMutateTransaction(mutation);
   }

   private static void deleteGoldenRecord(final String uid) {
      final var mutation = DgraphProto.Mutation.newBuilder()
                                               .setDelNquads(ByteString.copyFromUtf8(
                                                     String.format("""
                                                                    <%s> * *  .
                                                                   """,
                                                                   uid)))
                                               .build();
      Client.getInstance().doMutateTransaction(mutation);
   }

   static LinkInfo addNewDGraphPatient(final PatientRecord patientRecord) {
      final var result = insertPatientRecord(patientRecord);
      if (result.patientUID == null) {
         LOGGER.error("Failed to insert patient");
         return null;
      }
      final var grUID = cloneGoldenRecordFromPatient(patientRecord.demographicData(), result.patientUID, result.sourceUID, 1.0F);
      if (grUID == null) {
         LOGGER.error("Failed to insert golden record");
         return null;
      }
      return new LinkInfo(grUID, result.patientUID, 1.0F);
   }

   static String camelToSnake(final String str) {
      return str.replaceAll("([A-Z]+)", "\\_$1").toLowerCase();
   }

   static boolean updateGoldenRecordField(
         final String uid,
         final String fieldName,
         final String val) {
      String predicate = "GoldenRecord." + camelToSnake(fieldName);
      return updateGoldenRecordPredicate(uid, predicate, val);
   }

   static Either<MpiGeneralError, LinkInfo> unLink(
         final String goldenUID,
         final String patientUID,
         final float score) {

      final var goldenUidPatientUidList = Queries.getGoldenUidPatientUidList(goldenUID);
      if (goldenUidPatientUidList.isEmpty() || !goldenUidPatientUidList.contains(patientUID)) {
         return Either.left(
               new MpiServiceError.GoldenUIDPatientConflictError("Patient not linked to GoldenRecord",
                                                                 goldenUID,
                                                                 patientUID));
      }
      final var count = goldenUidPatientUidList.size();

      final var patient = Queries.getDGraphPatientRecord(patientUID);
      if (patient == null) {
         LOGGER.warn("patient {} not found", patientUID);
         return Either.left(new MpiServiceError.PatientUIDDoesNotExistError("Patient not found", patientUID));
      }
      final var grec = Queries.getGoldenRecordByUid(goldenUID);
      if (grec == null) {
         return Either.left(new MpiServiceError.GoldenUIDDoesNotExistError("Golden Record not found", goldenUID));
      }
      if (!deletePredicate(goldenUID, PREDICATE_GOLDEN_RECORD_PATIENTS, patientUID)) {
         return Either.left(new MpiServiceError.DeletePredicateError(patientUID, PREDICATE_GOLDEN_RECORD_PATIENTS));
      }
      if (count == 1) {
         deleteGoldenRecord(goldenUID);
      }
      final var newGoldenID = cloneGoldenRecordFromPatient(patient.demographicData(), patient.uid(),
                                                           patient.sourceId().uid(),
                                                           score);
      return Either.right(new LinkInfo(newGoldenID, patientUID, score));
   }

   static Either<MpiGeneralError, LinkInfo> updateLink(
         final String goldenUID,
         final String newGoldenUID,
         final String patientUID,
         final float score) {

      final var goldenUidPatientUidList = Queries.getGoldenUidPatientUidList(goldenUID);
      if (goldenUidPatientUidList.isEmpty() || !goldenUidPatientUidList.contains(patientUID)) {
         return Either.left(
               new MpiServiceError.GoldenUIDPatientConflictError("Patient not linked to GoldenRecord", goldenUID, patientUID));
      }

      final var count = Queries.countGoldenRecordEntities(goldenUID);
      deletePredicate(goldenUID, "GoldenRecord.patients", patientUID);
      if (count == 1) {
         deleteGoldenRecord(goldenUID);
      }

      final var scoreList = new ArrayList<LibMPIPatientScore>();
      scoreList.add(new LibMPIPatientScore(newGoldenUID, patientUID, score));
      addScoreFacets(scoreList);
      return Either.right(new LinkInfo(newGoldenUID, patientUID, score));
   }

   static LinkInfo linkDGraphPatient(
         final PatientRecord patientRecord,
         final LibMPIClientInterface.GoldenUIDScore goldenUIDScore) {
      final var result = insertPatientRecord(patientRecord);

      if (result.patientUID == null) {
         LOGGER.error("Failed to insert dgraphPatient");
         return null;
      }
      final List<LibMPIPatientScore> patientScoreList = new ArrayList<>();
      patientScoreList.add(new LibMPIPatientScore(goldenUIDScore.goldenUID(), result.patientUID, goldenUIDScore.score()));
      addScoreFacets(patientScoreList);
      addSourceId(patientScoreList.get(0).goldenUID(), result.sourceUID);
      final var grUID = patientScoreList.get(0).goldenUID();
      final var theScore = patientScoreList.get(0).score();
      return new LinkInfo(grUID, result.patientUID, theScore);
   }

   static Option<MpiGeneralError> createSchema() {
      final var schema =
            MUTATION_CREATE_SOURCE_ID_TYPE
            + MUTATION_CREATE_GOLDEN_RECORD_TYPE
            + MUTATION_CREATE_PATIENT_TYPE
            + MUTATION_CREATE_SOURCE_ID_FIELDS
            + MUTATION_CREATE_GOLDEN_RECORD_FIELDS
            + MUTATION_CREATE_PATIENT_FIELDS;
      try {
         final DgraphProto.Operation operation = DgraphProto.Operation.newBuilder().setSchema(schema).build();
         Client.getInstance().alter(operation);
         return Option.none();
      } catch (RuntimeException ex) {
         LOGGER.warn("{}", schema);
         LOGGER.error(ex.getLocalizedMessage(), ex);
         return Option.of(new MpiServiceError.GeneralError("Create Schema Error"));
      }
   }

   private record InsertPatientResult(
         String patientUID,
         String sourceUID) {
   }

}
