package org.jembi.jempi.api;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.AskPattern;
import akka.dispatch.MessageDispatcher;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.*;
import akka.http.javadsl.server.Route;
import akka.http.javadsl.server.directives.FileInfo;
import ch.megard.akka.http.cors.javadsl.settings.CorsSettings;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.softwaremill.session.*;
import com.softwaremill.session.javadsl.HttpSessionAwareDirectives;
import com.softwaremill.session.javadsl.InMemoryRefreshTokenStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.AppConfig;
import org.jembi.jempi.api.models.OAuthCodeRequestPayload;
import org.jembi.jempi.api.models.User;
import org.jembi.jempi.api.session.UserSession;
import org.jembi.jempi.libmpi.MpiGeneralError;
import org.jembi.jempi.libmpi.MpiServiceError;
import org.jembi.jempi.shared.models.*;
import org.jembi.jempi.shared.utils.AppUtils;
import org.jembi.jempi.shared.utils.CustomSearchRequestPayload;
import org.jembi.jempi.shared.utils.RecordType;
import org.jembi.jempi.shared.utils.SimpleSearchRequestPayload;
import org.json.simple.JSONArray;

import java.io.File;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static akka.http.javadsl.server.PathMatchers.segment;
import static ch.megard.akka.http.cors.javadsl.CorsDirectives.cors;
import static com.softwaremill.session.javadsl.SessionTransports.CookieST;

public final class HttpServer extends HttpSessionAwareDirectives<UserSession> {

   private static final Logger LOGGER = LogManager.getLogger(HttpServer.class);

   private static final SessionEncoder<UserSession> BASIC_ENCODER = new BasicSessionEncoder<>(UserSession.getSerializer());

   // in-memory refresh token storage
   private static final RefreshTokenStorage<UserSession> REFRESH_TOKEN_STORAGE = new InMemoryRefreshTokenStorage<UserSession>() {
      @Override
      public void log(final String msg) {
         LOGGER.info(msg);
      }
   };
   private static final Function<Entry<String, String>, String> PARAM_STRING = Entry::getValue;
   private final Refreshable<UserSession> refreshable;
   private final SetSessionTransport sessionTransport;
   private CompletionStage<ServerBinding> binding = null;
   private Http http = null;

   public HttpServer(final MessageDispatcher dispatcher) {
      super(new SessionManager<>(SessionConfig.defaultConfig(AppConfig.SESSION_SECRET), BASIC_ENCODER));

      // use Refreshable for sessions, which needs to be refreshed or OneOff otherwise
      // using Refreshable, a refresh token is set in form of a cookie or a custom header
      refreshable = new Refreshable<>(getSessionManager(), REFRESH_TOKEN_STORAGE, dispatcher);

      // set the session transport - based on Cookies (or Headers)
      sessionTransport = CookieST;
   }

   void close(final ActorSystem<Void> actorSystem) {
      binding.thenCompose(ServerBinding::unbind) // trigger unbinding from the port
             .thenAccept(unbound -> actorSystem.terminate()); // and shutdown when done
   }

   void open(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final JSONArray fields) {
      http = Http.get(actorSystem);
      binding = http.newServerAt(AppConfig.HTTP_SERVER_HOST, AppConfig.HTTP_SERVER_PORT)
                    .bind(AppConfig.AKKA_HTTP_SESSION_ENABLED
                                ? this.createCorsRoutes(actorSystem, backEnd, fields)
                                : this.createRoutes(actorSystem, backEnd));
      LOGGER.info("Server online at http://{}:{}", AppConfig.HTTP_SERVER_HOST, AppConfig.HTTP_SERVER_PORT);
   }

   /*
    *************************** ASK BACKEND ***************************
    */

   private CompletionStage<BackEnd.EventGetGoldenRecordCountRsp> askGoldenRecordCount(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      CompletionStage<BackEnd.EventGetGoldenRecordCountRsp> stage = AskPattern
            .ask(backEnd,
                 BackEnd.EventGetGoldenRecordCountReq::new,
                 java.time.Duration.ofSeconds(10),
                 actorSystem.scheduler());
      return stage.thenApply(response -> response);
   }

   private CompletionStage<BackEnd.EventGetPatientCountRsp> askPatientCount(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      LOGGER.debug("getPatientCount");
      CompletionStage<BackEnd.EventGetPatientCountRsp> stage = AskPattern
            .ask(backEnd,
                 BackEnd.EventGetPatientCountReq::new,
                 java.time.Duration.ofSeconds(10),
                 actorSystem.scheduler());
      return stage.thenApply(response -> response);
   }

