package org.jembi.jempi.api;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.AskPattern;
import akka.dispatch.MessageDispatcher;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCode;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.Route;
import akka.http.javadsl.server.directives.FileInfo;
import akka.stream.javadsl.Framing;
import akka.util.ByteString;
import ch.megard.akka.http.cors.javadsl.settings.CorsSettings;
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
import org.jembi.jempi.shared.models.CustomMU;
import org.jembi.jempi.shared.models.NotificationRequest;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.jembi.jempi.shared.utils.CustomSearchRequestPayload;
import org.jembi.jempi.shared.utils.RecordType;
import org.jembi.jempi.shared.utils.SimpleSearchRequestPayload;
import org.json.simple.JSONArray;

import static akka.http.javadsl.server.PathMatchers.segment;
import static ch.megard.akka.http.cors.javadsl.CorsDirectives.cors;
import static com.softwaremill.session.javadsl.SessionTransports.CookieST;

public class HttpServer extends HttpSessionAwareDirectives<UserSession> {

    private static final Logger LOGGER = LogManager.getLogger(HttpServer.class);

    private static final SessionEncoder<UserSession> BASIC_ENCODER = new BasicSessionEncoder(UserSession.getSerializer());

    // in-memory refresh token storage
    private static final RefreshTokenStorage<UserSession> REFRESH_TOKEN_STORAGE = new InMemoryRefreshTokenStorage<UserSession>() {
        @Override
        public void log(String msg) {
            LOGGER.info(msg);
        }
    };

    private Refreshable<UserSession> refreshable;
    private SetSessionTransport sessionTransport;

    private static final Function<Entry<String, String>, String> paramString = Entry::getValue;
    private CompletionStage<ServerBinding> binding = null;

    public HttpServer(MessageDispatcher dispatcher) {
        super(new SessionManager<>(
                        SessionConfig.defaultConfig(AppConfig.SESSION_SECRET),
                        BASIC_ENCODER
                )
        );

        // use Refreshable for sessions, which needs to be refreshed or OneOff otherwise
        // using Refreshable, a refresh token is set in form of a cookie or a custom header
        refreshable = new Refreshable<>(getSessionManager(), REFRESH_TOKEN_STORAGE, dispatcher);

        // set the session transport - based on Cookies (or Headers)
        sessionTransport = CookieST;
    }

    void close(ActorSystem<Void> actorSystem) {
        binding.thenCompose(ServerBinding::unbind) // trigger unbinding from the port
                .thenAccept(unbound -> actorSystem.terminate()); // and shutdown when done
    }

    void open(final ActorSystem<Void> actorSystem, final ActorRef<BackEnd.Event> backEnd, final JSONArray fields) {
        final Http http = Http.get(actorSystem);
        binding = http.newServerAt(AppConfig.HTTP_SERVER_HOST, AppConfig.HTTP_SERVER_PORT)
                .bind(this.createRoutes(actorSystem, backEnd, fields));
        LOGGER.info("Server online at http://{}:{}", AppConfig.HTTP_SERVER_HOST, AppConfig.HTTP_SERVER_PORT);
    }

    private CompletionStage<BackEnd.EventGetGoldenRecordCountRsp> getGoldenRecordCount(
            final ActorSystem<Void> actorSystem,
            final ActorRef<BackEnd.Event> backEnd) {
        CompletionStage<BackEnd.EventGetGoldenRecordCountRsp> stage = AskPattern.ask(backEnd,
                BackEnd.EventGetGoldenRecordCountReq::new,
                java.time.Duration.ofSeconds(10),
                actorSystem.scheduler());
        return stage.thenApply(response -> response);
    }

    private CompletionStage<BackEnd.EventGetDocumentCountRsp> getDocumentCount(final ActorSystem<Void> actorSystem,
                                                                               final ActorRef<BackEnd.Event> backEnd) {
        LOGGER.debug("getDocumentCount");
        CompletionStage<BackEnd.EventGetDocumentCountRsp> stage = AskPattern.ask(backEnd,
                BackEnd.EventGetDocumentCountReq::new,
                java.time.Duration.ofSeconds(10),
                actorSystem.scheduler());
        return stage.thenApply(response -> response);
    }

