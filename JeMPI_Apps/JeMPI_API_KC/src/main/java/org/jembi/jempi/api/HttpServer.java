package org.jembi.jempi.api;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.jembi.jempi.AppConfig;
import org.jembi.jempi.libapi.BackEnd;
import org.jembi.jempi.libapi.Routes;
import org.jembi.jempi.shared.models.GlobalConstants;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.ServerRequest;
import org.keycloak.adapters.rotation.AdapterTokenVerifier;
import org.keycloak.common.VerificationException;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.AccessTokenResponse;

import com.softwaremill.session.BasicSessionEncoder;
import com.softwaremill.session.CheckHeader;
import com.softwaremill.session.RefreshTokenStorage;
import com.softwaremill.session.Refreshable;
import com.softwaremill.session.SessionConfig;
import com.softwaremill.session.SessionEncoder;
import com.softwaremill.session.SessionManager;
import com.softwaremill.session.SetSessionTransport;
import com.softwaremill.session.javadsl.HttpSessionAwareDirectives;
import com.softwaremill.session.javadsl.InMemoryRefreshTokenStorage;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.dispatch.MessageDispatcher;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.ExceptionHandler;
import akka.http.javadsl.server.RejectionHandler;
import akka.http.javadsl.server.Route;
import ch.megard.akka.http.cors.javadsl.settings.CorsSettings;

import static ch.megard.akka.http.cors.javadsl.CorsDirectives.cors;
import static com.softwaremill.session.javadsl.SessionTransports.CookieST;

final class HttpServer extends HttpSessionAwareDirectives<UserSession> {

   private static final Logger LOGGER = LogManager.getLogger(HttpServer.class);

   private static final SessionEncoder<UserSession> BASIC_ENCODER = new BasicSessionEncoder<>(
         UserSession.getSerializer());
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

   HttpServer(final MessageDispatcher dispatcher) {
      super(new SessionManager<>(SessionConfig.defaultConfig(AppConfig.SESSION_SECRET), BASIC_ENCODER));

      // use Refreshable for sessions, which needs to be refreshed or OneOff otherwise
      // using Refreshable, a refresh token is set in form of a cookie or a custom
      // header
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
      Http http = Http.get(actorSystem);
      binding = http.newServerAt(httpServerHost, httpPort)
            .bind(this.createSecureRoutes(actorSystem, backEnd, jsonFields, http));
      LOGGER.debug("SECURE Server online at http://{}:{}", httpServerHost, httpPort);
   }

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
         LOGGER.debug("Query user: {}", user);
         if (user == null) {
            // Register new user
            LOGGER.debug("Registering user: {}", email);
            User newUser = User.buildUserFromToken(token);
            user = PsqlQueries.registerUser(newUser);
         }
         LOGGER.debug("User has signed in : {}", email);
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
      LOGGER.info("In routeLoginWithKeycloakRequest");
      return entity(
            Jackson.unmarshaller(OAuthCodeRequestPayload.class),
            obj -> onComplete(askLoginWithKeycloak(obj), response -> {
               LOGGER.info(response);
               if (response.isSuccess()) {
                  final var user = response.get();
                  LOGGER.info(user);
                  if (user != null) {
                     return setSession(refreshable,
                           sessionTransport,
                           new UserSession(user),
                           () -> setNewCsrfToken(checkHeader,
                                 () -> complete(StatusCodes.OK, user, Jackson.<User>marshaller())));
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

   private Route createSecureRoutes(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final String jsonFields,
         final Http http) {
      final var settings = CorsSettings.create(AppConfig.CONFIG);
      final CheckHeader<UserSession> checkHeader = new CheckHeader<>(getSessionManager());
      final RejectionHandler rejectionHandler = RejectionHandler.defaultHandler();
      final ExceptionHandler exceptionHandler = ExceptionHandler.newBuilder()
            .match(Exception.class, x -> {
               LOGGER.error("An exception ocurred while executing the Route", x);
               return complete(StatusCodes.INTERNAL_SERVER_ERROR, "An exception occurred, see server logs for details");
            }).build();

      return cors(
            settings,
            () -> randomTokenCsrfProtection(
                  checkHeader,
                  () -> pathPrefix("JeMPI",
                        () -> concat(
                              requiredSession(refreshable, sessionTransport,
                                    session -> Routes.createCoreAPIRoutes(actorSystem, backEnd, jsonFields, http)),
                              post(() -> path(GlobalConstants.SEGMENT_VALIDATE_OAUTH,
                                    () -> routeLoginWithKeycloakRequest(checkHeader))),
                              get(() -> concat(
                                    path(GlobalConstants.SEGMENT_CURRENT_USER, this::routeCurrentUser),
                                    path(GlobalConstants.SEGMENT_LOGOUT, this::routeLogout),
                                    path(GlobalConstants.SEGMENT_GET_FIELDS_CONFIG,
                                          () -> complete(StatusCodes.OK, jsonFields))))))))
            .seal(rejectionHandler, exceptionHandler);
   }

}
