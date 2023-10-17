package org.jembi.jempi.api;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.dispatch.MessageDispatcher;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.Route;
import ch.megard.akka.http.cors.javadsl.settings.CorsSettings;
import com.softwaremill.session.*;
import com.softwaremill.session.javadsl.HttpSessionAwareDirectives;
import com.softwaremill.session.javadsl.InMemoryRefreshTokenStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.jembi.jempi.AppConfig;
import org.jembi.jempi.libapi.Ask;
import org.jembi.jempi.libapi.BackEnd;
import org.jembi.jempi.libapi.Routes;
import org.jembi.jempi.shared.models.GlobalConstants;
import org.jembi.jempi.shared.models.RecordType;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.ServerRequest;
import org.keycloak.adapters.rotation.AdapterTokenVerifier;
import org.keycloak.common.VerificationException;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.AccessTokenResponse;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.regex.Pattern;

import static akka.http.javadsl.server.PathMatchers.segment;
import static ch.megard.akka.http.cors.javadsl.CorsDirectives.cors;
import static com.softwaremill.session.javadsl.SessionTransports.CookieST;

final class HttpServer extends HttpSessionAwareDirectives<UserSession> {

   private static final Logger LOGGER = LogManager.getLogger(HttpServer.class);

   private static final SessionEncoder<UserSession> BASIC_ENCODER = new BasicSessionEncoder<>(UserSession.getSerializer());
   // in-memory refresh token storage
   private static final RefreshTokenStorage<UserSession> REFRESH_TOKEN_STORAGE = new InMemoryRefreshTokenStorage<>() {
      @Override
      public void log(final String msg) {
         LOGGER.info(msg);
      }
   };
   private final Refreshable<UserSession> refreshable;
   private final SetSessionTransport sessionTransport;
   private CompletionStage<ServerBinding> binding = null;
   private AkkaAdapterConfig keycloakConfig = null;
   private KeycloakDeployment keycloak = null;

   private Http http = null;

   HttpServer(final MessageDispatcher dispatcher) {
      super(new SessionManager<>(SessionConfig.defaultConfig(AppConfig.SESSION_SECRET), BASIC_ENCODER));

      // use Refreshable for sessions, which needs to be refreshed or OneOff otherwise
      // using Refreshable, a refresh token is set in form of a cookie or a custom header
      refreshable = new Refreshable<>(getSessionManager(), REFRESH_TOKEN_STORAGE, dispatcher);

      // set the session transport - based on Cookies (or Headers)
      sessionTransport = CookieST;

      ClassLoader classLoader = getClass().getClassLoader();
      InputStream keycloakConfigStream = classLoader.getResourceAsStream("/keycloak.json");
      keycloakConfig = AkkaKeycloakDeploymentBuilder.loadAdapterConfig(keycloakConfigStream);
      keycloak = AkkaKeycloakDeploymentBuilder.build(keycloakConfig);
   }

   public void close(final ActorSystem<Void> actorSystem) {
      binding.thenCompose(ServerBinding::unbind) // trigger unbinding from the port
             .thenAccept(unbound -> actorSystem.terminate()); // and shutdown when done
   }

   public void open(
         final String httpServerHost,
         final int httpPort,
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final String jsonFields) {
      Configurator.setLevel(this.getClass(), AppConfig.GET_LOG_LEVEL);
      http = Http.get(actorSystem);
      binding = http.newServerAt(httpServerHost, httpPort)
                    .bind(this.createCorsRoutes(actorSystem, backEnd, jsonFields));
      LOGGER.info("Server online at http://{}:{}", httpServerHost, httpPort);
   }

   private Route patchGoldenRecord(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final String gid) {
      return requiredSession(refreshable, sessionTransport, session -> {
         if (session != null) {
            LOGGER.info("Current session: {}", session.getEmail());
            return Routes.patchGoldenRecord(actorSystem, backEnd, gid);
         }
         LOGGER.info("No active session");
         return complete(StatusCodes.FORBIDDEN);
      });
   }

   private Route getExpandedGoldenRecord(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final String gid) {
      return requiredSession(refreshable,
                             sessionTransport,
                             session -> Routes.getExpandedGoldenRecord(actorSystem, backEnd, gid));
   }

