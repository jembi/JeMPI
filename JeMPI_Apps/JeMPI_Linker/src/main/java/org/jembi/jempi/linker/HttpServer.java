package org.jembi.jempi.linker;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.server.AllDirectives;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.AppConfig;
import org.jembi.jempi.linker.backend.BackEnd;

import java.util.concurrent.CompletionStage;

final class HttpServer extends AllDirectives {

   private static final Logger LOGGER = LogManager.getLogger(HttpServer.class);
   private CompletionStage<ServerBinding> binding = null;

   private HttpServer() {
   }

   static HttpServer create() {
      return new HttpServer();
   }

   void close(final ActorSystem<Void> system) {
      binding.thenCompose(ServerBinding::unbind) // trigger unbinding from the port
             .thenAccept(unbound -> system.terminate()); // and shutdown when done
   }

   void open(
         final ActorSystem<Void> system,
         final ActorRef<BackEnd.Request> backEnd) {
      final Http http = Http.get(system);
      binding = http.newServerAt("0.0.0.0", AppConfig.LINKER_HTTP_PORT).bind(Routes.createRoute(system, backEnd));
      LOGGER.info("Server online at http://{}:{}", "0.0.0.0", AppConfig.LINKER_HTTP_PORT);
   }

}
