package org.jembi.jempi.api.httpServer;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.dispatch.MessageDispatcher;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.HttpEntity;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.ExceptionHandler;
import akka.http.javadsl.server.RejectionHandler;
import akka.http.javadsl.server.Route;
import ch.megard.akka.http.cors.javadsl.settings.CorsSettings;
import com.softwaremill.session.javadsl.HttpSessionAwareDirectives;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.jembi.jempi.AppConfig;
import org.jembi.jempi.api.httpServer.httpServerRoutes.RoutesEntries;
import org.jembi.jempi.api.user.UserSession;
import org.jembi.jempi.libapi.BackEnd;

import java.util.concurrent.CompletionStage;

import static ch.megard.akka.http.cors.javadsl.CorsDirectives.cors;

public final class HttpServer extends HttpSessionAwareDirectives<UserSession> {
   private static final Logger LOGGER = LogManager.getLogger(HttpServer.class);
   private CompletionStage<ServerBinding> binding = null;
   private ActorSystem<Void> actorSystem;
   private ActorRef<BackEnd.Event> backEnd;
   private Http akkaHttpServer = null;

   public HttpServer(final MessageDispatcher dispatcher) {
      super(new HttpServerSessionManager(dispatcher));
   }

   public void close(final ActorSystem<Void> actorSystem) {
      binding.thenCompose(ServerBinding::unbind) // trigger unbinding from the port
             .thenAccept(unbound -> actorSystem.terminate()); // and shutdown when done
   }

   public void open(
         final String httpServerHost,
         final int httpPort,
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {

      this.actorSystem = actorSystem;
      this.backEnd = backEnd;
      Configurator.setLevel(this.getClass(), AppConfig.GET_LOG_LEVEL);

      akkaHttpServer = Http.get(actorSystem);
      binding = akkaHttpServer.newServerAt(httpServerHost, httpPort).bind(this.createCorsRoutes());
      LOGGER.info("Server online at http://{}:{}", httpServerHost, httpPort);
   }

   public ActorSystem<Void> getActorSystem() {
      return actorSystem;
   }

   public Http getAkkaHttpServer() {
      return akkaHttpServer;
   }

   public ActorRef<BackEnd.Event> getBackEnd() {
      return backEnd;
   }

   Route createCorsRoutes() {
      final RejectionHandler rejectionHandler = RejectionHandler.defaultHandler().mapRejectionResponse(response -> {
         if (response.entity() instanceof HttpEntity.Strict) {
            String message = ((HttpEntity.Strict) response.entity()).getData().utf8String();
            LOGGER.warn("Request was rejected. Reason: %s".formatted(message));
         }

         return response;
      });
      final ExceptionHandler exceptionHandler = ExceptionHandler.newBuilder().match(Exception.class, x -> {
         LOGGER.error("An exception occurred while executing the Route", x);
         return complete(StatusCodes.INTERNAL_SERVER_ERROR, "An exception occurred. Please see server logs for details");
      }).build();


      return cors(CorsSettings.create(AppConfig.CONFIG),
                  () -> pathPrefix("JeMPI", () -> new RoutesEntries(this).getRouteEntries())).seal(rejectionHandler,
                                                                                                   exceptionHandler);
   }

}
