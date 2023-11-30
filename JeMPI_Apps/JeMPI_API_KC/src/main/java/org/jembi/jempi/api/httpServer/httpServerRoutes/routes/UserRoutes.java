package org.jembi.jempi.api.httpServer.httpServerRoutes.routes;

import com.softwaremill.session.CheckHeader;

import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.Route;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.api.httpServer.HttpServer;
import org.jembi.jempi.api.httpServer.httpServerRoutes.ApiHttpServerRouteEntries;
import org.jembi.jempi.api.keyCloak.KeyCloakAuthenticator;
import org.jembi.jempi.api.user.UserSession;
import org.jembi.jempi.api.keyCloak.OAuthCodeRequestPayload;
import org.jembi.jempi.shared.models.GlobalConstants;

import static akka.http.javadsl.server.Directives.*;

public final class UserRoutes extends ApiHttpServerRouteEntries {
    private static final Logger LOGGER = LogManager.getLogger(UserRoutes.class);
    private final KeyCloakAuthenticator keyCloakAuthenticator;
    public UserRoutes(final HttpServer ihttpServer) {
        super(ihttpServer);
        keyCloakAuthenticator =  new KeyCloakAuthenticator();
    }
    private Route routeLoginWithKeycloakRequest(final CheckHeader<UserSession> checkHeader) {

            return entity(
                    Jackson.unmarshaller(OAuthCodeRequestPayload.class),
                    obj -> onComplete(keyCloakAuthenticator.askLoginWithKeycloak(obj), response -> {
                        if (response.isSuccess()) {
                            final var user = response.get();
                            if (user != null) {
                                return this.httpServer.setSession(sessionManager.getRefreshable(),
                                        sessionManager.getSessionTransport(),
                                        new UserSession(user),
                                        () -> this.httpServer.setNewCsrfToken(checkHeader,
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
            return this.httpServer.optionalSession(sessionManager.getRefreshable(), sessionManager.getSessionTransport(), session -> {
                if (session.isPresent()) {
                    LOGGER.info("Current session: {}", session.get().getUsername());
                    return complete(StatusCodes.OK, session, Jackson.marshaller());
                }
                LOGGER.info("No active session");
                return complete(StatusCodes.OK, "");
            });
        }

        private Route routeLogout() {
            return this.httpServer.requiredSession(sessionManager.getRefreshable(), sessionManager.getSessionTransport(),
                    session -> this.httpServer.invalidateSession(sessionManager.getRefreshable(), sessionManager.getSessionTransport(), () -> extractRequestContext(ctx -> {
                        LOGGER.info("Logging out {}", session.getUsername());
                        return onSuccess(() -> ctx.completeWith(HttpResponse.create()),
                                routeResult -> complete("success"));
                    })));
        }

        @Override
        public Route getRouteEntries() {
            return concat(
                    post(() -> path(GlobalConstants.SEGMENT_VALIDATE_OAUTH, () -> routeLoginWithKeycloakRequest(checkHeader))),
                    get(() -> concat(
                            path(GlobalConstants.SEGMENT_GET_FIELDS_CONFIG, () -> httpServer.setNewCsrfToken(checkHeader, () -> complete(StatusCodes.OK, httpServer.getJsonFields()))),
                            path(GlobalConstants.SEGMENT_CURRENT_USER, this::routeCurrentUser),
                            path(GlobalConstants.SEGMENT_LOGOUT, this::routeLogout))
                    )

            );
        }
}
