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
import org.jembi.jempi.libmpi.MpiServiceError;
import org.jembi.jempi.linker.CustomLinkerProbabilistic;
import org.jembi.jempi.postgres.PsqlQueries;
import org.jembi.jempi.shared.mapper.JsonToFhir;
import org.jembi.jempi.shared.models.*;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.ServerRequest;
import org.keycloak.adapters.rotation.AdapterTokenVerifier;
import org.keycloak.common.VerificationException;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.AccessTokenResponse;


import java.io.*;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.hl7.fhir.r4.model.Patient;
import ca.uhn.fhir.context.FhirContext;
public final class BackEnd extends AbstractBehavior<BackEnd.Event> {

   private static final Logger LOGGER = LogManager.getLogger(BackEnd.class);
   private static LibMPI libMPI = null;
   private AkkaAdapterConfig keycloakConfig = null;
   private KeycloakDeployment keycloak = null;

   private BackEnd(final ActorContext<Event> context) {
      super(context);
      if (libMPI == null) {
         openMPI();
      }

      // Init keycloak
      ClassLoader classLoader = getClass().getClassLoader();
      InputStream keycloakConfigStream = classLoader.getResourceAsStream("/keycloak.json");
      keycloakConfig = AkkaKeycloakDeploymentBuilder.loadAdapterConfig(keycloakConfigStream);
      keycloak = AkkaKeycloakDeploymentBuilder.build(keycloakConfig);
      LOGGER.debug("Keycloak configured, realm : {}", keycloak.getRealm());
   }

   private BackEnd(
         final ActorContext<Event> context,
         final LibMPI libMPI) {
      super(context);
      BackEnd.libMPI = libMPI;
   }

   public static Behavior<BackEnd.Event> create() {
      return Behaviors.setup(BackEnd::new);
   }

   public static Behavior<Event> create(final LibMPI lib) {
      return Behaviors.setup(context -> new BackEnd(context, lib));
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
            .onMessage(LoginWithKeycloakRequest.class, this::loginWithKeycloakHandler)
            .onMessage(GetGoldenRecordCountRequest.class, this::getGoldenRecordCountHandler)
            .onMessage(GetPatientRecordCountRequest.class, this::getPatientRecordCountHandler)
            .onMessage(GetNumberOfRecordsRequest.class, this::getNumberOfRecordsHandler)
            .onMessage(GetGoldenIdsRequest.class, this::getGoldenIdsHandler)
            .onMessage(FindExpandedGoldenRecordRequest.class, this::findExpandedGoldenRecordHandler)
            .onMessage(FindExpandedGoldenRecordsRequest.class, this::findExpandedGoldenRecordsHandler)
            .onMessage(FindExpandedPatientRecordsRequest.class, this::findExpandedPatientRecordsHandler)
            .onMessage(FindPatientRecordRequest.class, this::findPatientRecordHandler)
            .onMessage(FindCandidatesRequest.class, this::findCandidatesHandler)
            .onMessage(FindMatchesForReviewRequest.class, this::findMatchesForReviewHandler)
            .onMessage(UpdateGoldenRecordFieldsRequest.class, this::updateGoldenRecordFieldsHandler)
            .onMessage(UpdateLinkToExistingGoldenRecordRequest.class, this::updateLinkToExistingGoldenRecordHandler)
            .onMessage(UpdateLinkToNewGoldenRecordRequest.class, this::updateLinkToNewGoldenRecordHandler)
            .onMessage(UpdateNotificationStateRequest.class, this::updateNotificationStateHandler)
            .onMessage(SimpleSearchGoldenRecordsRequest.class, this::simpleSearchGoldenRecordsHandler)
            .onMessage(CustomSearchGoldenRecordsRequest.class, this::customSearchGoldenRecordsHandler)
            .onMessage(SimpleSearchPatientRecordsRequest.class, this::simpleSearchPatientRecordsHandler)
            .onMessage(CustomSearchPatientRecordsRequest.class, this::customSearchPatientRecordsHandler)
            .onMessage(UploadCsvFileRequest.class, this::uploadCsvFileHandler)
            .onMessage(MapToFhirRequest.class, this::mapToFhir)
            .build();
   }

