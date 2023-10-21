package org.jembi.jempi.api.httpServer.httpServerRoutes;

import akka.http.javadsl.server.Route;
import com.softwaremill.session.javadsl.HttpSessionAwareDirectives;
import org.jembi.jempi.api.httpServer.HttpServer;
import org.jembi.jempi.api.httpServer.httpServerRoutes.routes.UserRoutes;
import org.jembi.jempi.api.user.UserSession;
import org.jembi.jempi.libapi.httpServer.HttpServerRouteEntries;

import static akka.http.javadsl.server.Directives.concat;

public class RoutesEntries extends ApiHttpServerRouteEntries {
    public RoutesEntries(HttpServer ihttpServer) {
        super(ihttpServer);
    }

    @Override
    public Route getRouteEntries() {
        return concat( new UserRoutes(this.httpServer).getRouteEntries());
    }
}