   private CompletionStage<BackEnd.EventGetNumberOfRecordsRsp> askNumberOfRecords(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      LOGGER.debug("getNumberOfRecords");
      CompletionStage<BackEnd.EventGetNumberOfRecordsRsp> stage = AskPattern
            .ask(backEnd,
                 BackEnd.EventGetNumberOfRecordsReq::new,
                 java.time.Duration.ofSeconds(10),
                 actorSystem.scheduler());
      return stage.thenApply(response -> response);
   }

   private CompletionStage<BackEnd.EventGetGoldenIdsRsp> askGoldenIds(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      LOGGER.debug("getGoldenIds");
      CompletionStage<BackEnd.EventGetGoldenIdsRsp> stage = AskPattern
            .ask(backEnd,
                 BackEnd.EventGetGoldenIdsReq::new,
                 java.time.Duration.ofSeconds(30),
                 actorSystem.scheduler());
      return stage.thenApply(response -> response);
   }

   private CompletionStage<BackEnd.EventGetMatchesForReviewListRsp> askMatchesForReviewList(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      CompletionStage<BackEnd.EventGetMatchesForReviewListRsp> stage = AskPattern
            .ask(backEnd,
                 BackEnd.EventGetMatchesForReviewReq::new,
                 java.time.Duration.ofSeconds(30),
                 actorSystem.scheduler());
      return stage.thenApply(response -> response);
   }

   private CompletionStage<BackEnd.EventFindExpandedGoldenRecordResponse> askGoldenRecordByUid(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final String uid) {
      LOGGER.debug("findGoldenRecordById");
      final CompletionStage<BackEnd.EventFindExpandedGoldenRecordResponse> stage = AskPattern
            .ask(backEnd,
                 replyTo -> new BackEnd.EventFindExpandedGoldenRecordRequest(replyTo, uid),
                 java.time.Duration.ofSeconds(5),
                 actorSystem.scheduler());
      return stage.thenApply(response -> response);
   }

   private CompletionStage<BackEnd.EventFindPatientRecordByUidResponse> askPatientRecordByUid(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final String uid) {
      LOGGER.debug("findPatientRecordById : " + uid);
      final CompletionStage<BackEnd.EventFindPatientRecordByUidResponse> stage = AskPattern
            .ask(backEnd,
                 replyTo -> new BackEnd.EventFindPatientByUidRequest(replyTo, uid),
                 java.time.Duration.ofSeconds(5),
                 actorSystem.scheduler());
      return stage.thenApply(response -> response);
   }

   private CompletionStage<BackEnd.EventGetCandidatesRsp> askCandidates(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final String uid,
         final CustomMU mu) {
      LOGGER.debug("getCandidates");
      CompletionStage<BackEnd.EventGetCandidatesRsp> stage = AskPattern
            .ask(backEnd,
                 replyTo -> new BackEnd.EventGetCandidatesReq(replyTo, uid, mu),
                 java.time.Duration.ofSeconds(5),
                 actorSystem.scheduler());
      return stage.thenApply(response -> response);
   }

   private CompletionStage<BackEnd.EventFindExpandedGoldenRecordsResponse> askExpandedGoldenRecords(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final List<String> uid) {
      LOGGER.debug("getExpandedGoldenRecords");
      CompletionStage<BackEnd.EventFindExpandedGoldenRecordsResponse> stage = AskPattern
            .ask(backEnd,
                 replyTo -> new BackEnd.EventFindExpandedGoldenRecordsRequest(replyTo, uid),
                 java.time.Duration.ofSeconds(6),
                 actorSystem.scheduler());
      return stage.thenApply(response -> response);
   }

   private CompletionStage<BackEnd.EventGetExpandedPatientRecordsRsp> askExpandedPatientRecords(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final List<String> uidList) {
      LOGGER.debug("getExpandedPatients");
      CompletionStage<BackEnd.EventGetExpandedPatientRecordsRsp> stage = AskPattern
            .ask(backEnd,
                 replyTo -> new BackEnd.EventGetExpandedPatientRecordsReq(replyTo, uidList),
                 java.time.Duration.ofSeconds(6),
                 actorSystem.scheduler());
      return stage.thenApply(response -> response);
   }

   private CompletionStage<BackEnd.EventUpdateGoldenRecordResponse> askUpdateGoldenRecord(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final String uid,
         final GoldenRecordUpdateRequestPayload payload) {
      LOGGER.debug("updateGoldenRecord");
      CompletionStage<BackEnd.EventUpdateGoldenRecordResponse> stage = AskPattern
            .ask(backEnd,
                 replyTo -> new BackEnd.EventUpdateGoldenRecordRequest(replyTo, uid, payload.fields()),
                 java.time.Duration.ofSeconds(6),
                 actorSystem.scheduler());
      return stage.thenApply(response -> response);
   }