   private Behavior<Event> mapToFhir(final MapToFhirRequest request) {
      PatientRecord payload = request.patientRecord();
      LOGGER.debug(payload);
      Patient patient = null;
      String patientJson =  null;
      try {
         patient = JsonToFhir.mapToPatientFhir(payload);
         FhirContext ctx = FhirContext.forR4();
      // Serialize the patient object to FHIR JSON
         patientJson = ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(patient);
         LOGGER.debug(patientJson);
      } catch (Exception e) {
         LOGGER.debug(e);
      }
      request.replyTo.tell(new MapToFhirResponse(patientJson));
      return Behaviors.same();
   }
   private Behavior<Event> simpleSearchGoldenRecordsHandler(final SimpleSearchGoldenRecordsRequest request) {
      SimpleSearchRequestPayload payload = request.searchRequestPayload();
      List<SimpleSearchRequestPayload.SearchParameter> parameters = payload.parameters();
      Integer offset = payload.offset();
      Integer limit = payload.limit();
      String sortBy = payload.sortBy();
      Boolean sortAsc = payload.sortAsc();
      libMPI.startTransaction();
      var recs = libMPI.simpleSearchGoldenRecords(parameters, offset, limit, sortBy, sortAsc);
      libMPI.closeTransaction();
      request.replyTo.tell(new SearchGoldenRecordsResponse(recs));
      return Behaviors.same();
   }

   private Behavior<Event> customSearchGoldenRecordsHandler(final CustomSearchGoldenRecordsRequest request) {
      CustomSearchRequestPayload payload = request.customSearchRequestPayload();
      List<SimpleSearchRequestPayload> parameters = payload.$or();
      Integer offset = payload.offset();
      Integer limit = payload.limit();
      String sortBy = payload.sortBy();
      Boolean sortAsc = payload.sortAsc();
      libMPI.startTransaction();
      var recs = libMPI.customSearchGoldenRecords(parameters, offset, limit, sortBy, sortAsc);
      libMPI.closeTransaction();
      request.replyTo.tell(new SearchGoldenRecordsResponse(recs));
      return Behaviors.same();
   }

   private Behavior<Event> simpleSearchPatientRecordsHandler(final SimpleSearchPatientRecordsRequest request) {
      SimpleSearchRequestPayload payload = request.searchRequestPayload();
      List<SimpleSearchRequestPayload.SearchParameter> parameters = payload.parameters();
      Integer offset = payload.offset();
      Integer limit = payload.limit();
      String sortBy = payload.sortBy();
      Boolean sortAsc = payload.sortAsc();
      libMPI.startTransaction();
      var recs = libMPI.simpleSearchPatientRecords(parameters, offset, limit, sortBy, sortAsc);
      libMPI.closeTransaction();
      request.replyTo.tell(new SearchPatientRecordsResponse(recs));
      return Behaviors.same();
   }

   private Behavior<Event> customSearchPatientRecordsHandler(final CustomSearchPatientRecordsRequest request) {
      CustomSearchRequestPayload payload = request.customSearchRequestPayload();
      List<SimpleSearchRequestPayload> parameters = payload.$or();
      Integer offset = payload.offset();
      Integer limit = payload.limit();
      String sortBy = payload.sortBy();
      Boolean sortAsc = payload.sortAsc();
      libMPI.startTransaction();
      var recs = libMPI.customSearchPatientRecords(parameters, offset, limit, sortBy, sortAsc);
      libMPI.closeTransaction();
      request.replyTo.tell(new SearchPatientRecordsResponse(recs));
      return Behaviors.same();
   }