   private Route getInteraction(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final String iid) {
      return requiredSession(refreshable,
                             sessionTransport,
                             session -> Routes.getInteraction(actorSystem, backEnd, iid));
   }

//   private Route routeGetPatientResource(
//           final ActorSystem<Void> actorSystem,
//           final ActorRef<BackEnd.Event> backEnd,
//           final String patientResourceId) {
//      return onComplete(askFindPatientResource(actorSystem, backEnd, patientResourceId),
//              result -> result.isSuccess()
//                      ? result.get()
//                      .patientResource()
//                      .mapLeft(this::mapError)
//                      .fold(error -> error,
//                              patientResource -> complete(StatusCodes.OK,
//                                      patientResource
//                              ))
//                      : complete(StatusCodes.IM_A_TEAPOT));
//   }

//   private Route routeSessionGetPatientResource(
//           final ActorSystem<Void> actorSystem,
//           final ActorRef<BackEnd.Event> backEnd,
//           final String patientResourceId) {
//      return requiredSession(refreshable, sessionTransport, session -> Routes.routeGetPatientResource(actorSystem, backEnd,
//      patientResourceId));
//   }

   private User loginWithKeycloakHandler(final OAuthCodeRequestPayload payload) {
      LOGGER.debug("loginWithKeycloak");
      LOGGER.debug("Logging in {}", payload);
      try {
         // Exchange code for a token from Keycloak
         AccessTokenResponse tokenResponse = ServerRequest.invokeAccessCodeToToken(keycloak, payload.code(),
                                                                                   keycloakConfig.getRedirectUri(),
                                                                                   payload.sessionId());
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
         return user;
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
      return null;
   }

   private CompletionStage<User> askLoginWithKeycloak(
         final OAuthCodeRequestPayload body) {
      CompletionStage<User> stage = CompletableFuture.completedFuture(loginWithKeycloakHandler(body));
      return stage.thenApply(response -> response);
   }

   private Route routeLoginWithKeycloakRequest(final CheckHeader<UserSession> checkHeader) {
      return entity(
            Jackson.unmarshaller(OAuthCodeRequestPayload.class),
            obj -> onComplete(askLoginWithKeycloak(obj), response -> {
               if (response.isSuccess()) {
                  final var user = response.get();
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
            LOGGER.info("Current session: {}", session.getEmail());
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

   private Route postUploadCsvFile(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      return withSizeLimit(1024L * 1024L * 10L,
                           () -> requiredSession(refreshable, sessionTransport, session -> {
                              if (session != null) {
                                 LOGGER.info("Current session: {}", session.getEmail());
                                 return storeUploadedFile("csv",
                                                          info -> {
                                                             try {
                                                                return File.createTempFile("import-", ".csv");
                                                             } catch (Exception e) {
                                                                LOGGER.error("error", e);
                                                                return null;
                                                             }
                                                          },
                                                          (info, file) -> onComplete(Ask.postUploadCsvFile(actorSystem, backEnd,
                                                                                                           info, file),
                                                                                     response -> response.isSuccess()
                                                                                           ? complete(StatusCodes.OK)
                                                                                           : complete(StatusCodes.IM_A_TEAPOT)));
                              }
                              LOGGER.info("No active session");
                              return complete(StatusCodes.FORBIDDEN);
                           }));
   }

   private Route routeCustomSearch(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final RecordType recordType) {
      return requiredSession(refreshable, sessionTransport, session -> {
         LOGGER.info("Custom search on {}", recordType);
         // Simple search for golden records
         return Routes.postCustomSearch(actorSystem, backEnd, recordType);
      });
   }

   private Route createJeMPIRoutes(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      return concat(post(() -> concat(path(GlobalConstants.SEGMENT_POST_UPDATE_NOTIFICATION,
                                           () -> Routes.postUpdateNotification(actorSystem, backEnd)),
                                      path(segment(GlobalConstants.SEGMENT_POST_SIMPLE_SEARCH).slash(segment(Pattern.compile(
                                            "^(golden|patient)$"))), type -> {
                                         final var t = type.equals("golden")
                                               ? RecordType.GoldenRecord
                                               : RecordType.Interaction;
                                         return Routes.postSimpleSearch(actorSystem, backEnd, t);
                                      }),
                                      path(segment(GlobalConstants.SEGMENT_POST_CUSTOM_SEARCH).slash(segment(Pattern.compile(
                                            "^(golden|patient)$"))), type -> {
                                         final var t = type.equals("golden")
                                               ? RecordType.GoldenRecord
                                               : RecordType.Interaction;
                                         return this.routeCustomSearch(actorSystem, backEnd, t);
                                      }),
                                      path(GlobalConstants.SEGMENT_PROXY_GET_CANDIDATES_WITH_SCORES, () -> Routes.proxyGetCandidatesWithScore(AppConfig.LINKER_IP, AppConfig.LINKER_HTTP_PORT, http)),
                                      path(GlobalConstants.SEGMENT_POST_UPLOAD_CSV_FILE,
                                           () -> this.postUploadCsvFile(actorSystem, backEnd)))),
                    patch(() -> concat(path(segment(GlobalConstants.SEGMENT_PATCH_GOLDEN_RECORD).slash(segment(Pattern.compile(
                                             "^[A-z0-9]+$"))), gid -> this.patchGoldenRecord(actorSystem, backEnd, gid)),
                                       path(GlobalConstants.SEGMENT_PATCH_IID_NEW_GID_LINK,
                                            () -> Routes.patchIidNewGidLink(actorSystem, backEnd)),
                                       path(GlobalConstants.SEGMENT_PATCH_IID_GID_LINK,
                                            () -> Routes.patchIidGidLink(actorSystem, backEnd)))),
                    get(() -> concat(
                          path(GlobalConstants.SEGMENT_CURRENT_USER, this::routeCurrentUser),
                          path(GlobalConstants.SEGMENT_LOGOUT, this::routeLogout),
                          path(GlobalConstants.SEGMENT_COUNT_GOLDEN_RECORDS,
                               () -> Routes.countGoldenRecords(actorSystem, backEnd)),
                          path(GlobalConstants.SEGMENT_COUNT_INTERACTIONS,
                               () -> Routes.countInteractions(actorSystem, backEnd)),
                          path(GlobalConstants.SEGMENT_COUNT_RECORDS, () -> Routes.countRecords(actorSystem, backEnd)),
                          path(GlobalConstants.SEGMENT_GET_GIDS_ALL, () -> Routes.getGidsAll(actorSystem, backEnd)),
                          path(GlobalConstants.SEGMENT_GET_GIDS_PAGED, () -> Routes.getGidsPaged(actorSystem, backEnd)),
                          path(GlobalConstants.SEGMENT_GET_EXPANDED_GOLDEN_RECORDS_USING_PARAMETER_LIST,
                               () -> Routes.getExpandedGoldenRecordsUsingParameterList(actorSystem, backEnd)),
                          path(GlobalConstants.SEGMENT_GET_EXPANDED_GOLDEN_RECORDS_USING_CSV,
                               () -> Routes.getExpandedGoldenRecordsFromUsingCSV(actorSystem, backEnd)),
                          path(GlobalConstants.SEGMENT_GET_EXPANDED_INTERACTIONS_USING_CSV,
                               () -> Routes.getExpandedInteractionsUsingCSV(actorSystem, backEnd)),
                          path(GlobalConstants.SEGMENT_GET_NOTIFICATIONS,
                               () -> Routes.getNotifications(actorSystem, backEnd)),
                          path(GlobalConstants.SEGMENT_PROXY_GET_CANDIDATES_WITH_SCORES,
                               () -> Routes.proxyGetCandidatesWithScore(AppConfig.LINKER_IP, AppConfig.LINKER_HTTP_PORT, http)),
                          path(segment(GlobalConstants.SEGMENT_GET_INTERACTION).slash(segment(Pattern.compile("^[A-z0-9]+$"))),
                               iid -> this.getInteraction(actorSystem, backEnd, iid)),
                          path(segment(GlobalConstants.SEGMENT_GET_EXPANDED_GOLDEN_RECORD).slash(
                                     segment(Pattern.compile("^[A-z0-9]+$"))),
                               gid -> this.getExpandedGoldenRecord(actorSystem, backEnd, gid)))));
   }

   Route createCorsRoutes(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final String jsonFields) {
      final var settings = CorsSettings.create(AppConfig.CONFIG);
      final CheckHeader<UserSession> checkHeader = new CheckHeader<>(getSessionManager());
      return cors(
            settings,
            () -> randomTokenCsrfProtection(
                  checkHeader,
                  () -> pathPrefix("jempi",
                                   () -> concat(
                                         createJeMPIRoutes(actorSystem, backEnd),
                                         post(() -> path(GlobalConstants.SEGMENT_VALIDATE_OAUTH,
                                                         () -> routeLoginWithKeycloakRequest(checkHeader))),
                                         get(() -> path(GlobalConstants.SEGMENT_GET_FIELDS_CONFIG,
                                                        () -> setNewCsrfToken(checkHeader,
                                                                              () -> complete(StatusCodes.OK, jsonFields))))))));
   }

}