   private CompletionStage<BackEnd.EventPatchLinkRsp> askLink(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final String goldenID,
         final String newGoldenID,
         final String patientID,
         final Float score) {
      LOGGER.debug("patchLink");
      final CompletionStage<BackEnd.EventPatchLinkRsp> stage = AskPattern
            .ask(backEnd,
                 replyTo -> new BackEnd.EventPatchLinkReq(replyTo, goldenID, newGoldenID, patientID, score),
                 java.time.Duration.ofSeconds(6),
                 actorSystem.scheduler());
      return stage.thenApply(response -> response);
   }

   private CompletionStage<BackEnd.EventPatchUnLinkRsp> askUnLink(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final String goldenID,
         final String patientID) {
      LOGGER.debug("patchUnLink");
      final CompletionStage<BackEnd.EventPatchUnLinkRsp> stage = AskPattern
            .ask(backEnd,
                 replyTo -> new BackEnd.EventPatchUnLinkReq(replyTo, goldenID, patientID, 2.0F),
                 java.time.Duration.ofSeconds(6),
                 actorSystem.scheduler());
      return stage.thenApply(response -> response);
   }

   private CompletionStage<BackEnd.EventResponse> askSimpleSearchGoldenRecords(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final SimpleSearchRequestPayload searchRequestPayload) {
      CompletionStage<BackEnd.EventSearchGoldenRecordsResponse> stage = AskPattern
            .ask(backEnd,
                 replyTo -> new BackEnd.EventSimpleSearchGoldenRecordsRequest(replyTo, searchRequestPayload),
                 java.time.Duration.ofSeconds(11),
                 actorSystem.scheduler());
      return stage.thenApply(response -> response);
   }

   private CompletionStage<BackEnd.EventResponse> askCustomSearchGoldenRecords(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final CustomSearchRequestPayload searchRequestPayload) {
      CompletionStage<BackEnd.EventSearchGoldenRecordsResponse> stage = AskPattern
            .ask(backEnd,
                 replyTo -> new BackEnd.EventCustomSearchGoldenRecordsRequest(replyTo, searchRequestPayload),
                 java.time.Duration.ofSeconds(11),
                 actorSystem.scheduler());
      return stage.thenApply(response -> response);
   }

   private CompletionStage<BackEnd.EventResponse> askSimpleSearchPatientRecords(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final SimpleSearchRequestPayload simpleSearchRequestPayload) {
      CompletionStage<BackEnd.EventSearchPatientRecordsResponse> stage = AskPattern
            .ask(backEnd,
                 replyTo -> new BackEnd.EventSimpleSearchPatientRecordsRequest(replyTo, simpleSearchRequestPayload),
                 java.time.Duration.ofSeconds(11),
                 actorSystem.scheduler());
      return stage.thenApply(response -> response);
   }

   private CompletionStage<BackEnd.EventResponse> askCustomSearchPatientRecords(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final CustomSearchRequestPayload searchRequestPayload) {
      CompletionStage<BackEnd.EventSearchPatientRecordsResponse> stage = AskPattern
            .ask(backEnd,
                 replyTo -> new BackEnd.EventCustomSearchPatientRecordsRequest(replyTo, searchRequestPayload),
                 java.time.Duration.ofSeconds(11),
                 actorSystem.scheduler());
      return stage.thenApply(response -> response);
   }

   private CompletionStage<BackEnd.EventNotificationRequestRsp> askNotificationRequest(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final NotificationRequest notificationRequest) {
      CompletionStage<BackEnd.EventNotificationRequestRsp> stage = AskPattern
            .ask(backEnd,
                 replyTo -> new BackEnd.EventNotificationRequestReq(replyTo,
                                                                    notificationRequest.notificationId(),
                                                                    notificationRequest.state()),
                 java.time.Duration.ofSeconds(11),
                 actorSystem.scheduler());
      return stage.thenApply(response -> response);
   }

   private CompletionStage<BackEnd.EventLoginWithKeycloakResponse> askLoginWithKeycloakRequest(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final OAuthCodeRequestPayload body) {
      CompletionStage<BackEnd.EventLoginWithKeycloakResponse> stage = AskPattern
            .ask(backEnd,
                 replyTo -> new BackEnd.EventLoginWithKeycloakRequest(replyTo, body),
                 java.time.Duration.ofSeconds(11),
                 actorSystem.scheduler());
      return stage.thenApply(response -> response);
   }

