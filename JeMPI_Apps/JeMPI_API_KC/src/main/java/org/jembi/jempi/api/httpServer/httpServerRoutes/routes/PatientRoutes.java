//package org.jembi.jempi.api.httpServer.HttpServerRoutes.Routes;
//
//import akka.actor.typed.ActorRef;
//import akka.actor.typed.ActorSystem;
//import akka.dispatch.MessageDispatcher;
//import akka.http.javadsl.Http;
//import akka.http.javadsl.ServerBinding;
//import akka.http.javadsl.marshallers.jackson.Jackson;
//import akka.http.javadsl.model.HttpResponse;
//import akka.http.javadsl.model.StatusCodes;
//import akka.http.javadsl.server.Route;
//import ch.megard.akka.http.cors.javadsl.settings.CorsSettings;
//import com.softwaremill.session.*;
//import com.softwaremill.session.javadsl.HttpSessionAwareDirectives;
//import com.softwaremill.session.javadsl.InMemoryRefreshTokenStorage;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//import org.apache.logging.log4j.core.config.Configurator;
//import org.jembi.jempi.AppConfig;
//import org.jembi.jempi.api.AkkaAdapterConfig;
//import org.jembi.jempi.api.AkkaKeycloakDeploymentBuilder;
//import org.jembi.jempi.api.OAuthCodeRequestPayload;
//import org.jembi.jempi.api.PsqlQueries;
//import org.jembi.jempi.api.user.User;
//import org.jembi.jempi.api.user.UserSession;
//import org.jembi.jempi.libapi.Ask;
//import org.jembi.jempi.libapi.BackEnd;
//import org.jembi.jempi.libapi.Routes;
//import org.jembi.jempi.shared.models.GlobalConstants;
//import org.jembi.jempi.shared.models.RecordType;
//import org.keycloak.adapters.KeycloakDeployment;
//import org.keycloak.adapters.ServerRequest;
//import org.keycloak.adapters.rotation.AdapterTokenVerifier;
//import org.keycloak.common.VerificationException;
//import org.keycloak.representations.AccessToken;
//import org.keycloak.representations.AccessTokenResponse;
//
//import java.io.File;
//import java.io.IOException;
//import java.io.InputStream;
//import java.util.concurrent.CompletableFuture;
//import java.util.concurrent.CompletionStage;
//import java.util.regex.Pattern;
//
//import static akka.http.javadsl.server.PathMatchers.segment;
//import static ch.megard.akka.http.cors.javadsl.CorsDirectives.cors;
//import static com.softwaremill.session.javadsl.SessionTransports.CookieST;
//
//public class PatientRoutes {
//    private Route routeCustomSearch(
//            final ActorSystem<Void> actorSystem,
//            final ActorRef<BackEnd.Event> backEnd,
//            final RecordType recordType) {
//        return requiredSession(refreshable, sessionTransport, session -> {
//            LOGGER.info("Custom search on {}", recordType);
//            // Simple search for golden records
//            return Routes.postCustomSearch(actorSystem, backEnd, recordType);
//        });
//    }
//
//    private Route patchGoldenRecord(
//            final ActorSystem<Void> actorSystem,
//            final ActorRef<BackEnd.Event> backEnd,
//            final String gid) {
//        return requiredSession(refreshable, sessionTransport, session -> {
//            if (session != null) {
//                LOGGER.info("Current session: {}", session.getEmail());
//                return Routes.patchGoldenRecord(actorSystem, backEnd, gid);
//            }
//            LOGGER.info("No active session");
//            return complete(StatusCodes.FORBIDDEN);
//        });
//    }
//
//    private Route getExpandedGoldenRecord(
//            final ActorSystem<Void> actorSystem,
//            final ActorRef<BackEnd.Event> backEnd,
//            final String gid) {
//        return requiredSession(refreshable,
//                sessionTransport,
//                session -> Routes.getExpandedGoldenRecord(actorSystem, backEnd, gid));
//    }
//
//    private Route getInteraction(
//            final ActorSystem<Void> actorSystem,
//            final ActorRef<BackEnd.Event> backEnd,
//            final String iid) {
//        return requiredSession(refreshable,
//                sessionTransport,
//                session -> Routes.getInteraction(actorSystem, backEnd, iid));
//    }
//
//    public String GetServerRoutes(){
//            return concat(
//                get(() -> concat(
//                            path(GlobalConstants.SEGMENT_COUNT_GOLDEN_RECORDS, () -> Routes.countGoldenRecords(actorSystem, backEnd)),
//                            path(GlobalConstants.SEGMENT_COUNT_INTERACTIONS,() -> Routes.countInteractions(actorSystem, backEnd)),
//                            path(GlobalConstants.SEGMENT_COUNT_RECORDS, () -> Routes.countRecords(actorSystem, backEnd)),
//                            path(GlobalConstants.SEGMENT_GET_GIDS_ALL, () -> Routes.getGidsAll(actorSystem, backEnd)),
//                            path(GlobalConstants.SEGMENT_GET_GIDS_PAGED, () -> Routes.getGidsPaged(actorSystem, backEnd)),
//                            path(GlobalConstants.SEGMENT_GET_EXPANDED_GOLDEN_RECORDS_USING_PARAMETER_LIST, () -> Routes.getExpandedGoldenRecordsUsingParameterList(actorSystem, backEnd)),
//                            path(GlobalConstants.SEGMENT_GET_EXPANDED_GOLDEN_RECORDS_USING_CSV, () -> Routes.getExpandedGoldenRecordsFromUsingCSV(actorSystem, backEnd)),
//                            path(GlobalConstants.SEGMENT_GET_EXPANDED_INTERACTIONS_USING_CSV, () -> Routes.getExpandedInteractionsUsingCSV(actorSystem, backEnd)),
//                            path(GlobalConstants.SEGMENT_GET_NOTIFICATIONS, () -> Routes.getNotifications(actorSystem, backEnd)),
//                            path(GlobalConstants.SEGMENT_PROXY_GET_CANDIDATES_WITH_SCORES, () -> Routes.proxyGetCandidatesWithScore(AppConfig.LINKER_IP, AppConfig.LINKER_HTTP_PORT, http)),
//                            path(segment(GlobalConstants.SEGMENT_GET_INTERACTION).slash(segment(Pattern.compile("^[A-z0-9]+$"))), iid -> this.getInteraction(actorSystem, backEnd, iid)),
//                            path(segment(GlobalConstants.SEGMENT_GET_EXPANDED_GOLDEN_RECORD).slash(segment(Pattern.compile("^[A-z0-9]+$"))), gid -> this.getExpandedGoldenRecord(actorSystem, backEnd, gid))
//                        )
//                ),
//                post(() -> concat(
//                                path(GlobalConstants.SEGMENT_POST_UPDATE_NOTIFICATION, () -> Routes.postUpdateNotification(actorSystem, backEnd)),
//                                path(segment(GlobalConstants.SEGMENT_POST_SIMPLE_SEARCH).slash(segment(Pattern.compile(
//                                                        "^(golden|patient)$"))), type -> {
//                                                    final var t = type.equals("golden")
//                                                        ? RecordType.GoldenRecord
//                                                        : RecordType.Interaction;
//                                                    return Routes.postSimpleSearch(actorSystem, backEnd, t);
//                                                }),
//                                path(segment(GlobalConstants.SEGMENT_POST_CUSTOM_SEARCH).slash(segment(Pattern.compile(
//                                                    "^(golden|patient)$"))), type -> {
//                                                final var t = type.equals("golden")
//                                                    ? RecordType.GoldenRecord
//                                                    : RecordType.Interaction;
//                                                return this.routeCustomSearch(actorSystem, backEnd, t);
//                                            }),
//                                path(GlobalConstants.SEGMENT_PROXY_GET_CANDIDATES_WITH_SCORES, () -> Routes.proxyGetCandidatesWithScore(AppConfig.LINKER_IP, AppConfig.LINKER_HTTP_PORT, http)),
//                                path(GlobalConstants.SEGMENT_POST_UPLOAD_CSV_FILE,() -> this.postUploadCsvFile(actorSystem, backEnd))
//                            )
//
//                ),
//                patch(() -> concat(
//                                path(segment(GlobalConstants.SEGMENT_PATCH_GOLDEN_RECORD).slash(segment(Pattern.compile(
//                                             "^[A-z0-9]+$"))), gid -> this.patchGoldenRecord(actorSystem, backEnd, gid)),
//                                path(GlobalConstants.SEGMENT_PATCH_IID_NEW_GID_LINK,
//                                            () -> Routes.patchIidNewGidLink(actorSystem, backEnd)),
//                                path(GlobalConstants.SEGMENT_PATCH_IID_GID_LINK,
//                                            () -> Routes.patchIidGidLink(actorSystem, backEnd)))),
//
//            )
//            return
//        }
//}
