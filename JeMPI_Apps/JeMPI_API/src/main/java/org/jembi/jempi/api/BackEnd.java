package org.jembi.jempi.api;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;
import io.vavr.control.Either;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.AppConfig;
import org.jembi.jempi.api.keycloak.AkkaAdapterConfig;
import org.jembi.jempi.api.keycloak.AkkaKeycloakDeploymentBuilder;
import org.jembi.jempi.api.models.OAuthCodeRequestPayload;
import org.jembi.jempi.libmpi.LibMPI;
import org.jembi.jempi.libmpi.MpiExpandedGoldenRecord;
import org.jembi.jempi.libmpi.MpiGeneralError;
import org.jembi.jempi.linker.CustomLinkerProbabilistic;
import org.jembi.jempi.shared.models.CustomEntity;
import org.jembi.jempi.shared.models.CustomGoldenRecord;
import org.jembi.jempi.shared.models.CustomMU;
import org.jembi.jempi.api.models.User;
import org.jembi.jempi.postgres.PsqlQueries;
import org.jembi.jempi.shared.models.LinkInfo;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.ServerRequest;
import org.keycloak.adapters.rotation.AdapterTokenVerifier;
import org.keycloak.common.VerificationException;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.AccessTokenResponse;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;

public class BackEnd extends AbstractBehavior<BackEnd.Event> {

    private static final Logger LOGGER = LogManager.getLogger(BackEnd.class);