   private CompletionStage<BackEnd.EventPostCsvFileResponse> askUploadRequest(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final FileInfo info,
         final File file) {
      CompletionStage<BackEnd.EventPostCsvFileResponse> stage = AskPattern
            .ask(backEnd,
                 replyTo -> new BackEnd.EventPostCsvFileRequest(replyTo, info, file),
                 java.time.Duration.ofSeconds(11),
                 actorSystem.scheduler());
      return stage.thenApply(response -> response);
   }

   /*
    *************************** PROXY ***************************
    */

   private CompletionStage<HttpResponse> proxyPostCalculateScores(final CalculateScoresRequest body) throws JsonProcessingException {
      final var request = HttpRequest
            .create("http://linker:50000/JeMPI/calculate-scores")
            .withMethod(HttpMethods.POST)
            .withEntity(ContentTypes.APPLICATION_JSON, AppUtils.OBJECT_MAPPER.writeValueAsBytes(body));
      final var stage = http.singleRequest(request);
      return stage.thenApply(response -> response);
   }

   /*
    *************************** ROUTES ***************************
    */

   private Route mapError(final MpiGeneralError obj) {
      LOGGER.debug("{}", obj);
      return switch (obj) {
         case MpiServiceError.PatientUIDDoesNotExistError e -> complete(StatusCodes.BAD_REQUEST, e, Jackson.marshaller());
         case MpiServiceError.GoldenUIDDoesNotExistError e -> complete(StatusCodes.BAD_REQUEST, e, Jackson.marshaller());
         case MpiServiceError.GoldenUIDPatientConflictError e -> complete(StatusCodes.BAD_REQUEST, e, Jackson.marshaller());
         case MpiServiceError.DeletePredicateError e -> complete(StatusCodes.BAD_REQUEST, e, Jackson.marshaller());
         default -> complete(StatusCodes.INTERNAL_SERVER_ERROR);
      };
   }

   private Route routeUpdateGoldenRecord(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final String uid) {
      return entity(Jackson.unmarshaller(GoldenRecordUpdateRequestPayload.class),
                    payload -> payload != null
                          ? onComplete(askUpdateGoldenRecord(actorSystem, backEnd, uid, payload),
                                       result -> {
                                          if (result.isSuccess()) {
                                             final var updatedFields = result.get().fields();
                                             if (updatedFields.size() == 0) {
                                                return complete(StatusCodes.BAD_REQUEST);
                                             } else {
                                                return complete(StatusCodes.OK, result.get(), Jackson.marshaller());
                                             }
                                          } else {
                                             return complete(StatusCodes.INTERNAL_SERVER_ERROR);
                                          }
                                       })
                          : complete(StatusCodes.NO_CONTENT));
   }

   private Route routeSessionUpdateGoldenRecord(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final String uid) {
      return requiredSession(refreshable, sessionTransport, session -> {
         if (session != null) {
            LOGGER.info("Current session: " + session.getEmail());
            return routeUpdateGoldenRecord(actorSystem, backEnd, uid);
         }
         LOGGER.info("No active session");
         return complete(StatusCodes.FORBIDDEN);
      });

   }

   private Route routeUnlink(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      return parameter("goldenID",
                       goldenID -> parameter("patientID",
                                             patientID -> onComplete(askUnLink(actorSystem, backEnd, goldenID, patientID),
                                                                     result -> result.isSuccess()
                                                                           ? result.get()
                                                                                   .linkInfo()
                                                                                   .mapLeft(this::mapError)
                                                                                   .fold(error -> error,
                                                                                         linkInfo -> complete(StatusCodes.OK,
                                                                                                              linkInfo,
                                                                                                              Jackson.marshaller()))
                                                                           : complete(StatusCodes.IM_A_TEAPOT))));
   }

   private Route routeLink(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      return parameter("goldenID",
                       goldenID ->
                             parameter("newGoldenID",
                                       newGoldenID ->
                                             parameter("patientID",
                                                       patientID ->
                                                             parameter("score",
                                                                       score -> onComplete(
                                                                             askLink(actorSystem,
                                                                                     backEnd,
                                                                                     goldenID,
                                                                                     newGoldenID,
                                                                                     patientID,
                                                                                     Float.parseFloat(score)),
                                                                             result -> result.isSuccess()
                                                                                   ? result.get()
                                                                                           .linkInfo()
                                                                                           .mapLeft(this::mapError)
                                                                                           .fold(error -> error,
                                                                                                 linkInfo -> complete(
                                                                                                       StatusCodes.OK,
                                                                                                       linkInfo,
                                                                                                       Jackson.marshaller()))
                                                                                   : complete(StatusCodes.IM_A_TEAPOT))))));
   }