    private CompletionStage<BackEnd.EventGetNumberOfRecordsRsp> getNumberOfRecords(final ActorSystem<Void> actorSystem,
                                                                                   final ActorRef<BackEnd.Event> backEnd) {
        LOGGER.debug("getNumberOfRecords");
        CompletionStage<BackEnd.EventGetNumberOfRecordsRsp> stage = AskPattern.ask(
                backEnd,
                BackEnd.EventGetNumberOfRecordsReq::new,
                java.time.Duration.ofSeconds(10),
                actorSystem.scheduler());
        return stage.thenApply(response -> response);
    }

    private CompletionStage<BackEnd.EventGetGoldenIdListByPredicateRsp> getGoldenIdListByPredicate(
            final ActorSystem<Void> actorSystem, final ActorRef<BackEnd.Event> backEnd,
            final String predicate,
            final String val) {
        LOGGER.debug("getGoldenIdListByPredicate");
        CompletionStage<BackEnd.EventGetGoldenIdListByPredicateRsp> stage = AskPattern.ask(backEnd,
                replyTo -> new BackEnd.EventGetGoldenIdListByPredicateReq(replyTo, predicate, val),
                java.time.Duration.ofSeconds(20),
                actorSystem.scheduler());
        return stage.thenApply(response -> response);
    }

    private CompletionStage<BackEnd.EventGetGoldenIdListRsp> getGoldenIdList(final ActorSystem<Void> actorSystem,
                                                                             final ActorRef<BackEnd.Event> backEnd) {
        LOGGER.debug("getGoldenIdList");
        CompletionStage<BackEnd.EventGetGoldenIdListRsp> stage = AskPattern.ask(backEnd,
                BackEnd.EventGetGoldenIdListReq::new,
                java.time.Duration.ofSeconds(30),
                actorSystem.scheduler());
        return stage.thenApply(response -> response);
    }

    private CompletionStage<BackEnd.EventGetMatchesForReviewListRsp> getMatchesForReviewList(final ActorSystem<Void> actorSystem,
                                                                                             final ActorRef<BackEnd.Event> backEnd) {
        CompletionStage<BackEnd.EventGetMatchesForReviewListRsp> stage =
                AskPattern.ask(backEnd,
                        BackEnd.EventGetMatchesForReviewReq::new,
                        java.time.Duration.ofSeconds(30),
                        actorSystem.scheduler());
        return stage.thenApply(response -> response);
    }

    private CompletionStage<BackEnd.EventFindGoldenRecordByUidResponse> findGoldenRecordByUid(final ActorSystem<Void> actorSystem,
                                                                                              final ActorRef<BackEnd.Event> backEnd,
                                                                                              final String uid) {
        LOGGER.debug("findGoldenRecordById");
        final CompletionStage<BackEnd.EventFindGoldenRecordByUidResponse> stage = AskPattern.ask(backEnd,
                replyTo -> new BackEnd.EventFindGoldenRecordByUidRequest(replyTo, uid),
                java.time.Duration.ofSeconds(5),
                actorSystem.scheduler());
        return stage.thenApply(response -> response);
    }

    private CompletionStage<BackEnd.EventFindPatientRecordByUidResponse> findPatientRecordByUid(final ActorSystem<Void> actorSystem,
                                                                                                final ActorRef<BackEnd.Event> backEnd,
                                                                                                final String uid) {
        LOGGER.debug("findPatientRecordById : " + uid);
        final CompletionStage<BackEnd.EventFindPatientRecordByUidResponse> stage = AskPattern.ask(backEnd,
                replyTo -> new BackEnd.EventFindPatientByUidRequest(replyTo, uid),
                java.time.Duration.ofSeconds(5),
                actorSystem.scheduler());
        return stage.thenApply(response -> response);
    }

    private CompletionStage<BackEnd.EventGetCandidatesRsp> getCandidates(final ActorSystem<Void> actorSystem,
                                                                         final ActorRef<BackEnd.Event> backEnd,
                                                                         final String uid, final CustomMU mu) {
        LOGGER.debug("getCandidates");
        CompletionStage<BackEnd.EventGetCandidatesRsp> stage = AskPattern.ask(backEnd,
                replyTo -> new BackEnd.EventGetCandidatesReq(replyTo, uid, mu),
                java.time.Duration.ofSeconds(5),
                actorSystem.scheduler());
        return stage.thenApply(response -> response);
    }

