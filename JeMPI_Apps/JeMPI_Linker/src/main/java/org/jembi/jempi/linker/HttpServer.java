package org.jembi.jempi.linker;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.AppConfig;
import org.jembi.jempi.linker.backend.BackEnd;
import org.jembi.jempi.shared.models.GlobalConstants;

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
      binding = http.newServerAt("0.0.0.0", AppConfig.LINKER_HTTP_PORT).bind(this.createRoute(system, backEnd));
      LOGGER.info("Server online at http://{}:{}", "0.0.0.0", AppConfig.LINKER_HTTP_PORT);
   }

   private Route createRoute(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Request> backEnd) {
      return pathPrefix("JeMPI",
                        () -> concat(patch(() -> path(GlobalConstants.SEGMENT_PROXY_PATCH_CR_UPDATE_FIELDS,
                                                      () -> Routes.proxyPatchCrUpdateField(actorSystem, backEnd))),
                                     post(() -> concat(path(GlobalConstants.SEGMENT_PROXY_POST_LINK_INTERACTION,
                                                            () -> Routes.proxyPostLinkInteraction(actorSystem, backEnd)),
                                                       path(GlobalConstants.SEGMENT_PROXY_POST_LINK_INTERACTION_TO_GID,
                                                            () -> Routes.proxyPostLinkInteractionToGID(actorSystem, backEnd)),
                                                       path(GlobalConstants.SEGMENT_PROXY_POST_CALCULATE_SCORES,
                                                            () -> Routes.proxyPostCalculateScores(actorSystem, backEnd)),
                                                       path(GlobalConstants.SEGMENT_PROXY_POST_CR_CANDIDATES,
                                                            () -> Routes.proxyGetCrCandidates(actorSystem, backEnd)),
                                                       path(GlobalConstants.SEGMENT_PROXY_POST_CR_FIND,
                                                            () -> Routes.proxyGetCrFind(actorSystem, backEnd)),
                                                       path(GlobalConstants.SEGMENT_PROXY_POST_CR_REGISTER,
                                                            () -> Routes.proxyPostCrRegister(actorSystem, backEnd)))),
                                     get(() -> concat(// path("mu", () -> Routes.routeMU(actorSystem, backEnd)),
                                                      path(GlobalConstants.SEGMENT_PROXY_GET_CANDIDATES_WITH_SCORES,
                                                           () -> Routes.proxyGetCandidatesWithScore(actorSystem, backEnd))))));
   }


}
