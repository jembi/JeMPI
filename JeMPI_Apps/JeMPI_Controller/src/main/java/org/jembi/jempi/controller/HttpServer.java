package org.jembi.jempi.controller;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.*;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.AppConfig;
import org.jembi.jempi.shared.models.ApiModels;
import org.jembi.jempi.shared.models.GlobalConstants;

import java.util.Locale;
import java.util.concurrent.CompletionStage;

import static org.jembi.jempi.shared.utils.AppUtils.OBJECT_MAPPER;

public final class HttpServer extends AllDirectives {

   private static final Logger LOGGER = LogManager.getLogger(HttpServer.class);

   private CompletionStage<ServerBinding> binding = null;
   private Http http = null;

   void close(final ActorSystem<Void> system) {
      binding.thenCompose(ServerBinding::unbind) // trigger unbinding from the port
             .thenAccept(unbound -> system.terminate()); // and shutdown when done
   }

   void open(
         final ActorSystem<Void> system,
         final ActorRef<BackEnd.Event> backEnd) {
      http = Http.get(system);
      binding = http.newServerAt("0.0.0.0",
                                 AppConfig.CONTROLLER_HTTP_PORT)
                    .bind(this.createRoute(system, backEnd));
      LOGGER.info("Server online at http://{}:{}", "0.0.0.0", AppConfig.CONTROLLER_HTTP_PORT);
   }

   private CompletionStage<HttpResponse> postLinkInteraction(final ApiModels.LinkInteractionSyncBody body) throws JsonProcessingException {
      final HttpRequest request;
      request = HttpRequest
            .create(String.format(Locale.ROOT,
                                  "http://%s:%d/JeMPI/%s",
                                  AppConfig.LINKER_IP,
                                  AppConfig.LINKER_HTTP_PORT,
                                  GlobalConstants.SEGMENT_PROXY_POST_LINK_INTERACTION))
            .withMethod(HttpMethods.POST)
            .withEntity(ContentTypes.APPLICATION_JSON, OBJECT_MAPPER.writeValueAsBytes(body));
      final var stage = http.singleRequest(request);
      return stage.thenApply(response -> response);
   }

   private CompletionStage<HttpResponse> postLinkInteractionToGid(final ApiModels.LinkInteractionToGidSyncBody body) throws JsonProcessingException {
      final var request = HttpRequest
            .create(String.format(Locale.ROOT,
                                  "http://%s:%d/JeMPI/%s",
                                  AppConfig.LINKER_IP,
                                  AppConfig.LINKER_HTTP_PORT,
                                  GlobalConstants.SEGMENT_PROXY_POST_LINK_INTERACTION_TO_GID))
            .withMethod(HttpMethods.POST)
            .withEntity(ContentTypes.APPLICATION_JSON, OBJECT_MAPPER.writeValueAsBytes(body));
      final var stage = http.singleRequest(request);
      return stage.thenApply(response -> response);
   }

   private CompletionStage<HttpResponse> getMU() {
      final var request = HttpRequest
            .create(String.format(Locale.ROOT, "http://%s:%d/JeMPI/mu", AppConfig.LINKER_IP, AppConfig.LINKER_HTTP_PORT))
            .withMethod(HttpMethods.GET);
      final var stage = http.singleRequest(request);
      return stage.thenApply(response -> response);
   }

   private Route routeLinkInteraction() {
      return entity(Jackson.unmarshaller(ApiModels.LinkInteractionSyncBody.class),
                    obj -> {
                       try {
                          LOGGER.debug("{}", obj);
                          return onComplete(postLinkInteraction(obj),
                                            response -> response.isSuccess()
                                                  ? complete(response.get())
                                                  : complete(StatusCodes.IM_A_TEAPOT));
                       } catch (JsonProcessingException e) {
                          LOGGER.error(e.getLocalizedMessage(), e);
                          return complete(StatusCodes.IM_A_TEAPOT);
                       }
                    });
   }

   private Route routeLinkInteractionToGid() {
      return entity(Jackson.unmarshaller(ApiModels.LinkInteractionToGidSyncBody.class),
                    obj -> {
                       try {
                          return onComplete(postLinkInteractionToGid(obj),
                                            response -> response.isSuccess()
                                                  ? complete(response.get())
                                                  : complete(ApiModels.getHttpErrorResponse(StatusCodes.IM_A_TEAPOT)));
                       } catch (JsonProcessingException e) {
                          LOGGER.error(e.getLocalizedMessage(), e);
                       }
                       return complete(ApiModels.getHttpErrorResponse(StatusCodes.IM_A_TEAPOT));
                    });
   }

   private Route routeMU() {
      return onComplete(getMU(),
                        response -> response.isSuccess()
                              ? complete(response.get())
                              : complete(StatusCodes.IM_A_TEAPOT));
   }

   private Route createRoute(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      return pathPrefix("JeMPI",
                        () -> concat(
                              post(() -> concat(
                                    path(GlobalConstants.SEGMENT_PROXY_POST_LINK_INTERACTION, this::routeLinkInteraction),
                                    path(GlobalConstants.SEGMENT_PROXY_POST_LINK_INTERACTION_TO_GID,
                                         this::routeLinkInteractionToGid))),
                              get(() -> path("mu", this::routeMU))));
   }

}
