package org.jembi.jempi.api.httpServer.httpServerRoutes;

import akka.http.javadsl.server.Route;
import com.softwaremill.session.CheckHeader;
import org.jembi.jempi.api.httpServer.HttpServer;
import org.jembi.jempi.api.httpServer.HttpServerSessionManager;
import org.jembi.jempi.api.user.UserSession;
import org.jembi.jempi.libapi.httpServer.HttpServerRouteEntries;

public abstract class ApiHttpServerRouteEntries extends HttpServerRouteEntries<Route, HttpServer> {
    protected HttpServerSessionManager sessionManager;
    protected CheckHeader<UserSession> checkHeader;
    public ApiHttpServerRouteEntries(HttpServer ihttpServer) {
        super(ihttpServer);
        sessionManager = (HttpServerSessionManager) this.httpServer.getSessionManager();
        checkHeader = new CheckHeader<>(sessionManager);
    }


    @Override
    public abstract Route getRouteEntries();
}