   private Behavior<Event> loginWithKeycloakHandler(final LoginWithKeycloakRequest request) {
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
            LOGGER.debug("User registration ... {}", email);
            User newUser = User.buildUserFromToken(token);
            user = PsqlQueries.registerUser(newUser);
         }
         LOGGER.debug("User has signed in : {}", user.getEmail());
         request.replyTo.tell(new LoginWithKeycloakResponse(user));
         return Behaviors.same();
      } catch (SQLException e) {
         LOGGER.error("failed sql query: {}", e.getMessage());
      } catch (VerificationException e) {
         LOGGER.error("failed verification of token: {}", e.getMessage());
      } catch (ServerRequest.HttpFailure failure) {
         LOGGER.error("failed to turn code into token");
         LOGGER.error("status from server: {}", failure.getStatus());
         if (failure.getError() != null && !failure.getError().trim().isEmpty()) {
            LOGGER.error(failure.getLocalizedMessage(), failure);
         }
      } catch (IOException e) {
         LOGGER.error("failed to turn code into token", e);
      }
      request.replyTo.tell(new LoginWithKeycloakResponse(null));
      return Behaviors.same();
   }

   private Behavior<Event> findMatchesForReviewHandler(final FindMatchesForReviewRequest request) {
      LOGGER.debug("getMatchesForReview");
      var recs = PsqlQueries.getMatchesForReview();
      request.replyTo.tell(new FindMatchesForReviewResponse(recs));
      return Behaviors.same();
   }

   private Behavior<Event> getGoldenRecordCountHandler(final GetGoldenRecordCountRequest request) {
      LOGGER.debug("getGoldenRecordCount");
      try {
         libMPI.startTransaction();
         final long count = libMPI.countGoldenRecords();
         libMPI.closeTransaction();

         request.replyTo.tell(new GetGoldenRecordCountResponse(Either.right(count)));
      } catch (Exception exception) {
         LOGGER.error("libMPI.countGoldenRecords failed with error message: {}", exception.getMessage());
         request.replyTo.tell(new GetGoldenRecordCountResponse(Either.left(new MpiServiceError.GeneralError(exception.getMessage()))));
      }
      return Behaviors.same();
   }

   private Behavior<Event> getPatientRecordCountHandler(final GetPatientRecordCountRequest request) {
      LOGGER.debug("getDocumentCount");

      try {
         libMPI.startTransaction();
         final long count = libMPI.countPatientRecords();
         libMPI.closeTransaction();

         request.replyTo.tell(new GetPatientRecordCountResponse(Either.right(count)));
      } catch (Exception exception) {
         LOGGER.error("libMPI.countPatientRecords failed with error message: {}", exception.getMessage());
         request.replyTo.tell(new GetPatientRecordCountResponse(Either.left(new MpiServiceError.GeneralError(
               exception.getMessage()))));
      }
      return Behaviors.same();
   }

   private Behavior<Event> getNumberOfRecordsHandler(final GetNumberOfRecordsRequest request) {
      LOGGER.debug("getNumberOfRecords");
      libMPI.startTransaction();
      var recs = libMPI.countGoldenRecords();
      var docs = libMPI.countPatientRecords();
      libMPI.closeTransaction();
      request.replyTo.tell(new BackEnd.GetNumberOfRecordsResponse(recs, docs));
      return Behaviors.same();
   }

   private Behavior<Event> getGoldenIdsHandler(final GetGoldenIdsRequest request) {
      LOGGER.debug("getGoldenIds");
      libMPI.startTransaction();
      var recs = libMPI.findGoldenIds();
      request.replyTo.tell(new GetGoldenIdsResponse(recs));
      libMPI.closeTransaction();
      return Behaviors.same();
   }

   private Behavior<Event> findExpandedGoldenRecordHandler(final FindExpandedGoldenRecordRequest request) {
      ExpandedGoldenRecord expandedGoldenRecord = null;
      LOGGER.debug("findExpandedGoldenRecordHandler");

      try {
         libMPI.startTransaction();
         expandedGoldenRecord = libMPI.findExpandedGoldenRecord(request.goldenId);
         libMPI.closeTransaction();
      } catch (Exception exception) {
         LOGGER.error("libMPI.findExpandedGoldenRecord failed for goldenId: {} with error: {}",
                      request.goldenId,
                      exception.getMessage());
      }

      if (expandedGoldenRecord == null) {
         request.replyTo.tell(new FindExpandedGoldenRecordResponse(Either.left(new MpiServiceError.GoldenIdDoesNotExistError(
               "Golden Record does not exist",
               request.goldenId))));
      } else {
         request.replyTo.tell(new FindExpandedGoldenRecordResponse(Either.right(expandedGoldenRecord)));
      }

      return Behaviors.same();
   }

   private Behavior<Event> findExpandedGoldenRecordsHandler(final FindExpandedGoldenRecordsRequest request) {
      List<ExpandedGoldenRecord> goldenRecords = null;
      LOGGER.debug("getExpandedGoldenRecords");

      try {
         libMPI.startTransaction();
         goldenRecords = libMPI.findExpandedGoldenRecords(request.goldenIds);
         libMPI.closeTransaction();
      } catch (Exception exception) {
         LOGGER.error("libMPI.findExpandedGoldenRecords failed for goldenIds: {} with error: {}",
                      request.goldenIds,
                      exception.getMessage());
      }

      if (goldenRecords == null) {
         request.replyTo.tell(new FindExpandedGoldenRecordsResponse(Either.left(new MpiServiceError.GoldenIdDoesNotExistError(
               "Golden Records do not exist",
               Arrays.asList(request.goldenIds).toString()))));
      } else {
         request.replyTo.tell(new FindExpandedGoldenRecordsResponse(Either.right(goldenRecords)));
      }

      return Behaviors.same();
   }

   private Behavior<Event> findExpandedPatientRecordsHandler(final FindExpandedPatientRecordsRequest request) {
      List<ExpandedPatientRecord> expandedPatientRecords = null;
      LOGGER.debug("getExpandedPatients");

      try {
         libMPI.startTransaction();
         expandedPatientRecords = libMPI.findExpandedPatientRecords(request.patientIds);
         libMPI.closeTransaction();
      } catch (Exception exception) {
         LOGGER.error("libMPI.findExpandedPatientRecords failed for patientIds: {} with error: {}",
                      request.patientIds,
                      exception.getMessage());
      }

      if (expandedPatientRecords == null) {
         request.replyTo.tell(new FindExpandedPatientRecordsResponse(Either.left(new MpiServiceError.PatientIdDoesNotExistError(
               "Patient Records do not exist",
               Arrays.asList(request.patientIds).toString()))));
      } else {
         request.replyTo.tell(new FindExpandedPatientRecordsResponse(Either.right(expandedPatientRecords)));
      }
      return Behaviors.same();
   }

   private Behavior<Event> findPatientRecordHandler(final FindPatientRecordRequest request) {
      PatientRecord patientRecord = null;
      LOGGER.debug("findPatientRecordHandler");

      try {
         libMPI.startTransaction();
         patientRecord = libMPI.findPatientRecord(request.patientId);
         libMPI.closeTransaction();
      } catch (Exception exception) {
         LOGGER.error("libMPI.findPatientRecord failed for patientId: {} with error: {}",
                      request.patientId,
                      exception.getMessage());
      }

      if (patientRecord == null) {
         request.replyTo.tell(new FindPatientRecordResponse(Either.left(new MpiServiceError.PatientIdDoesNotExistError(
               "Patient not found",
               request.patientId))));
      } else {
         request.replyTo.tell(new FindPatientRecordResponse(Either.right(patientRecord)));
      }

      return Behaviors.same();
   }

   private Behavior<Event> findCandidatesHandler(final FindCandidatesRequest request) {
      LOGGER.debug("getCandidates");
      LOGGER.debug("{} {}", request.patientId, request.mu);
      PatientRecord patientRecord = null;
      List<GoldenRecord> goldenRecords = null;
      List<FindCandidatesResponse.Candidate> candidates = null;

      try {
         libMPI.startTransaction();
         patientRecord = libMPI.findPatientRecord(request.patientId);

         try {
            goldenRecords = libMPI.getCandidates(patientRecord.demographicData(), true);
         } catch (Exception exception) {
            LOGGER.error("libMPI.getCandidates failed to find patientRecord.demographicData: {}",
                         patientRecord.demographicData());
            request.replyTo.tell(new FindCandidatesResponse(Either.left(new MpiServiceError.CandidatesNotFoundError(
                  "Candidates(golden records) not found with demographic data for patientId",
                  request.patientId))));
            return Behaviors.same();
         }

         libMPI.closeTransaction();

         final var patientDemographic = patientRecord.demographicData();
         CustomLinkerProbabilistic.updateMU(request.mu);
         CustomLinkerProbabilistic.checkUpdatedMU();
         candidates = goldenRecords
               .stream()
               .map(candidate -> new FindCandidatesResponse.Candidate(candidate,
                                                                      CustomLinkerProbabilistic.probabilisticScore(candidate.demographicData(),
                                                                                                                   patientDemographic)))
               .toList();
         request.replyTo.tell(new FindCandidatesResponse(Either.right(candidates)));
      } catch (Exception exception) {
         LOGGER.error("findCandidatesHandler failed to find patientId: {}", request.patientId);
         request.replyTo.tell(new FindCandidatesResponse(Either.left(new MpiServiceError.PatientIdDoesNotExistError(
               "Patient not found",
               request.patientId))));
      }

      return Behaviors.same();
   }

   private Behavior<Event> updateGoldenRecordFieldsHandler(final UpdateGoldenRecordFieldsRequest request) {
      final var fields = request.fields();
      final var goldenId = request.goldenId;
      libMPI.startTransaction();
      final var updatedFields = new ArrayList<GoldenRecordUpdateRequestPayload.Field>();
      LOGGER.debug("Golden record {} update.", goldenId);
      for (final GoldenRecordUpdateRequestPayload.Field field : fields) {
         final var result = libMPI.updateGoldenRecordField(goldenId, field.name(), field.value());
         if (result) {
            LOGGER.debug("Golden record field update {} has been successfully updated.", field);
            updatedFields.add(field);
         } else {
            LOGGER.debug("Golden record field update {} update has failed.", field);
         }
      }
      request.replyTo.tell(new UpdateGoldenRecordFieldsResponse(updatedFields));
      libMPI.closeTransaction();
      return Behaviors.same();
   }

   private Behavior<Event> updateLinkToExistingGoldenRecordHandler(final UpdateLinkToExistingGoldenRecordRequest request) {
      var result = libMPI.updateLink(request.currentGoldenId, request.newGoldenId, request.patientId, request.score);
      request.replyTo.tell(new UpdateLinkToExistingGoldenRecordResponse(result));
      return Behaviors.same();
   }

   private Behavior<Event> updateLinkToNewGoldenRecordHandler(final UpdateLinkToNewGoldenRecordRequest request) {
      var linkInfo = libMPI.linkToNewGoldenRecord(request.currentGoldenId, request.patientId, request.score);
      request.replyTo.tell(new UpdateLinkToNewGoldenRecordResponse(linkInfo));
      return Behaviors.same();
   }

   private Behavior<Event> updateNotificationStateHandler(final UpdateNotificationStateRequest request) {
      try {
         PsqlQueries.updateNotificationState(request.notificationId, request.state);
      } catch (SQLException exception) {
         LOGGER.error(exception.getMessage());
      }
      request.replyTo.tell(new UpdateNotificationStateRespnse());
      return Behaviors.same();
   }

   private Behavior<Event> uploadCsvFileHandler(final UploadCsvFileRequest request) throws IOException {
      File file = request.file();
      try {
         Files.copy(file.toPath(), Paths.get("/app/csv/" + file.getName()));
//       final var result = file.delete()
         java.nio.file.Files.delete(file.toPath());
         LOGGER.debug("File moved successfully");
      } catch (NoSuchFileException e) {
         LOGGER.debug("No such file");
      } catch (SecurityException | IOException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
      }
      request.replyTo.tell(new UploadCsvFileResponse());
      return Behaviors.same();
   }

   interface Event {
   }

   interface EventResponse {
   }

   public record GetGoldenRecordCountRequest(ActorRef<GetGoldenRecordCountResponse> replyTo) implements Event {
   }

   public record GetGoldenRecordCountResponse(Either<MpiGeneralError, Long> count) implements EventResponse {
   }

   public record GetPatientRecordCountRequest(ActorRef<GetPatientRecordCountResponse> replyTo) implements Event {
   }

   public record GetPatientRecordCountResponse(Either<MpiGeneralError, Long> count) implements EventResponse {
   }

   public record GetNumberOfRecordsRequest(ActorRef<GetNumberOfRecordsResponse> replyTo) implements Event {
   }

   public record GetNumberOfRecordsResponse(
         long goldenRecords,
         long patientRecords) implements EventResponse {
   }

   public record GetGoldenIdsRequest(ActorRef<GetGoldenIdsResponse> replyTo) implements Event {
   }

   public record GetGoldenIdsResponse(List<String> records) implements EventResponse {
   }

   public record FindExpandedGoldenRecordRequest(
         ActorRef<FindExpandedGoldenRecordResponse> replyTo,
         String goldenId)
         implements Event {
   }

   public record FindExpandedGoldenRecordResponse(Either<MpiGeneralError, ExpandedGoldenRecord> goldenRecord) implements EventResponse {
   }

   public record FindExpandedGoldenRecordsRequest(
         ActorRef<FindExpandedGoldenRecordsResponse> replyTo,
         List<String> goldenIds) implements Event {
   }

   public record FindExpandedGoldenRecordsResponse(Either<MpiGeneralError, List<ExpandedGoldenRecord>> expandedGoldenRecords)
         implements EventResponse {
   }

   public record FindExpandedPatientRecordsRequest(
         ActorRef<FindExpandedPatientRecordsResponse> replyTo,
         List<String> patientIds) implements Event {
   }

   public record FindExpandedPatientRecordsResponse(Either<MpiGeneralError, List<ExpandedPatientRecord>> expandedPatientRecords)
         implements EventResponse {
   }

   public record FindPatientRecordRequest(
         ActorRef<FindPatientRecordResponse> replyTo,
         String patientId) implements Event {
   }

   public record FindPatientRecordResponse(Either<MpiGeneralError, PatientRecord> patient)
         implements EventResponse {
   }

   public record FindMatchesForReviewRequest(ActorRef<FindMatchesForReviewResponse> replyTo) implements Event {
   }

   public record FindMatchesForReviewResponse(List<HashMap<String, Object>> records) implements EventResponse {
   }

   public record UpdateGoldenRecordFieldsRequest(
         ActorRef<UpdateGoldenRecordFieldsResponse> replyTo,
         String goldenId,
         List<GoldenRecordUpdateRequestPayload.Field> fields) implements Event {
   }

   public record UpdateGoldenRecordFieldsResponse(List<GoldenRecordUpdateRequestPayload.Field> fields) implements EventResponse {
   }

   public record UpdateLinkToExistingGoldenRecordRequest(
         ActorRef<UpdateLinkToExistingGoldenRecordResponse> replyTo,
         String currentGoldenId,
         String newGoldenId,
         String patientId,
         Float score) implements Event {
   }

   public record UpdateLinkToExistingGoldenRecordResponse(Either<MpiGeneralError, LinkInfo> linkInfo)
         implements EventResponse {
   }

   public record UpdateLinkToNewGoldenRecordRequest(
         ActorRef<UpdateLinkToNewGoldenRecordResponse> replyTo,
         String currentGoldenId,
         String patientId,
         float score) implements Event {
   }

   public record UpdateLinkToNewGoldenRecordResponse(Either<MpiGeneralError, LinkInfo> linkInfo)
         implements EventResponse {
   }

   public record FindCandidatesRequest(
         ActorRef<FindCandidatesResponse> replyTo,
         String patientId,
         CustomMU mu) implements Event {
   }

   public record FindCandidatesResponse(Either<MpiGeneralError, List<Candidate>> candidates) implements EventResponse {
      record Candidate(
            GoldenRecord goldenRecord,
            float score) {
      }
   }

   public record UpdateNotificationStateRequest(
         ActorRef<UpdateNotificationStateRespnse> replyTo,
         String notificationId,
         String state) implements Event {
   }

   public record UpdateNotificationStateRespnse() implements EventResponse {
   }

   /**
    * Authentication events
    */

   public record LoginWithKeycloakRequest(
         ActorRef<LoginWithKeycloakResponse> replyTo,
         OAuthCodeRequestPayload payload) implements Event {
   }

   public record LoginWithKeycloakResponse(User user) implements EventResponse {
   }

   /**
    * Search events
    */
   public record SimpleSearchGoldenRecordsRequest(
         ActorRef<SearchGoldenRecordsResponse> replyTo,
         SimpleSearchRequestPayload searchRequestPayload) implements Event {
   }

   public record CustomSearchGoldenRecordsRequest(
         ActorRef<SearchGoldenRecordsResponse> replyTo,
         CustomSearchRequestPayload customSearchRequestPayload) implements Event {
   }

   public record MapToFhirRequest(
           ActorRef<MapToFhirResponse> replyTo,
           PatientRecord patientRecord) implements Event {
   }

   public record MapToFhirResponse(String patient) implements EventResponse {
   }

   public record SearchGoldenRecordsResponse(
         LibMPIPaginatedResultSet<ExpandedGoldenRecord> records) implements EventResponse {
   }

   public record SimpleSearchPatientRecordsRequest(
         ActorRef<SearchPatientRecordsResponse> replyTo,
         SimpleSearchRequestPayload searchRequestPayload) implements Event {
   }

   public record CustomSearchPatientRecordsRequest(
         ActorRef<SearchPatientRecordsResponse> replyTo,
         CustomSearchRequestPayload customSearchRequestPayload) implements Event {
   }

   public record SearchPatientRecordsResponse(
         LibMPIPaginatedResultSet<PatientRecord> records) implements EventResponse {
   }

   public record UploadCsvFileRequest(
         ActorRef<UploadCsvFileResponse> replyTo,
         FileInfo info,
         File file)
         implements Event {
   }

   public record UploadCsvFileResponse() implements EventResponse {
   }

}