    private CompletionStage<BackEnd.EventGetGoldenRecordDocumentsRsp> getGoldenRecordDocuments(
            final ActorSystem<Void> actorSystem,
            final ActorRef<BackEnd.Event> backEnd,
            final List<String> uid) {
        LOGGER.debug("getGoldenRecordDocuments");
        CompletionStage<BackEnd.EventGetGoldenRecordDocumentsRsp> stage = AskPattern.ask(backEnd,
                replyTo -> new BackEnd.EventGetGoldenRecordDocumentsReq(replyTo, uid),
                java.time.Duration.ofSeconds(6),
                actorSystem.scheduler());
        return stage.thenApply(response -> response);
    }


    private CompletionStage<BackEnd.EventPatchGoldenRecordPredicateRsp> patchGoldenRecordPredicate(
            final ActorSystem<Void> actorSystem, final ActorRef<BackEnd.Event> backEnd,
            final String uid,
            final String predicate,
            final String value) {
        LOGGER.debug("patchGoldenRecordPredicate");
        CompletionStage<BackEnd.EventPatchGoldenRecordPredicateRsp> stage = AskPattern.ask(backEnd,
                replyTo -> new BackEnd.EventPatchGoldenRecordPredicateReq(replyTo, uid, predicate, value),
                java.time.Duration.ofSeconds(6),
                actorSystem.scheduler());
        return stage.thenApply(response -> response);
    }

    private CompletionStage<BackEnd.EventPatchLinkRsp> patchLink(final ActorSystem<Void> actorSystem,
                                                                 final ActorRef<BackEnd.Event> backEnd,
                                                                 final String goldenID,
                                                                 final String newGoldenID,
                                                                 final String docID,
                                                                 final Float score) {
        LOGGER.debug("patchLink");
        final CompletionStage<BackEnd.EventPatchLinkRsp> stage = AskPattern.ask(backEnd,
                replyTo -> new BackEnd.EventPatchLinkReq(replyTo, goldenID, newGoldenID, docID, score),
                java.time.Duration.ofSeconds(6),
                actorSystem.scheduler());
        return stage.thenApply(response -> response);
    }

    private CompletionStage<BackEnd.EventPatchUnLinkRsp> patchUnLink(final ActorSystem<Void> actorSystem,
                                                                     final ActorRef<BackEnd.Event> backEnd,
                                                                     final String goldenID, final String docID) {
        LOGGER.debug("patchUnLink");
        final CompletionStage<BackEnd.EventPatchUnLinkRsp> stage = AskPattern.ask(backEnd,
                replyTo -> new BackEnd.EventPatchUnLinkReq(replyTo, goldenID, docID, 2.0F),
                java.time.Duration.ofSeconds(6),
                actorSystem.scheduler());
        return stage.thenApply(response -> response);
    }

    private Route mapError(final MpiGeneralError obj) {
        LOGGER.debug("{}", obj);
        return switch (obj) {
            case MpiServiceError.EntityIDDoesNotExistError e ->
                    complete(StatusCodes.BAD_REQUEST, e, Jackson.marshaller());
            case MpiServiceError.GoldenIDDoesNotExistError e ->
                    complete(StatusCodes.BAD_REQUEST, e, Jackson.marshaller());
            case MpiServiceError.GoldenIDEntityConflictError e ->
                    complete(StatusCodes.BAD_REQUEST, e, Jackson.marshaller());
            case MpiServiceError.DeletePredicateError e -> complete(StatusCodes.BAD_REQUEST, e, Jackson.marshaller());
            default -> complete(StatusCodes.INTERNAL_SERVER_ERROR);
        };
    }

    private StatusCode mapPatchGoldenRecordPredicateResult(int result) {
        return result == 0 ? StatusCodes.OK : StatusCodes.CONFLICT;
    }