   private Route routeGoldenRecordCount(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      return onComplete(askGoldenRecordCount(actorSystem, backEnd),
                        result -> result.isSuccess()
                              ? complete(StatusCodes.OK, new GoldenRecordCount(result.get().count()), Jackson.marshaller())
                              : complete(StatusCodes.IM_A_TEAPOT));
   }

   private Route routePatientCount(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      return onComplete(askPatientCount(actorSystem, backEnd),
                        result -> result.isSuccess()
                              ? complete(StatusCodes.OK, new PatientCount(result.get().count()), Jackson.marshaller())
                              : complete(StatusCodes.IM_A_TEAPOT));
   }

   private Route routeNumberOfRecords(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      return onComplete(askNumberOfRecords(actorSystem, backEnd),
                        result -> result.isSuccess()
                              ? complete(StatusCodes.OK,
                                         new NumberOfRecords(result.get().goldenRecords(), result.get().patients()),
                                         Jackson.marshaller())
                              : complete(StatusCodes.IM_A_TEAPOT));
   }

   private Route routeGoldenIds(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      return onComplete(askGoldenIds(actorSystem, backEnd),
                        result -> result.isSuccess()
                              ? complete(StatusCodes.OK, result.get(), Jackson.marshaller())
                              : complete(StatusCodes.IM_A_TEAPOT));
   }

   private Route routeMatchesForReviewList(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      return onComplete(askMatchesForReviewList(actorSystem, backEnd),
                        result -> result.isSuccess()
                              ? complete(StatusCodes.OK, result.get(), Jackson.marshaller())
                              : complete(StatusCodes.IM_A_TEAPOT));
   }

/*
   private Route routeGoldenRecord(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      return parameterList(params -> {
         final var uidList = params.stream().map(PARAM_STRING).toList();
         LOGGER.debug("routeGoldenRecord: {}", uidList);
         return onComplete(askExpandedGoldenRecords(actorSystem, backEnd, uidList),
                           result -> result.isSuccess()
                                 ? complete(StatusCodes.OK, result.get(), Jackson.marshaller())
                                 : complete(StatusCodes.IM_A_TEAPOT));
      });
   }
*/


   private Route routeExpandedGoldenRecords(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      return parameter("uidList", items -> {
         final var uidList = Stream.of(items.split(",")).map(String::trim).toList();
         return onComplete(
               askExpandedGoldenRecords(actorSystem, backEnd, uidList),
               result -> result.isSuccess()
                     ? complete(StatusCodes.OK,
                                result.get()
                                      .expandedGoldenRecords()
                                      .stream()
                                      .map(expandedGoldenRecord -> new ApiExpandedGoldenRecord(expandedGoldenRecord.goldenRecord(),
                                                                                               expandedGoldenRecord.patientRecordsWithScore()))
                                      .toList(),
                                Jackson.marshaller())
                     : complete(StatusCodes.IM_A_TEAPOT));
      });
   }

   private Route routeExpandedPatientRecords(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      return parameter("uidList", items -> {
         final var uidList = Stream.of(items.split(",")).map(String::trim).toList();
         return onComplete(askExpandedPatientRecords(actorSystem, backEnd, uidList),
                           result -> result.isSuccess()
                                 ? complete(StatusCodes.OK, result.get(), Jackson.marshaller())
                                 : complete(StatusCodes.IM_A_TEAPOT));
      });
   }

   private Route routeFindGoldenRecordByUid(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final String uid) {
      return onComplete(askGoldenRecordByUid(actorSystem, backEnd, uid),
                        result -> result.isSuccess()
                              ? complete(StatusCodes.OK, result.get().goldenRecord(), Jackson.marshaller())
                              : complete(StatusCodes.IM_A_TEAPOT));
   }

   private Route routeSessionFindGoldenRecordByUid(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final String uid) {
      return requiredSession(refreshable, sessionTransport, session -> routeFindGoldenRecordByUid(actorSystem, backEnd, uid));
   }

   private Route routeFindPatientRecordByUid(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final String uid) {
      return onComplete(askPatientRecordByUid(actorSystem, backEnd, uid),
                        result -> result.isSuccess()
                              ? complete(StatusCodes.OK, result.get().patient(), Jackson.marshaller())
                              : complete(StatusCodes.IM_A_TEAPOT));
   }

   private Route routeSessionFindPatientRecordByUid(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final String uid) {
      return requiredSession(refreshable, sessionTransport, session -> routeFindPatientRecordByUid(actorSystem, backEnd, uid));
   }

