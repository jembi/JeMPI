package org.jembi.jempi.api.httpServer;

import akka.dispatch.MessageDispatcher;
import com.softwaremill.session.*;
import com.softwaremill.session.javadsl.InMemoryRefreshTokenStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.AppConfig;
import org.jembi.jempi.api.user.UserSession;

import static com.softwaremill.session.javadsl.SessionTransports.HeaderST;

public final class HttpServerSessionManager extends SessionManager<UserSession> {
    private static final Logger LOGGER = LogManager.getLogger(HttpServerSessionManager.class);
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

    public HttpServerSessionManager(final MessageDispatcher dispatcher) {
        super(SessionConfig.defaultConfig(AppConfig.SESSION_SECRET), BASIC_ENCODER);
        // use Refreshable for sessions, which needs to be refreshed or OneOff otherwise
        // using Refreshable, a refresh token is set in form of a cookie or a custom header
        refreshable = new Refreshable<>(this, REFRESH_TOKEN_STORAGE, dispatcher);

        // set the session transport - based on Cookies (or Headers)
        sessionTransport = HeaderST;
    }

    public Refreshable<UserSession> getRefreshable() {
        return refreshable;
    }

    public SetSessionTransport getSessionTransport() {
        return sessionTransport;
    }
}
