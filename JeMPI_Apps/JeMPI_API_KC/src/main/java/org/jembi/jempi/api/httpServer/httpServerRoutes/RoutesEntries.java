package org.jembi.jempi.api.httpServer.httpServerRoutes;

import akka.http.javadsl.server.Route;
import org.jembi.jempi.AppConfig;
import org.jembi.jempi.api.httpServer.HttpServer;
import org.jembi.jempi.api.httpServer.httpServerRoutes.routes.UserRoutes;
import org.jembi.jempi.libapi.Routes;

import static akka.http.javadsl.server.Directives.concat;

public final class RoutesEntries extends ApiHttpServerRouteEntries {
   public RoutesEntries(final HttpServer ihttpServer) {
      super(ihttpServer);
   }

   @Override
   public Route getRouteEntries() {

      return concat(new UserRoutes(this.httpServer).getRouteEntries(),
                    requireSession(Routes.createCoreAPIRoutes(this.httpServer.getActorSystem(),
                                                              this.httpServer.getBackEnd(),
                                                              this.httpServer.getJsonFields(),
                                                              AppConfig.LINKER_IP,
                                                              AppConfig.LINKER_HTTP_PORT,
                                                              this.httpServer.getAkkaHttpServer())));

   }
}
