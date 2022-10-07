package org.jembi.jempi.stagingdisi;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.AppConfig;

import java.util.concurrent.CompletionStage;

public class FrontEndRest extends AllDirectives {
   private static final Logger LOGGER = LogManager.getLogger(FrontEndRest.class);

   private CompletionStage<ServerBinding> binding = null;
   private Http http = null;

   void close(ActorSystem<Void> system) {
      binding.thenCompose(ServerBinding::unbind) // trigger unbinding from the port
             .thenAccept(unbound -> system.terminate()); // and shutdown when done
   }

   void open(final ActorSystem<Void> system,
             final ActorRef<BackEnd.Event> backEnd) {
      http = Http.get(system);
      binding = http.newServerAt(AppConfig.HTTP_SERVER_HOST,
                                 AppConfig.HTTP_SERVER_PORT)
                    .bind(this.createRoute(system, backEnd));
      LOGGER.info("Server online at http://{}:{}", AppConfig.HTTP_SERVER_HOST, AppConfig.HTTP_SERVER_PORT);
   }

   private Route createRoute(final ActorSystem<Void> actorSystem, final ActorRef<BackEnd.Event> backEnd) {
      return pathPrefix("JeMPI",
                        () -> concat(
                              post(() -> concat(
                                    path("test1", () -> complete(StatusCodes.OK)),
                                    path("test2", () -> complete(StatusCodes.OK)))),
                              get(() -> path("mu", () -> complete(StatusCodes.OK)))));
   }

}