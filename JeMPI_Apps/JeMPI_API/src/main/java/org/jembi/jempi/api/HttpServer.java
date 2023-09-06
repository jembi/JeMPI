package org.jembi.jempi.api;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import akka.http.javadsl.model.StatusCodes;
import ch.megard.akka.http.cors.javadsl.settings.CorsSettings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.jembi.jempi.AppConfig;
import org.jembi.jempi.libapi.BackEnd;
import org.jembi.jempi.libapi.Routes;
import org.jembi.jempi.shared.models.GlobalConstants;

import java.util.concurrent.CompletionStage;

import static ch.megard.akka.http.cors.javadsl.CorsDirectives.cors;

public final class HttpServer extends AllDirectives {

   private static final Logger LOGGER = LogManager.getLogger(HttpServer.class);

   private CompletionStage<ServerBinding> binding = null;

   private HttpServer() {
      Configurator.setLevel(this.getClass(), AppConfig.GET_LOG_LEVEL);
   }

   static HttpServer create() {
      return new HttpServer();
   }

   public void open(
         final String httpServerHost,
         final int httpPort,
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final String jsonFields) {
      Http http = Http.get(actorSystem);
      binding = http.newServerAt(httpServerHost, httpPort).bind(createCorsRoutes(actorSystem, backEnd, jsonFields, http));
      LOGGER.info("Server online at http://{}:{}", httpServerHost, httpPort);
   }

   public void close(final ActorSystem<Void> actorSystem) {
      binding.thenCompose(ServerBinding::unbind) // trigger unbinding from the port
             .thenAccept(unbound -> actorSystem.terminate()); // and shutdown when done
   }

   public Route createCorsRoutes(
           final ActorSystem<Void> actorSystem,
           final ActorRef<BackEnd.Event> backEnd,
           final String jsonFields,
           final Http http) {
      final var settings = CorsSettings.create(AppConfig.CONFIG);
      return cors(settings, () -> pathPrefix("JeMPI", () -> concat(
         Routes.createJeMPIRoutes(actorSystem, backEnd, jsonFields, http),
         path(GlobalConstants.SEGMENT_GET_FIELDS_CONFIG, () -> complete(StatusCodes.OK, jsonFields))
      )));
   }

}