    private static LibMPI libMPI = null;
    private AkkaAdapterConfig keycloakConfig;
    private KeycloakDeployment keycloak;

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
        final var host = new String[] { AppConfig.DGRAPH_ALPHA1_HOST, AppConfig.DGRAPH_ALPHA2_HOST,
                AppConfig.DGRAPH_ALPHA3_HOST };
        final var port = new int[] { AppConfig.DGRAPH_ALPHA1_PORT, AppConfig.DGRAPH_ALPHA2_PORT,
                AppConfig.DGRAPH_ALPHA3_PORT };
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
                .onMessage(EventGetDocumentCountReq.class, this::eventGetDocumentCountHandler)
                .onMessage(EventGetNumberOfRecordsReq.class, this::eventGetNumberOfRecordsHandler)
                .onMessage(EventGetGoldenIdListByPredicateReq.class, this::eventGetGoldenIdListByPredicateHandler)
                .onMessage(EventGetGoldenIdListReq.class, this::eventGetGoldenIdListHandler)
                .onMessage(EventGetGoldenRecordReq.class, this::eventGetGoldenRecordHandler)
                .onMessage(EventGetGoldenRecordDocumentsReq.class, this::eventGetGoldenRecordDocumentsHandler)
                .onMessage(EventGetDocumentReq.class, this::eventGetDocumentHandler)
                .onMessage(EventGetCandidatesReq.class, this::eventGetCandidatesHandler)
                .onMessage(EventPatchGoldenRecordPredicateReq.class, this::eventPatchGoldenRecordPredicateHandler)
                .onMessage(EventPatchLinkReq.class, this::eventPatchLinkHandler)
                .onMessage(EventGetMatchesForReviewReq.class, this::eventGetMatchesForReviewHandler)
                .onMessage(EventPatchUnLinkReq.class, this::eventPatchUnLinkHandler)
                .onMessage(EventNotificationRequestReq.class, this::eventNotificationRequestHandler)
                .build();
    }

    private Behavior<Event> eventLoginWithKeycloakHandler(final EventLoginWithKeycloakRequest request) {
        LOGGER.debug("loginWithKeycloak");
        LOGGER.debug("Logging in {}", request.payload);
        try {
            // Exchange code for a token from Keycloak
            AccessTokenResponse tokenResponse = ServerRequest.invokeAccessCodeToToken(keycloak, request.payload.code(), keycloakConfig.getRedirectUri(), request.payload.sessionId());
            LOGGER.debug("Token Exchange succeeded!");

            String tokenString = tokenResponse.getToken();
            String idTokenString = tokenResponse.getIdToken();

            AdapterTokenVerifier.VerifiedTokens tokens = AdapterTokenVerifier.verifyTokens(tokenString, idTokenString, keycloak);
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

    private Behavior<Event> eventGetDocumentCountHandler(final EventGetDocumentCountReq request) {
        LOGGER.debug("getDocumentCount");
        libMPI.startTransaction();
        final var count = libMPI.countEntities();
        libMPI.closeTransaction();
        request.replyTo.tell(new EventGetDocumentCountRsp(count));
        return Behaviors.same();
    }

    private Behavior<Event> eventGetNumberOfRecordsHandler(final EventGetNumberOfRecordsReq request) {
        LOGGER.debug("getNumberOfRecords");
        libMPI.startTransaction();
        var recs = libMPI.countGoldenRecords();
        var docs = libMPI.countEntities();
        libMPI.closeTransaction();
        request.replyTo.tell(new BackEnd.EventGetNumberOfRecordsRsp(recs, docs));
        return Behaviors.same();
    }

    private Behavior<Event> eventGetGoldenIdListByPredicateHandler(EventGetGoldenIdListByPredicateReq request) {
        LOGGER.debug("getGoldenRecordsByPredicate");
        libMPI.startTransaction();
        var recs = libMPI.getGoldenIdListByPredicate(request.predicate, request.val);
        request.replyTo.tell(new EventGetGoldenIdListByPredicateRsp(recs));
        libMPI.closeTransaction();
        return Behaviors.same();
    }

    private Behavior<Event> eventGetGoldenIdListHandler(final EventGetGoldenIdListReq request) {
        LOGGER.debug("getGoldenIdList");
        libMPI.startTransaction();
        var recs = libMPI.getGoldenIdList();
        request.replyTo.tell(new EventGetGoldenIdListRsp(recs));
        libMPI.closeTransaction();
        return Behaviors.same();
    }

    private Behavior<Event> eventGetGoldenRecordHandler(final EventGetGoldenRecordReq request) {
        LOGGER.debug("getGoldenRecord");
        libMPI.startTransaction();
        final var rec = libMPI.getGoldenRecord(request.uid);
        request.replyTo.tell(new EventGetGoldenRecordRsp(rec));
        libMPI.closeTransaction();
        return Behaviors.same();
    }

    private Behavior<Event> eventGetGoldenRecordDocumentsHandler(final EventGetGoldenRecordDocumentsReq request) {
        LOGGER.debug("getGoldenRecordDocuments");
        libMPI.startTransaction();
        final var mpiExpandedGoldenRecordList = libMPI.getMpiExpandedGoldenRecordList(request.uids);
        request.replyTo.tell(new EventGetGoldenRecordDocumentsRsp(mpiExpandedGoldenRecordList));
        libMPI.closeTransaction();
        return Behaviors.same();
    }

    private Behavior<Event> eventGetDocumentHandler(final EventGetDocumentReq request) {
        LOGGER.debug("getDocument");
        libMPI.startTransaction();
        final var document = libMPI.getDocument(request.uid);
        request.replyTo.tell(new EventGetDocumentRsp(document));
        libMPI.closeTransaction();
        return Behaviors.same();
    }

    private Behavior<Event> eventGetCandidatesHandler(final EventGetCandidatesReq request) {
        LOGGER.debug("getCandidates");
        LOGGER.debug("{} {}", request.docID, request.mu);
        libMPI.startTransaction();
        final var mpiEntity = libMPI.getMpiEntity(request.docID);
        final var recs = libMPI.getCandidates(mpiEntity, true);

        CustomLinkerProbabilistic.updateMU(request.mu);
        CustomLinkerProbabilistic.checkUpdatedMU();
        final var candidates = recs
                .stream()
                .map(candidate -> new EventGetCandidatesRsp.Candidate(candidate,
                        CustomLinkerProbabilistic.probabilisticScore(candidate, mpiEntity)))
                .toList();
        request.replyTo.tell(new EventGetCandidatesRsp(Either.right(candidates)));
        libMPI.closeTransaction();
        return Behaviors.same();
    }

    private Behavior<Event> eventPatchGoldenRecordPredicateHandler(final EventPatchGoldenRecordPredicateReq request) {
        final var result = libMPI.updateGoldenRecordPredicate(request.goldenID, request.predicate, request.value);
        if (result) {
            request.replyTo.tell(new EventPatchGoldenRecordPredicateRsp(0));
        } else {
            request.replyTo.tell(new EventPatchGoldenRecordPredicateRsp(-1));
        }
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

    interface Event {
    }

    interface EventResponse {
    }

    public record EventGetGoldenRecordCountReq(ActorRef<EventGetGoldenRecordCountRsp> replyTo) implements Event {
    }

    public record EventGetGoldenRecordCountRsp(long count) implements EventResponse {
    }

    public record EventGetDocumentCountReq(ActorRef<EventGetDocumentCountRsp> replyTo) implements Event {
    }

    public record EventGetDocumentCountRsp(long count) implements EventResponse {
    }

    public record EventGetNumberOfRecordsReq(ActorRef<EventGetNumberOfRecordsRsp> replyTo) implements Event {
    }

    public record EventGetNumberOfRecordsRsp(long goldenRecords, long documents) implements EventResponse {
    }

    public record EventGetGoldenIdListByPredicateReq(ActorRef<EventGetGoldenIdListByPredicateRsp> replyTo,
            String predicate,
            String val) implements Event {
    }

    public record EventGetGoldenIdListByPredicateRsp(List<String> records) implements EventResponse {
    }

    public record EventGetGoldenIdListReq(ActorRef<EventGetGoldenIdListRsp> replyTo) implements Event {
    }

    public record EventGetGoldenIdListRsp(List<String> records) implements EventResponse {
    }

    public record EventGetGoldenRecordReq(ActorRef<EventGetGoldenRecordRsp> replyTo, String uid) implements Event {
    }

    public record EventGetGoldenRecordRsp(CustomGoldenRecord goldenRecord) implements EventResponse {
    }

    public record EventGetGoldenRecordDocumentsReq(ActorRef<EventGetGoldenRecordDocumentsRsp> replyTo,
            List<String> uids) implements Event {
    }

    public record EventGetGoldenRecordDocumentsRsp(List<MpiExpandedGoldenRecord> goldenRecords)
            implements EventResponse {
    }

    public record EventGetDocumentReq(ActorRef<EventGetDocumentRsp> replyTo,
            String uid) implements Event {
    }

    public record EventGetMatchesForReviewReq(ActorRef<EventGetMatchesForReviewListRsp> replyTo) implements Event {}

    public record EventGetMatchesForReviewListRsp(List records) implements EventResponse {}
    
    public record EventGetDocumentRsp(CustomEntity document)
            implements EventResponse {
    }

    public record EventPatchGoldenRecordPredicateReq(ActorRef<EventPatchGoldenRecordPredicateRsp> replyTo,
            String goldenID,
            String predicate,
            String value) implements Event {
    }

    public record EventPatchGoldenRecordPredicateRsp(Integer result) implements EventResponse {
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
        record Candidate(CustomGoldenRecord goldenRecord, float score) {
        }
    }

    public record EventNotificationRequestReq(ActorRef<EventNotificationRequestRsp> replyTo,
                                              String notificationId,
                                              String state) implements Event {
    }

    public record EventNotificationRequestRsp() implements EventResponse {
    }

    public record EventLoginWithKeycloakRequest(ActorRef<EventLoginWithKeycloakResponse> replyTo,
                                                OAuthCodeRequestPayload payload) implements Event {
    }

    public record EventLoginWithKeycloakResponse(User user) implements EventResponse {
    }

}