   private Route routeCandidates(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      return parameter("uid",
                       uid -> entity(Jackson.unmarshaller(CustomMU.class),
                                     mu -> onComplete(askCandidates(actorSystem, backEnd, uid, mu),
                                                      result -> result.isSuccess()
                                                            ? result.get()
                                                                    .candidates()
                                                                    .mapLeft(this::mapError)
                                                                    .fold(error -> error,
                                                                          candidateList -> complete(StatusCodes.OK,
                                                                                                    candidateList,
                                                                                                    Jackson.marshaller()))
                                                            : complete(StatusCodes.IM_A_TEAPOT))));
   }

   private Route routeNotificationRequest(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      return entity(Jackson.unmarshaller(NotificationRequest.class),
                    obj -> onComplete(askNotificationRequest(actorSystem, backEnd, obj), response -> {
                       if (response.isSuccess()) {
                          final var eventNotificationRequestRsp = response.get();
                          return complete(StatusCodes.OK, eventNotificationRequestRsp, Jackson.marshaller());
                       } else {
                          return complete(StatusCodes.IM_A_TEAPOT);
                       }
                    }));
   }

   private Route routeLoginWithKeycloakRequest(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final CheckHeader<UserSession> checkHeader) {
      return entity(
            Jackson.unmarshaller(OAuthCodeRequestPayload.class),
            obj -> onComplete(askLoginWithKeycloakRequest(actorSystem, backEnd, obj), response -> {
               if (response.isSuccess()) {
                  final var eventLoginWithKeycloakResponse = response.get();
                  final User user = eventLoginWithKeycloakResponse.user();
                  if (user != null) {
                     return setSession(refreshable,
                                       sessionTransport,
                                       new UserSession(user),
                                       () -> setNewCsrfToken(checkHeader,
                                                             () -> complete(StatusCodes.OK, user, Jackson.marshaller())));
                  } else {
                     return complete(StatusCodes.FORBIDDEN);
                  }
               } else {
                  return complete(StatusCodes.IM_A_TEAPOT);
               }
            }));
   }

   private Route routeCurrentUser() {
      return requiredSession(refreshable, sessionTransport, session -> {
         if (session != null) {
            LOGGER.info("Current session: " + session.getEmail());
            return complete(StatusCodes.OK, session, Jackson.marshaller());
         }
         LOGGER.info("No active session");
         return complete(StatusCodes.FORBIDDEN);
      });
   }

   private Route routeLogout() {
      return requiredSession(refreshable,
                             sessionTransport,
                             session -> invalidateSession(refreshable, sessionTransport, () -> extractRequestContext(ctx -> {
                                LOGGER.info("Logging out {}", session.getUsername());
                                return onSuccess(() -> ctx.completeWith(HttpResponse.create()),
                                                 routeResult -> complete("success"));
                             })));
   }

   private Route routeUpload(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      return withSizeLimit(
            AppConfig.JEMPI_FILE_IMPORT_MAX_SIZE_BYTE,
            () -> storeUploadedFile("csv",
                                    (info) -> {
                                       try {
                                          LOGGER.debug(GlobalConstants.SEGMENT_UPLOAD);
                                          return File.createTempFile("import-", ".csv");
                                       } catch (Exception e) {
                                          LOGGER.error(e.getMessage(), e);
                                          return null;
                                       }
                                    },
                                    (info, file) -> onComplete(askUploadRequest(actorSystem, backEnd, info, file),
                                                               response -> response.isSuccess()
                                                                     ? complete(StatusCodes.OK)
                                                                     : complete(StatusCodes.IM_A_TEAPOT))));
   }

   private Route routeSessionUpload(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      return withSizeLimit(AppConfig.JEMPI_FILE_IMPORT_MAX_SIZE_BYTE,
                           () -> requiredSession(refreshable, sessionTransport, session -> {
                              if (session != null) {
                                 LOGGER.info("Current session: " + session.getEmail());
                                 return storeUploadedFile("csv",
                                                          (info) -> {
                                                             try {
                                                                LOGGER.debug(GlobalConstants.SEGMENT_UPLOAD);
                                                                return File.createTempFile("import-", ".csv");
                                                             } catch (Exception e) {
                                                                LOGGER.error("error", e);
                                                                return null;
                                                             }
                                                          },
                                                          (info, file) -> onComplete(askUploadRequest(actorSystem,
                                                                                                      backEnd,
                                                                                                      info,
                                                                                                      file),
                                                                                     response -> response.isSuccess()
                                                                                           ? complete(StatusCodes.OK)
                                                                                           : complete(StatusCodes.IM_A_TEAPOT)));
                              }
                              LOGGER.info("No active session");
                              return complete(StatusCodes.FORBIDDEN);
                           }));
   }