    private Route routePatchGoldenRecordPredicate(final ActorSystem<Void> actorSystem,
                                                  final ActorRef<BackEnd.Event> backEnd) {
        return parameter(
                "uid",
                uid -> parameter(
                        "predicate",
                        predicate -> parameter(
                                "value",
                                value -> onComplete(
                                        patchGoldenRecordPredicate(actorSystem, backEnd, uid, predicate, value),
                                        result -> complete(result.isSuccess()
                                                ? mapPatchGoldenRecordPredicateResult(result.get().result())
                                                : StatusCodes.IM_A_TEAPOT)))));
    }

    private Route routeUnlink(final ActorSystem<Void> actorSystem, final ActorRef<BackEnd.Event> backEnd) {
        return parameter(
                "goldenID",
                goldenID -> parameter(
                        "docID",
                        docID -> onComplete(
                                patchUnLink(actorSystem, backEnd, goldenID, docID),
                                result -> result.isSuccess()
                                        ? result.get()
                                        .linkInfo()
                                        .mapLeft(this::mapError)
                                        .fold(error -> error,
                                                linkInfo -> complete(StatusCodes.OK, linkInfo,
                                                        Jackson.marshaller()))
                                        : complete(StatusCodes.IM_A_TEAPOT))));
    }

    private Route routeLink(final ActorSystem<Void> actorSystem, final ActorRef<BackEnd.Event> backEnd) {
        return parameter(
                "goldenID",
                goldenID -> parameter(
                        "newGoldenID",
                        newGoldenID -> parameter(
                                "docID",
                                docID -> parameter(
                                        "score",
                                        score -> onComplete(
                                                patchLink(actorSystem, backEnd, goldenID, newGoldenID, docID,
                                                        Float.parseFloat(score)),
                                                result -> result.isSuccess()
                                                        ? result.get()
                                                        .linkInfo()
                                                        .mapLeft(this::mapError)
                                                        .fold(error -> error,
                                                                linkInfo -> complete(StatusCodes.OK, linkInfo,
                                                                        Jackson.marshaller()))
                                                        : complete(StatusCodes.IM_A_TEAPOT))))));
    }

    private Route routeGoldenRecordCount(final ActorSystem<Void> actorSystem, final ActorRef<BackEnd.Event> backEnd) {
        return onComplete(
                getGoldenRecordCount(actorSystem, backEnd),
                result -> result.isSuccess()
                        ? complete(StatusCodes.OK, new GoldenRecordCount(result.get().count()), Jackson.marshaller())
                        : complete(StatusCodes.IM_A_TEAPOT));
    }

    private Route routeDocumentCount(final ActorSystem<Void> actorSystem, final ActorRef<BackEnd.Event> backEnd) {
        return onComplete(
                getDocumentCount(actorSystem, backEnd),
                result -> result.isSuccess()
                        ? complete(StatusCodes.OK, new DocumentCount(result.get().count()), Jackson.marshaller())
                        : complete(StatusCodes.IM_A_TEAPOT));
    }

    private Route routeNumberOfRecords(final ActorSystem<Void> actorSystem, final ActorRef<BackEnd.Event> backEnd) {
        return onComplete(
                getNumberOfRecords(actorSystem, backEnd),
                result -> result.isSuccess()
                        ? complete(StatusCodes.OK,
                        new NumberOfRecords(result.get().goldenRecords(), result.get().documents()),
                        Jackson.marshaller())
                        : complete(StatusCodes.IM_A_TEAPOT));
    }

    private Route routeGoldenIdList(final ActorSystem<Void> actorSystem, final ActorRef<BackEnd.Event> backEnd) {
        return onComplete(
                getGoldenIdList(actorSystem, backEnd),
                result -> result.isSuccess()
                        ? complete(StatusCodes.OK, result.get(), Jackson.marshaller())
                        : complete(StatusCodes.IM_A_TEAPOT));
    }

    private Route routeMatchesForReviewList(final ActorSystem<Void> actorSystem, final ActorRef<BackEnd.Event> backEnd) {
        return onComplete(
                getMatchesForReviewList(actorSystem, backEnd),
                result -> result.isSuccess()
                        ? complete(StatusCodes.OK, result.get(), Jackson.marshaller())
                        : complete(StatusCodes.IM_A_TEAPOT));
    }

