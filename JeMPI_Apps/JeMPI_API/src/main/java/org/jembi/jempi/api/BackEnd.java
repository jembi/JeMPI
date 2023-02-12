package org.jembi.jempi.api;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;
import akka.http.javadsl.server.directives.FileInfo;
import io.vavr.control.Either;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.AppConfig;
import org.jembi.jempi.api.keycloak.AkkaAdapterConfig;
import org.jembi.jempi.api.keycloak.AkkaKeycloakDeploymentBuilder;
import org.jembi.jempi.api.models.OAuthCodeRequestPayload;
import org.jembi.jempi.api.models.User;
import org.jembi.jempi.libmpi.LibMPI;
import org.jembi.jempi.libmpi.MpiGeneralError;
import org.jembi.jempi.linker.CustomLinkerProbabilistic;
import org.jembi.jempi.postgres.PsqlQueries;
import org.jembi.jempi.shared.models.*;
import org.jembi.jempi.shared.utils.CustomSearchRequestPayload;
import org.jembi.jempi.shared.utils.GoldenRecordUpdateRequestPayload;
import org.jembi.jempi.shared.utils.LibMPIPaginatedResultSet;
import org.jembi.jempi.shared.utils.SimpleSearchRequestPayload;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.ServerRequest;
import org.keycloak.adapters.rotation.AdapterTokenVerifier;
import org.keycloak.common.VerificationException;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.AccessTokenResponse;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BackEnd extends AbstractBehavior<BackEnd.Event> {

   private static final Logger LOGGER = LogManager.getLogger(BackEnd.class);

   private static LibMPI libMPI = null;
   private final AkkaAdapterConfig keycloakConfig;
   private final KeycloakDeployment keycloak;

   private BackEnd(ActorContext<Event> context) {
      super(context);
      if (libMPI == null) {
         openMPI();
      }
      // Init keycloak
      ClassLoader classLoader = getClass().getClassLoader();
      InputStream keycloakConfigStream = classLoader.getResourceAsStream("/keycloak.json");
      keycloakConfig = AkkaKeycloakDeploymentBuilder.loadAdapterConfig(keycloakConfigStream);
      keycloak = AkkaKeycloakDeploymentBuilder.build(keycloakConfig);
      LOGGER.debug("Keycloak configured, realm : " + keycloak.getRealm());
   }

   public static Behavior<BackEnd.Event> create() {
      return Behaviors.setup(BackEnd::new);
   }

   private static void openMPI() {
      final var host = new String[]{AppConfig.DGRAPH_ALPHA1_HOST, AppConfig.DGRAPH_ALPHA2_HOST,
                                    AppConfig.DGRAPH_ALPHA3_HOST};
      final var port = new int[]{AppConfig.DGRAPH_ALPHA1_PORT, AppConfig.DGRAPH_ALPHA2_PORT,
                                 AppConfig.DGRAPH_ALPHA3_PORT};
      libMPI = new LibMPI(host, port);
   }

   @Override
   public Receive<Event> createReceive() {
      return actor();
   }

   public Receive<Event> actor() {
      ReceiveBuilder<Event> builder = newReceiveBuilder();
      return builder
            .onMessage(EventLoginWithKeycloakRequest.class, this::eventLoginWithKeycloakHandler)
            .onMessage(EventGetGoldenRecordCountReq.class, this::eventGetGoldenRecordCountHandler)
            .onMessage(EventGetPatientCountReq.class, this::eventGetPatientCountHandler)
            .onMessage(EventGetNumberOfRecordsReq.class, this::eventGetNumberOfRecordsHandler)
//            .onMessage(EventGetGoldenIdListByPredicateReq.class, this::eventGetGoldenIdListByPredicateHandler)
            .onMessage(EventGetGoldenIdListReq.class, this::eventGetGoldenIdListHandler)
            .onMessage(EventFindGoldenRecordByUidRequest.class, this::findGoldenRecordByUidEventHandler)
            .onMessage(EventGetExpandedGoldenRecordsReq.class, this::eventGetExpandedGoldenRecordsHandler)
            .onMessage(EventGetExpandedPatientRecordsReq.class, this::eventGetExpandedPatientRecordsHandler)
            .onMessage(EventFindPatientByUidRequest.class, this::findPatientByUidEventHandler)
            .onMessage(EventGetCandidatesReq.class, this::eventGetCandidatesHandler)
            .onMessage(EventUpdateGoldenRecordRequest.class, this::eventUpdateGoldenRecordHandler)
            .onMessage(EventPatchLinkReq.class, this::eventPatchLinkHandler)
            .onMessage(EventGetMatchesForReviewReq.class, this::eventGetMatchesForReviewHandler)
            .onMessage(EventPatchUnLinkReq.class, this::eventPatchUnLinkHandler)
            .onMessage(EventNotificationRequestReq.class, this::eventNotificationRequestHandler)
            .onMessage(EventSimpleSearchGoldenRecordsRequest.class, this::eventSimpleSearchGoldenRecordsHandler)
            .onMessage(EventCustomSearchGoldenRecordsRequest.class, this::eventCustomSearchGoldenRecordsHandler)
            .onMessage(EventSimpleSearchPatientRecordsRequest.class, this::eventSimpleSearchPatientRecordsHandler)
            .onMessage(EventCustomSearchPatientRecordsRequest.class, this::eventCustomSearchPatientRecordsHandler)
            .onMessage(EventPostCsvFileRequest.class, this::eventPostCsvFileRequestHandler)
            .build();
   }

   private Behavior<Event> eventSimpleSearchGoldenRecordsHandler(EventSimpleSearchGoldenRecordsRequest request) {
      SimpleSearchRequestPayload payload = request.searchRequestPayload();
      List<SimpleSearchRequestPayload.SearchParameter> parameters = payload.parameters();
      Integer offset = payload.offset();
      Integer limit = payload.limit();
      String sortBy = payload.sortBy();
      Boolean sortAsc = payload.sortAsc();
      libMPI.startTransaction();
      var recs = libMPI.simpleSearchGoldenRecords(parameters, offset, limit, sortBy, sortAsc);
      libMPI.closeTransaction();
      request.replyTo.tell(new EventSearchGoldenRecordsResponse(recs));
      return Behaviors.same();
   }

   private Behavior<Event> eventCustomSearchGoldenRecordsHandler(EventCustomSearchGoldenRecordsRequest request) {
      CustomSearchRequestPayload payload = request.searchRequestPayload();
      List<SimpleSearchRequestPayload> parameters = payload.$or();
      Integer offset = payload.offset();
      Integer limit = payload.limit();
      String sortBy = payload.sortBy();
      Boolean sortAsc = payload.sortAsc();
      libMPI.startTransaction();
      var recs = libMPI.customSearchGoldenRecords(parameters, offset, limit, sortBy, sortAsc);
      libMPI.closeTransaction();
      request.replyTo.tell(new EventSearchGoldenRecordsResponse(recs));
      return Behaviors.same();
   }

   private Behavior<Event> eventSimpleSearchPatientRecordsHandler(EventSimpleSearchPatientRecordsRequest request) {
      SimpleSearchRequestPayload payload = request.searchRequestPayload();
      List<SimpleSearchRequestPayload.SearchParameter> parameters = payload.parameters();
      Integer offset = payload.offset();
      Integer limit = payload.limit();
      String sortBy = payload.sortBy();
      Boolean sortAsc = payload.sortAsc();
      libMPI.startTransaction();
      var recs = libMPI.simpleSearchPatientRecords(parameters, offset, limit, sortBy, sortAsc);
      libMPI.closeTransaction();
      request.replyTo.tell(new EventSearchPatientRecordsResponse(recs));
      return Behaviors.same();
   }

   private Behavior<Event> eventCustomSearchPatientRecordsHandler(EventCustomSearchPatientRecordsRequest request) {
      CustomSearchRequestPayload payload = request.searchRequestPayload();
      List<SimpleSearchRequestPayload> parameters = payload.$or();
      Integer offset = payload.offset();
      Integer limit = payload.limit();
      String sortBy = payload.sortBy();
      Boolean sortAsc = payload.sortAsc();
      libMPI.startTransaction();
      var recs = libMPI.customSearchPatientRecords(parameters, offset, limit, sortBy, sortAsc);
      libMPI.closeTransaction();
      request.replyTo.tell(new EventSearchPatientRecordsResponse(recs));
      return Behaviors.same();
   }

   private Behavior<Event> eventLoginWithKeycloakHandler(final EventLoginWithKeycloakRequest request) {
      LOGGER.debug("loginWithKeycloak");
      LOGGER.debug("Logging in {}", request.payload);
      try {
         // Exchange code for a token from Keycloak
         AccessTokenResponse tokenResponse = ServerRequest.invokeAccessCodeToToken(keycloak, request.payload.code(),
                                                                                   keycloakConfig.getRedirectUri(),
                                                                                   request.payload.sessionId());
         LOGGER.debug("Token Exchange succeeded!");

         String tokenString = tokenResponse.getToken();
         String idTokenString = tokenResponse.getIdToken();

         AdapterTokenVerifier.VerifiedTokens tokens = AdapterTokenVerifier.verifyTokens(tokenString, idTokenString,
                                                                                        keycloak);
         LOGGER.debug("Token Verification succeeded!");
         AccessToken token = tokens.getAccessToken();
         LOGGER.debug("Is user already registered?");
         String email = token.getEmail();
         User user = PsqlQueries.getUserByEmail(email);
         if (user == null) {
            // Register new user
            LOGGER.debug("User registration ... " + email);
            User newUser = User.buildUserFromToken(token);
            user = PsqlQueries.registerUser(newUser);
         }
         LOGGER.debug("User has signed in : " + user.getEmail());
         request.replyTo.tell(new EventLoginWithKeycloakResponse(user));
         return Behaviors.same();
      } catch (SQLException e) {
         LOGGER.error("failed sql query: " + e.getMessage());
      } catch (VerificationException e) {
         LOGGER.error("failed verification of token: " + e.getMessage());
      } catch (ServerRequest.HttpFailure failure) {
         LOGGER.error("failed to turn code into token");
         LOGGER.error("status from server: " + failure.getStatus());
         if (failure.getError() != null && !failure.getError().trim().isEmpty()) {
            LOGGER.error("   " + failure.getError());
         }
      } catch (IOException e) {
         LOGGER.error("failed to turn code into token", e);
      }
      request.replyTo.tell(new EventLoginWithKeycloakResponse(null));
      return Behaviors.same();
   }

   private Behavior<Event> eventGetMatchesForReviewHandler(final EventGetMatchesForReviewReq request) {
      LOGGER.debug("getMatchesForReview");
      var recs = PsqlQueries.getMatchesForReview();
      request.replyTo.tell(new EventGetMatchesForReviewListRsp(recs));
      return Behaviors.same();
   }

   private Behavior<Event> eventGetGoldenRecordCountHandler(final EventGetGoldenRecordCountReq request) {
      LOGGER.debug("getGoldenRecordCount");
      libMPI.startTransaction();
      final var count = libMPI.countGoldenRecords();
      libMPI.closeTransaction();
      request.replyTo.tell(new EventGetGoldenRecordCountRsp(count));
      return Behaviors.same();
   }

   private Behavior<Event> eventGetPatientCountHandler(final EventGetPatientCountReq request) {
      LOGGER.debug("getDocumentCount");
      libMPI.startTransaction();
      final var count = libMPI.countPatientRecords();
      libMPI.closeTransaction();
      request.replyTo.tell(new EventGetPatientCountRsp(count));
      return Behaviors.same();
   }

   private Behavior<Event> eventGetNumberOfRecordsHandler(final EventGetNumberOfRecordsReq request) {
      LOGGER.debug("getNumberOfRecords");
      libMPI.startTransaction();
      var recs = libMPI.countGoldenRecords();
      var docs = libMPI.countPatientRecords();
      libMPI.closeTransaction();
      request.replyTo.tell(new BackEnd.EventGetNumberOfRecordsRsp(recs, docs));
      return Behaviors.same();
   }

//   private Behavior<Event> eventGetGoldenIdListByPredicateHandler(EventGetGoldenIdListByPredicateReq request) {
//      LOGGER.debug("getGoldenRecordsByPredicate");
//      libMPI.startTransaction();
//      var recs = libMPI.getGoldenIdListByPredicate(request.predicate, request.val);
//      request.replyTo.tell(new EventGetGoldenIdListByPredicateRsp(recs));
//      libMPI.closeTransaction();
//      return Behaviors.same();
//   }

   private Behavior<Event> eventGetGoldenIdListHandler(final EventGetGoldenIdListReq request) {
      LOGGER.debug("getGoldenIdList");
      libMPI.startTransaction();
      var recs = libMPI.getGoldenIdList();
      request.replyTo.tell(new EventGetGoldenIdListRsp(recs));
      libMPI.closeTransaction();
      return Behaviors.same();
   }

   private Behavior<Event> findGoldenRecordByUidEventHandler(final EventFindGoldenRecordByUidRequest request) {
      LOGGER.debug("findGoldenRecordByUidEventHandler");
      libMPI.startTransaction();
      final var rec = libMPI.getGoldenRecord(request.uid);
      request.replyTo.tell(new EventFindGoldenRecordByUidResponse(rec));
      libMPI.closeTransaction();
      return Behaviors.same();
   }

   private Behavior<Event> eventGetExpandedGoldenRecordsHandler(final EventGetExpandedGoldenRecordsReq request) {
      LOGGER.debug("getGoldenRecordDocuments");
      libMPI.startTransaction();
      final var mpiExpandedGoldenRecordList = libMPI.getExpandedGoldenRecords(request.uids);
      request.replyTo.tell(new EventGetExpandedGoldenRecordsRsp(mpiExpandedGoldenRecordList));
      libMPI.closeTransaction();
      return Behaviors.same();
   }

   private Behavior<Event> eventGetExpandedPatientRecordsHandler(final EventGetExpandedPatientRecordsReq request) {
      LOGGER.debug("getExpandedPatients");
      libMPI.startTransaction();
      final var patients = libMPI.getMpiExpandedPatients(request.uids);
      request.replyTo.tell(new EventGetExpandedPatientRecordsRsp(patients));
      libMPI.closeTransaction();
      return Behaviors.same();
   }

   private Behavior<Event> findPatientByUidEventHandler(final EventFindPatientByUidRequest request) {
      LOGGER.debug("findPatientByUidEventHandler");
      libMPI.startTransaction();
      final var patient = libMPI.getPatientRecord(request.uid);
      request.replyTo.tell(new EventFindPatientRecordByUidResponse(patient));
      libMPI.closeTransaction();
      return Behaviors.same();
   }

   private Behavior<Event> eventGetCandidatesHandler(final EventGetCandidatesReq request) {
      LOGGER.debug("getCandidates");
      LOGGER.debug("{} {}", request.docID, request.mu);
      libMPI.startTransaction();
      final var patient = libMPI.getPatientRecord(request.docID);
      final var recs = libMPI.getCandidates(patient.demographicData(), true);

      CustomLinkerProbabilistic.updateMU(request.mu);
      CustomLinkerProbabilistic.checkUpdatedMU();
      final var candidates = recs
            .stream()
            .map(candidate -> new EventGetCandidatesRsp.Candidate(candidate,
                                                                  CustomLinkerProbabilistic.probabilisticScore(candidate.demographicData(),
                                                                                                               patient.demographicData())))
            .toList();
      request.replyTo.tell(new EventGetCandidatesRsp(Either.right(candidates)));
      libMPI.closeTransaction();
      return Behaviors.same();
   }

   private Behavior<Event> eventUpdateGoldenRecordHandler(final EventUpdateGoldenRecordRequest request) {
      final var fields = request.fields();
      final var uid = request.uid();
      libMPI.startTransaction();
      List<GoldenRecordUpdateRequestPayload.Field> updatedFields = new ArrayList();
      LOGGER.debug("Golden record {} update.", uid);
      for (int i = 0; i < fields.size(); i++) {
         final var field = fields.get(i);
         final var result = libMPI.updateGoldenRecordField(uid, field.name(), field.value());
         if (result) {
            LOGGER.debug("Golden record field update {} has been successfully updated.", field);
            updatedFields.add(fields.get(i));
         } else {
            LOGGER.debug("Golden record field update {} update has failed.", field);
         }
      }
      request.replyTo.tell(new EventUpdateGoldenRecordResponse(updatedFields));
      libMPI.closeTransaction();
      return Behaviors.same();
   }

   private Behavior<Event> eventPatchLinkHandler(final EventPatchLinkReq request) {
      var listLinkInfo = libMPI.updateLink(
            request.goldenID, request.newGoldenID, request.docID, request.score);
      request.replyTo.tell(new EventPatchLinkRsp(listLinkInfo));
      return Behaviors.same();
   }

   private Behavior<Event> eventPatchUnLinkHandler(final EventPatchUnLinkReq request) {
      var linkInfo = libMPI.unLink(
            request.goldenID, request.docID, request.score);
      request.replyTo.tell(new EventPatchUnLinkRsp(linkInfo));
      return Behaviors.same();
   }

   private Behavior<Event> eventNotificationRequestHandler(EventNotificationRequestReq request) {
      try {
         PsqlQueries.updateNotificationState(request.notificationId, request.state);
      } catch (SQLException exception) {
         LOGGER.error(exception.getMessage());
      }
      request.replyTo.tell(new EventNotificationRequestRsp());
      return Behaviors.same();
   }

   private Behavior<Event> eventPostCsvFileRequestHandler(EventPostCsvFileRequest request) throws IOException {
      File file = request.file();
      FileInfo info = request.info();
      try {
         Files.copy(file.toPath(), Paths.get("/app/csv/" + file.getName()));
         LOGGER.debug("File moved successfully");
         file.delete();
      } catch (IOException e) {LOGGER.error(e);}
      request.replyTo.tell(new EventPostCsvFileResponse());
      return Behaviors.same();
   }

   interface Event {
   }

   interface EventResponse {
   }

   public record EventGetGoldenRecordCountReq(ActorRef<EventGetGoldenRecordCountRsp> replyTo) implements Event {
   }

   public record EventGetGoldenRecordCountRsp(long count) implements EventResponse {
   }

   public record EventGetPatientCountReq(ActorRef<EventGetPatientCountRsp> replyTo) implements Event {
   }

   public record EventGetPatientCountRsp(long count) implements EventResponse {
   }

   public record EventGetNumberOfRecordsReq(ActorRef<EventGetNumberOfRecordsRsp> replyTo) implements Event {
   }

   public record EventGetNumberOfRecordsRsp(long goldenRecords, long patients) implements EventResponse {
   }

//   public record EventGetGoldenIdListByPredicateReq(ActorRef<EventGetGoldenIdListByPredicateRsp> replyTo,
//                                                    String predicate,
//                                                    String val) implements Event {
//   }
//
//   public record EventGetGoldenIdListByPredicateRsp(List<String> records) implements EventResponse {
//   }

   public record EventGetGoldenIdListReq(ActorRef<EventGetGoldenIdListRsp> replyTo) implements Event {
   }

   public record EventGetGoldenIdListRsp(List<String> records) implements EventResponse {
   }

   public record EventFindGoldenRecordByUidRequest(ActorRef<EventFindGoldenRecordByUidResponse> replyTo, String uid)
         implements Event {
   }

   public record EventFindGoldenRecordByUidResponse(GoldenRecord goldenRecord) implements EventResponse {
   }

   public record EventGetExpandedGoldenRecordsReq(ActorRef<EventGetExpandedGoldenRecordsRsp> replyTo,
                                                  List<String> uids) implements Event {
   }

   public record EventGetExpandedGoldenRecordsRsp(List<ExpandedGoldenRecord> expandedGoldenRecords)
         implements EventResponse {
   }

   public record EventGetExpandedPatientRecordsReq(ActorRef<EventGetExpandedPatientRecordsRsp> replyTo,
                                             List<String> uids) implements Event {
   }

   public record EventGetExpandedPatientRecordsRsp(List<ExpandedPatientRecord> expandedPatientRecords)
         implements EventResponse {
   }


   public record EventFindPatientByUidRequest(ActorRef<EventFindPatientRecordByUidResponse> replyTo,
                                              String uid) implements Event {
   }

   public record EventGetMatchesForReviewReq(ActorRef<EventGetMatchesForReviewListRsp> replyTo) implements Event {
   }

   public record EventGetMatchesForReviewListRsp(List records) implements EventResponse {
   }

   public record EventFindPatientRecordByUidResponse(PatientRecord patient)
         implements EventResponse {
   }

   public record EventUpdateGoldenRecordRequest(ActorRef<EventUpdateGoldenRecordResponse> replyTo,
                                                String uid,
                                                List<GoldenRecordUpdateRequestPayload.Field> fields) implements Event {
   }

   public record EventUpdateGoldenRecordResponse(List<GoldenRecordUpdateRequestPayload.Field> fields) implements EventResponse {
   }

   public record EventPatchLinkReq(ActorRef<EventPatchLinkRsp> replyTo,
                                   String goldenID,
                                   String newGoldenID,
                                   String docID,
                                   Float score) implements Event {
   }

   public record EventPatchLinkRsp(Either<MpiGeneralError, LinkInfo> linkInfo)
         implements EventResponse {
   }

   public record EventPatchUnLinkReq(ActorRef<EventPatchUnLinkRsp> replyTo,
                                     String goldenID,
                                     String docID,
                                     float score) implements Event {
   }

   public record EventPatchUnLinkRsp(Either<MpiGeneralError, LinkInfo> linkInfo)
         implements EventResponse {
   }

   public record EventGetCandidatesReq(ActorRef<EventGetCandidatesRsp> replyTo,
                                       String docID, CustomMU mu) implements Event {
   }

   public record EventGetCandidatesRsp(Either<MpiGeneralError, List<Candidate>> candidates) implements EventResponse {
      record Candidate(GoldenRecord goldenRecord, float score) {
      }
   }

   public record EventNotificationRequestReq(ActorRef<EventNotificationRequestRsp> replyTo,
                                             String notificationId,
                                             String state) implements Event {
   }

   public record EventNotificationRequestRsp() implements EventResponse {
   }

   /**
    * Authentication events
    */

   public record EventLoginWithKeycloakRequest(ActorRef<EventLoginWithKeycloakResponse> replyTo,
                                               OAuthCodeRequestPayload payload) implements Event {
   }

   public record EventLoginWithKeycloakResponse(User user) implements EventResponse {
   }

   /**
    * Search events
    */
   public record EventSimpleSearchGoldenRecordsRequest(ActorRef<EventSearchGoldenRecordsResponse> replyTo,
                                                       SimpleSearchRequestPayload searchRequestPayload) implements Event {
   }

   public record EventCustomSearchGoldenRecordsRequest(ActorRef<EventSearchGoldenRecordsResponse> replyTo,
                                                       CustomSearchRequestPayload searchRequestPayload) implements Event {
   }

   public record EventSearchGoldenRecordsResponse(
         LibMPIPaginatedResultSet<ExpandedGoldenRecord> records) implements EventResponse {
   }

   public record EventSimpleSearchPatientRecordsRequest(ActorRef<EventSearchPatientRecordsResponse> replyTo,
                                                        SimpleSearchRequestPayload searchRequestPayload) implements Event {
   }

   public record EventCustomSearchPatientRecordsRequest(ActorRef<EventSearchPatientRecordsResponse> replyTo,
                                                        CustomSearchRequestPayload searchRequestPayload) implements Event {
   }

   public record EventSearchPatientRecordsResponse(LibMPIPaginatedResultSet<PatientRecord> records) implements EventResponse {
   }

   public record EventPostCsvFileRequest(ActorRef<EventPostCsvFileResponse> replyTo, FileInfo info, File file)
         implements Event {
   }

   public record EventPostCsvFileResponse() implements EventResponse {
   }

}