   private Route routeSimpleSearch(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final RecordType recordType) {
      LOGGER.info("Simple search on {}", recordType);
      return entity(Jackson.unmarshaller(SimpleSearchRequestPayload.class), searchParameters -> onComplete(() -> {
         if (recordType == RecordType.GoldenRecord) {
            return askSimpleSearchGoldenRecords(actorSystem, backEnd, searchParameters);
         } else {
            return askSimpleSearchPatientRecords(actorSystem, backEnd, searchParameters);
         }
      }, response -> {
         if (response.isSuccess()) {
            final var eventSearchRsp = response.get();
            return complete(StatusCodes.OK, eventSearchRsp, Jackson.marshaller());
         } else {
            return complete(StatusCodes.IM_A_TEAPOT);
         }
      }));
   }

   private Route routeSessionSimpleSearch(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final RecordType recordType) {
      return requiredSession(refreshable, sessionTransport, session -> routeSimpleSearch(actorSystem, backEnd, recordType));
   }

   private Route routeCustomSearch(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final RecordType recordType) {
      return entity(Jackson.unmarshaller(CustomSearchRequestPayload.class), searchParameters -> onComplete(() -> {
         if (recordType == RecordType.GoldenRecord) {
            return askCustomSearchGoldenRecords(actorSystem, backEnd, searchParameters);
         } else {
            return askCustomSearchPatientRecords(actorSystem, backEnd, searchParameters);
         }
      }, response -> {
         if (response.isSuccess()) {
            final var eventSearchRsp = response.get();
            return complete(StatusCodes.OK, eventSearchRsp, Jackson.marshaller());
         } else {
            return complete(StatusCodes.IM_A_TEAPOT);
         }
      }));
   }

   private Route routeCalculateScores() {
      return entity(Jackson.unmarshaller(CalculateScoresRequest.class),
                    obj -> {
                       try {
                          return onComplete(proxyPostCalculateScores(obj),
                                            response -> response.isSuccess()
                                                  ? complete(response.get())
                                                  : complete(StatusCodes.IM_A_TEAPOT));
                       } catch (JsonProcessingException e) {
                          LOGGER.error(e.getLocalizedMessage(), e);
                          return complete(StatusCodes.IM_A_TEAPOT);
                       }
                    });
   }

   private Route routeSessionCustomSearch(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final RecordType recordType) {
      return requiredSession(refreshable, sessionTransport, session -> {
         LOGGER.info("Custom search on {}", recordType);
         // Simple search for golden records
         return routeCustomSearch(actorSystem, backEnd, recordType);
      });
   }