    private Route routeGoldenIdListByPredicate(final ActorSystem<Void> actorSystem, final ActorRef<BackEnd.Event> backEnd) {
        return parameter(
                "predicate",
                predicate -> parameter(
                        "value",
                        value -> onComplete(getGoldenIdListByPredicate(actorSystem, backEnd, predicate, value),
                                result -> result.isSuccess()
                                        ? complete(StatusCodes.OK, result.get(), Jackson.marshaller())
                                        : complete(StatusCodes.IM_A_TEAPOT))));
    }

    private Route routeGoldenRecordDocuments(final ActorSystem<Void> actorSystem,
                                             final ActorRef<BackEnd.Event> backEnd) {
        return parameterList(params -> {
            final var uidList = params.stream().map(paramString).toList();
            return onComplete(getGoldenRecordDocuments(actorSystem, backEnd, uidList),
                    result -> result.isSuccess()
                            ? complete(StatusCodes.OK, result.get(), Jackson.marshaller())
                            : complete(StatusCodes.IM_A_TEAPOT));
        });
    }

    private Route routeGoldenRecordDocumentList(final ActorSystem<Void> actorSystem,
                                                final ActorRef<BackEnd.Event> backEnd) {
        return parameter("uidList",
                items -> {
                    final var uidList = Stream.of(items.split(",")).map(String::trim).toList();
                    return onComplete(getGoldenRecordDocuments(actorSystem, backEnd, uidList),
                            result -> result.isSuccess()
                                    ? complete(StatusCodes.OK, result.get(), Jackson.marshaller())
                                    : complete(StatusCodes.IM_A_TEAPOT));
                });
    }

    private Route routeFindGoldenRecordByUid(final ActorSystem<Void> actorSystem, final ActorRef<BackEnd.Event> backEnd, final String uid) {
        return requiredSession(refreshable, sessionTransport, session ->
                onComplete(findGoldenRecordByUid(actorSystem, backEnd, uid),
                        result -> result.isSuccess()
                                ? complete(StatusCodes.OK, result.get().goldenRecord(), Jackson.marshaller())
                                : complete(StatusCodes.IM_A_TEAPOT)));
    }

    private Route routeFindPatientRecordByUid(final ActorSystem<Void> actorSystem, final ActorRef<BackEnd.Event> backEnd, final String uid) {
        return requiredSession(refreshable, sessionTransport, session ->
                onComplete(findPatientRecordByUid(actorSystem, backEnd, uid),
                        result -> result.isSuccess()
                                ? complete(StatusCodes.OK, result.get().document(), Jackson.marshaller())
                                : complete(StatusCodes.IM_A_TEAPOT)));
    }

    private Route routeCandidates(final ActorSystem<Void> actorSystem, final ActorRef<BackEnd.Event> backEnd) {
        return parameter(
                "uid",
                uid -> entity(
                        Jackson.unmarshaller(CustomMU.class),
                        mu -> onComplete(
                                getCandidates(actorSystem, backEnd, uid, mu),
                                result -> result.isSuccess()
                                        ? result.get()
                                        .candidates()
                                        .mapLeft(this::mapError)
                                        .fold(error -> error,
                                                candidateList -> complete(StatusCodes.OK, candidateList,
                                                        Jackson.marshaller()))
                                        : complete(StatusCodes.IM_A_TEAPOT))));
    }

    private Route routeNotificationRequest(final ActorSystem<Void> actorSystem, final ActorRef<BackEnd.Event> backEnd) {
        return entity(Jackson.unmarshaller(NotificationRequest.class),
                obj -> onComplete(postNotificationRequest(actorSystem, backEnd, obj),
                        response -> {
                            if (response.isSuccess()) {
                                final var eventNotificationRequestRsp = response.get();
                                return complete(
                                        StatusCodes.OK,
                                        eventNotificationRequestRsp,
                                        Jackson.marshaller());
                            } else {
                                return complete(StatusCodes.IM_A_TEAPOT);
                            }
                        }));
    }

