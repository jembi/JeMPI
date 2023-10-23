package org.jembi.jempi.api.httpServer.httpServerRoutes;

import akka.http.javadsl.server.Route;
import org.jembi.jempi.api.httpServer.HttpServer;
import org.jembi.jempi.api.httpServer.httpServerRoutes.routes.AdminRoutes;
import org.jembi.jempi.api.httpServer.httpServerRoutes.routes.PatientRoutes;
import org.jembi.jempi.api.httpServer.httpServerRoutes.routes.UserRoutes;


import static akka.http.javadsl.server.Directives.concat;

public class RoutesEntries extends ApiHttpServerRouteEntries {
    public RoutesEntries(HttpServer ihttpServer) {
        super(ihttpServer);
    }

    @Override
    public Route getRouteEntries() {
        return concat( new UserRoutes(this.httpServer).getRouteEntries(),
                        new PatientRoutes(this.httpServer).getRouteEntries(),
                        new AdminRoutes(this.httpServer).getRouteEntries());

    }
}