   private Route createJeMPIRoutes(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      return concat(
            post(() -> concat(path(GlobalConstants.SEGMENT_UPDATE_NOTIFICATION,
                                   () -> routeNotificationRequest(actorSystem, backEnd)),
                              path(segment(GlobalConstants.SEGMENT_POST_SIMPLE_SEARCH).slash(segment(Pattern.compile(
                                         "^(golden|patient)$"))),
                                   (type) -> {
                                      final var t = type.equals("golden")
                                            ? RecordType.GoldenRecord
                                            : RecordType.PatientRecord;
                                      return AppConfig.AKKA_HTTP_SESSION_ENABLED
                                            ? routeSessionSimpleSearch(actorSystem, backEnd, t)
                                            : routeSimpleSearch(actorSystem, backEnd, t);
                                   }),
                              path(segment(GlobalConstants.SEGMENT_POST_CUSTOM_SEARCH).slash(segment(Pattern.compile(
                                         "^(golden|patient)$"))),
                                   (type) -> {
                                      final var t = type.equals("golden")
                                            ? RecordType.GoldenRecord
                                            : RecordType.PatientRecord;
                                      return AppConfig.AKKA_HTTP_SESSION_ENABLED
                                            ? routeSessionCustomSearch(actorSystem, backEnd, t)
                                            : routeCustomSearch(actorSystem, backEnd, t);
                                   }),
                              path(GlobalConstants.SEGMENT_CALCULATE_SCORES, this::routeCalculateScores),
                              path(GlobalConstants.SEGMENT_UPLOAD, () -> AppConfig.AKKA_HTTP_SESSION_ENABLED
                                    ? routeSessionUpload(actorSystem, backEnd)
                                    : routeUpload(actorSystem, backEnd)))),
            patch(() -> concat(
                  path(segment(GlobalConstants.SEGMENT_UPDATE_GOLDEN_RECORD).slash(segment(Pattern.compile("^[A-z0-9]+$"))),
                       (uid) -> AppConfig.AKKA_HTTP_SESSION_ENABLED
                             ? routeSessionUpdateGoldenRecord(actorSystem, backEnd, uid)
                             : routeUpdateGoldenRecord(actorSystem, backEnd, uid)),
                  path(GlobalConstants.SEGMENT_CREATE_GOLDEN_RECORD, () -> routeUnlink(actorSystem, backEnd)),
                  path(GlobalConstants.SEGMENT_LINK_RECORD, () -> routeLink(actorSystem, backEnd)))),
            get(() -> concat(
                  path(GlobalConstants.SEGMENT_CURRENT_USER, this::routeCurrentUser),
                  path(GlobalConstants.SEGMENT_LOGOUT, this::routeLogout),
                  path(GlobalConstants.SEGMENT_COUNT_GOLDEN_RECORDS, () -> routeGoldenRecordCount(actorSystem, backEnd)),
                  path(GlobalConstants.SEGMENT_COUNT_PATIENT_RECORDS, () -> routePatientCount(actorSystem, backEnd)),
                  path(GlobalConstants.SEGMENT_COUNT_RECORDS, () -> routeNumberOfRecords(actorSystem, backEnd)),
                  path(GlobalConstants.SEGMENT_GOLDEN_IDS, () -> routeGoldenIds(actorSystem, backEnd)),
//                path(GlobalConstants.SEGMENT_GET_GOLDEN_ID_DOCUMENTS, () -> routeGoldenRecord(actorSystem, backEnd)),
                  path(GlobalConstants.SEGMENT_EXPANDED_GOLDEN_RECORDS, () -> routeExpandedGoldenRecords(actorSystem, backEnd)),
                  path(GlobalConstants.SEGMENT_EXPANDED_PATIENT_RECORDS, () -> routeExpandedPatientRecords(actorSystem, backEnd)),
                  path(GlobalConstants.SEGMENT_GET_NOTIFICATIONS, () -> routeMatchesForReviewList(actorSystem, backEnd)),
                  path(GlobalConstants.SEGMENT_CANDIDATE_GOLDEN_RECORDS, () -> routeCandidates(actorSystem, backEnd)),
                  path(segment(GlobalConstants.SEGMENT_PATIENT_RECORD_ROUTE).slash(segment(Pattern.compile("^[A-z0-9]+$"))),
                       (uid) -> AppConfig.AKKA_HTTP_SESSION_ENABLED
                             ? routeSessionFindPatientRecordByUid(actorSystem, backEnd, uid)
                             : routeFindPatientRecordByUid(actorSystem, backEnd, uid)),
                  path(segment(GlobalConstants.SEGMENT_GOLDEN_RECORD_ROUTE).slash(segment(Pattern.compile("^[A-z0-9]+$"))),
                       (uid) -> AppConfig.AKKA_HTTP_SESSION_ENABLED
                             ? routeSessionFindGoldenRecordByUid(actorSystem, backEnd, uid)
                             : routeFindGoldenRecordByUid(actorSystem, backEnd, uid)))));
   }

   private Route createRoutes(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      return pathPrefix("JeMPI", () -> createJeMPIRoutes(actorSystem, backEnd));
   }

   private Route createCorsRoutes(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final JSONArray fields) {
      final var settings = CorsSettings.create(AppConfig.CONFIG);
      CheckHeader<UserSession> checkHeader = new CheckHeader<>(getSessionManager());
      return cors(
            settings,
            () -> randomTokenCsrfProtection(
                  checkHeader,
                  () -> pathPrefix("JeMPI",
                                   () -> concat(
                                         createJeMPIRoutes(actorSystem, backEnd),
                                         post(() -> path(GlobalConstants.SEGMENT_VALIDATE_OAUTH,
                                                         () -> routeLoginWithKeycloakRequest(actorSystem, backEnd, checkHeader))),
                                         get(() -> path(GlobalConstants.SEGMENT_GET_FIELDS_CONFIG,
                                                        () -> setNewCsrfToken(
                                                              checkHeader,
                                                              () -> complete(StatusCodes.OK, fields.toJSONString()))))))));
   }

   private record GoldenRecordCount(Long count) {
   }

   private record PatientCount(Long count) {
   }

   private record ApiExpandedGoldenRecord(
         GoldenRecord goldenRecord,
         List<PatientRecordWithScore> mpiPatientRecords) {
   }

   private record ApiExpandedPatientRecord(
         PatientRecord patientRecord,
         List<GoldenRecordWithScore> mpiGoldenRecords) {
   }

   private record NumberOfRecords(
         Long goldenRecords,
         Long patientRecords) {
   }

}
