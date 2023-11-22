package org.jembi.jempi.api.httpServer.httpServerRoutes;

import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.Route;
import com.softwaremill.session.CheckHeader;
import org.jembi.jempi.api.httpServer.HttpServer;
import org.jembi.jempi.api.httpServer.HttpServerSessionManager;
import org.jembi.jempi.api.user.UserSession;
import org.jembi.jempi.libapi.httpServer.HttpServerRouteEntries;

import static akka.http.javadsl.server.Directives.complete;

public abstract class ApiHttpServerRouteEntries extends HttpServerRouteEntries<Route, HttpServer> {
    protected HttpServerSessionManager sessionManager;
    protected CheckHeader<UserSession> checkHeader;
    public ApiHttpServerRouteEntries(HttpServer ihttpServer) {
        super(ihttpServer);
        sessionManager = (HttpServerSessionManager) this.httpServer.getSessionManager();
        checkHeader = new CheckHeader<>(sessionManager);
    }

    protected Route requireSession(Route routes){
        return  this.httpServer.requiredSession(sessionManager.getRefreshable(), sessionManager.getSessionTransport(), session -> {
            if (session != null) {
                return routes;
            }
            return complete(StatusCodes.FORBIDDEN);
        });
    }
    @Override
    public abstract Route getRouteEntries();
}
