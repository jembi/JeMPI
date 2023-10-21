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
//public class AdminRoutes {
//     private Route postUploadCsvFile(
//                final ActorSystem<Void> actorSystem,
//                final ActorRef<BackEnd.Event> backEnd) {
//            return withSizeLimit(1024L * 1024L * 10L,
//                    () -> requiredSession(refreshable, sessionTransport, session -> {
//                        if (session != null) {
//                            LOGGER.info("Current session: {}", session.getEmail());
//                            return storeUploadedFile("csv",
//                                    info -> {
//                                        try {
//                                            return File.createTempFile("import-", ".csv");
//                                        } catch (Exception e) {
//                                            LOGGER.error("error", e);
//                                            return null;
//                                        }
//                                    },
//                                    (info, file) -> onComplete(Ask.postUploadCsvFile(actorSystem, backEnd,
//                                            info, file),
//                                            response -> response.isSuccess()
//                                                    ? complete(StatusCodes.OK)
//                                                    : complete(StatusCodes.IM_A_TEAPOT)));
//                        }
//                        LOGGER.info("No active session");
//                        return complete(StatusCodes.FORBIDDEN);
//                    }));
//        }
//
//        public String GetServerRoutes(){
//            return post(() -> concat(
//                path(GlobalConstants.SEGMENT_POST_UPLOAD_CSV_FILE,() -> this.postUploadCsvFile(actorSystem, backEnd))
//            )
//        }
//}