    private Route routeLoginWithKeycloakRequest(final ActorSystem<Void> actorSystem, final ActorRef<BackEnd.Event> backEnd, CheckHeader<UserSession> checkHeader) {
        return entity(Jackson.unmarshaller(OAuthCodeRequestPayload.class),
                obj -> onComplete(loginWithKeycloakRequest(actorSystem, backEnd, obj),
                        response -> {
                            if (response.isSuccess()) {
                                final var eventLoginWithKeycloakResponse = response.get();
                                User user = eventLoginWithKeycloakResponse.user();
                                if (user != null) {
                                    return setSession(refreshable, sessionTransport, new UserSession(user), () ->
                                            setNewCsrfToken(checkHeader, () ->
                                                    complete(
                                                            StatusCodes.OK,
                                                            user,
                                                            Jackson.marshaller())
                                            )
                                    );
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
        return requiredSession(refreshable, sessionTransport, session ->
                invalidateSession(refreshable, sessionTransport, () ->
                        extractRequestContext(ctx -> {
                            LOGGER.info("Logging out {}", session.getUsername());
                            return onSuccess(() -> ctx.completeWith(HttpResponse.create()), routeResult ->
                                    complete("success")
                            );
                        })));
    }

    private Route routeUpload(final ActorSystem<Void> actorSystem,
                              final ActorRef<BackEnd.Event> backEnd) {
        return withSizeLimit(AppConfig.JEMPI_FILE_IMPORT_MAX_SIZE_BYTE, () -> requiredSession(refreshable, sessionTransport, session ->
                {
                    if (session != null) {
                        LOGGER.info("Current session: " + session.getEmail());
                        return storeUploadedFile("csv", (info) -> {
                            try {
                                LOGGER.debug("upload");
                                return File.createTempFile("import-", ".csv");
                            } catch (Exception e) {
                                LOGGER.error("error", e);
                                return null;
                            }
                        }, (info, file) -> onComplete(uploadRequest(actorSystem, backEnd, info, file), response ->
                                response.isSuccess() ? complete(StatusCodes.OK) : complete(StatusCodes.IM_A_TEAPOT)
                        ));
                    }
                    LOGGER.info("No active session");
                    return complete(StatusCodes.FORBIDDEN);
                }
        ));
    }

    private CompletionStage<BackEnd.EventResponse> simpleSearchGoldenRecords(final ActorSystem<Void> actorSystem,
                                                                                                      final ActorRef<BackEnd.Event> backEnd,
                                                                                                      final SimpleSearchRequestPayload searchRequestPayload) {
        CompletionStage<BackEnd.EventSearchGoldenRecordsResponse> stage =
                AskPattern.ask(backEnd,
                        replyTo -> new BackEnd.EventSimpleSearchGoldenRecordsRequest(replyTo, searchRequestPayload),
                        java.time.Duration.ofSeconds(11),
                        actorSystem.scheduler());
        return stage.thenApply(response -> response);
    }

    private CompletionStage<BackEnd.EventResponse> customSearchGoldenRecords(final ActorSystem<Void> actorSystem,
                                                                             final ActorRef<BackEnd.Event> backEnd,
                                                                             final CustomSearchRequestPayload searchRequestPayload) {
        CompletionStage<BackEnd.EventSearchGoldenRecordsResponse> stage =
                AskPattern.ask(backEnd,
                        replyTo -> new BackEnd.EventCustomSearchGoldenRecordsRequest(replyTo, searchRequestPayload),
                        java.time.Duration.ofSeconds(11),
                        actorSystem.scheduler());
        return stage.thenApply(response -> response);
    }

    private CompletionStage<BackEnd.EventResponse> simpleSearchPatientRecords(final ActorSystem<Void> actorSystem,
                                                                                                        final ActorRef<BackEnd.Event> backEnd,
                                                                                                        final SimpleSearchRequestPayload simpleSearchRequestPayload) {
        CompletionStage<BackEnd.EventSearchPatientRecordsResponse> stage =
                AskPattern.ask(backEnd,
                        replyTo -> new BackEnd.EventSimpleSearchPatientRecordsRequest(replyTo, simpleSearchRequestPayload),
                        java.time.Duration.ofSeconds(11),
                        actorSystem.scheduler());
        return stage.thenApply(response -> response);
    }

    private CompletionStage<BackEnd.EventResponse> customSearchPatientRecords(final ActorSystem<Void> actorSystem,
                                                                             final ActorRef<BackEnd.Event> backEnd,
                                                                             final CustomSearchRequestPayload searchRequestPayload) {
        CompletionStage<BackEnd.EventSearchPatientRecordsResponse> stage =
                AskPattern.ask(backEnd,
                        replyTo -> new BackEnd.EventCustomSearchPatientRecordsRequest(replyTo, searchRequestPayload),
                        java.time.Duration.ofSeconds(11),
                        actorSystem.scheduler());
        return stage.thenApply(response -> response);
    }
    private Route routeSimpleSearch(final ActorSystem<Void> actorSystem, final ActorRef<BackEnd.Event> backEnd, final RecordType recordType) {
        return requiredSession(refreshable, sessionTransport, session -> {
            LOGGER.info("Simple search on {}", recordType);
            // Simple search for golden records
            return entity(Jackson.unmarshaller(SimpleSearchRequestPayload.class),
                    searchParameters ->
                            onComplete(
                                    () -> {
                                        if (recordType == RecordType.GoldenRecord) {
                                            return simpleSearchGoldenRecords(actorSystem, backEnd, searchParameters);
                                        } else {
                                            return simpleSearchPatientRecords(actorSystem, backEnd, searchParameters);
                                        }
                                    },
                                    response -> {
                                        if (response.isSuccess()) {
                                            final var eventSearchRsp = response.get();
                                            return complete(
                                                    StatusCodes.OK,
                                                    eventSearchRsp,
                                                    Jackson.marshaller());
                                        } else {
                                            return complete(StatusCodes.IM_A_TEAPOT);
                                        }
                                    }));
        });
    }

    private Route routeCustomSearch(final ActorSystem<Void> actorSystem, final ActorRef<BackEnd.Event> backEnd, final RecordType recordType) {
        return requiredSession(refreshable, sessionTransport, session -> {
            LOGGER.info("Custom search on {}", recordType);
            // Simple search for golden records
            return entity(Jackson.unmarshaller(CustomSearchRequestPayload.class),
                    searchParameters ->
                            onComplete(
                                    () -> {
                                        if (recordType == RecordType.GoldenRecord) {
                                            return customSearchGoldenRecords(actorSystem, backEnd, searchParameters);
                                        } else {
                                            return customSearchPatientRecords(actorSystem, backEnd, searchParameters);
                                        }
                                    },
                                    response -> {
                                        if (response.isSuccess()) {
                                            final var eventSearchRsp = response.get();
                                            return complete(
                                                    StatusCodes.OK,
                                                    eventSearchRsp,
                                                    Jackson.marshaller());
                                        } else {
                                            return complete(StatusCodes.IM_A_TEAPOT);
                                        }
                                    }));
        });
    }

    private Route createRoutes(final ActorSystem<Void> actorSystem, final ActorRef<BackEnd.Event> backEnd, final JSONArray fields) {
        final var settings = CorsSettings.create(AppConfig.CONFIG);
        CheckHeader<UserSession> checkHeader = new CheckHeader<>(getSessionManager());
        return cors(settings,
                () -> randomTokenCsrfProtection(checkHeader,
                        () -> pathPrefix(
                                "JeMPI",
                                () -> concat(
                                        post(() -> concat(
                                                path("NotificationRequest",
                                                        () -> routeNotificationRequest(actorSystem, backEnd)),
                                                path("authenticate",
                                                        () -> routeLoginWithKeycloakRequest(actorSystem, backEnd, checkHeader)),
                                                path(segment("search").slash(segment(Pattern.compile("^(golden|patient)$"))),
                                                        (type) -> routeSimpleSearch(actorSystem, backEnd, type.equals("golden") ? RecordType.GoldenRecord : RecordType.Entity)),
                                                path(segment("custom-search").slash(segment(Pattern.compile("^(golden|patient)$"))),
                                                        (type) -> routeCustomSearch(actorSystem, backEnd, type.equals("golden") ? RecordType.GoldenRecord : RecordType.Entity)),
                                                path("upload", () -> routeUpload(actorSystem, backEnd)))),
                                        patch(() -> concat(
                                                path("PatchGoldenRecordPredicate",
                                                        () -> routePatchGoldenRecordPredicate(actorSystem, backEnd)),
                                                path("Unlink",
                                                        () -> routeUnlink(actorSystem, backEnd)),
                                                path("Link",
                                                        () -> routeLink(actorSystem, backEnd)))),
                                        get(() -> concat(
                                                path("config",
                                                        () -> setNewCsrfToken(checkHeader,
                                                                () -> complete(StatusCodes.OK, fields.toJSONString()))),
                                                path("current-user",
                                                        () -> routeCurrentUser()
                                                ),
                                                path("logout",
                                                        () -> routeLogout()
                                                ),
                                                path("GoldenRecordCount",
                                                        () -> routeGoldenRecordCount(actorSystem, backEnd)),
                                                path("DocumentCount",
                                                        () -> routeDocumentCount(actorSystem, backEnd)),
                                                path("NumberOfRecords",
                                                        () -> routeNumberOfRecords(actorSystem, backEnd)),
                                                path("GoldenIdList",
                                                        () -> routeGoldenIdList(actorSystem, backEnd)),
                                                path("GoldenIdListByPredicate",
                                                        () -> routeGoldenIdListByPredicate(actorSystem, backEnd)),
                                                path("GoldenRecordDocuments",
                                                        () -> routeGoldenRecordDocuments(actorSystem, backEnd)),
                                                path("GoldenRecordDocumentList",
                                                        () -> routeGoldenRecordDocumentList(actorSystem, backEnd)),
                                                path("MatchesForReview",
                                                        () -> routeMatchesForReviewList(actorSystem, backEnd)),
                                                path(segment("patient-record").slash(segment(Pattern.compile("^[A-z0-9]+$"))),
                                                        (id) -> routeFindPatientRecordByUid(actorSystem, backEnd, id)),
                                                path(segment("golden-record").slash(segment(Pattern.compile("^[A-z0-9]+$"))),
                                                        (id) -> routeFindGoldenRecordByUid(actorSystem, backEnd, id)),
                                                path("Candidates",
                                                        () -> routeCandidates(actorSystem, backEnd))))))));
    }

    private CompletionStage<BackEnd.EventNotificationRequestRsp> postNotificationRequest(final ActorSystem<Void> actorSystem,
                                                                                         final ActorRef<BackEnd.Event> backEnd,
                                                                                         final NotificationRequest notificationRequest) {
        CompletionStage<BackEnd.EventNotificationRequestRsp> stage =
                AskPattern.ask(backEnd,
                        replyTo -> new BackEnd.EventNotificationRequestReq(replyTo,
                                notificationRequest.notificationId(), notificationRequest.state()),
                        java.time.Duration.ofSeconds(11),
                        actorSystem.scheduler());
        return stage.thenApply(response -> response);
    }

    private CompletionStage<BackEnd.EventLoginWithKeycloakResponse> loginWithKeycloakRequest(final ActorSystem<Void> actorSystem,
                                                                                             final ActorRef<BackEnd.Event> backEnd,
                                                                                             final OAuthCodeRequestPayload body) {
        CompletionStage<BackEnd.EventLoginWithKeycloakResponse> stage =
                AskPattern.ask(backEnd,
                        replyTo -> new BackEnd.EventLoginWithKeycloakRequest(replyTo, body),
                        java.time.Duration.ofSeconds(11),
                        actorSystem.scheduler());
        return stage.thenApply(response -> response);
    }

    private CompletionStage<BackEnd.EventPostCsvFileResponse> uploadRequest(final ActorSystem<Void> actorSystem,
                                                                            final ActorRef<BackEnd.Event> backEnd,
                                                                            final FileInfo info, final File file) {
        CompletionStage<BackEnd.EventPostCsvFileResponse> stage =
                AskPattern.ask(backEnd,
                        replyTo -> new BackEnd.EventPostCsvFileRequest(replyTo, info, file),
                        java.time.Duration.ofSeconds(11),
                        actorSystem.scheduler());
        return stage.thenApply(response -> response);
    }

    private record GoldenRecordCount(Long count) {
    }

    private record DocumentCount(Long count) {
    }

    private record NumberOfRecords(Long goldenRecords,
                                   Long documents) {
    }

}
