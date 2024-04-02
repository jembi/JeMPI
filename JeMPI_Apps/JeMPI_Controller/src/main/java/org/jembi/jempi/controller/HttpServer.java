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
import org.jembi.jempi.libmpi.MpiServiceError;
import org.jembi.jempi.shared.models.ApiModels;
import org.jembi.jempi.shared.models.GlobalConstants;
import org.jembi.jempi.shared.models.NotificationResolutionProcessorData;

import java.util.Locale;
import java.util.concurrent.CompletionStage;

import static org.jembi.jempi.controller.MapError.mapError;
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
      binding = http.newServerAt("0.0.0.0", AppConfig.CONTROLLER_HTTP_PORT).bind(this.createRoute(system, backEnd));
      LOGGER.info("Server online at http://{}:{}", "0.0.0.0", AppConfig.CONTROLLER_HTTP_PORT);
   }

   private CompletionStage<HttpResponse> postLinkInteraction(final ApiModels.LinkInteractionSyncBody body) throws JsonProcessingException {
      final HttpRequest request;
      request = HttpRequest.create(String.format(Locale.ROOT,
                                                 "http://%s:%d/JeMPI/%s",
                                                 AppConfig.LINKER_IP,
                                                 AppConfig.LINKER_HTTP_PORT,
                                                 GlobalConstants.SEGMENT_PROXY_POST_LINK_INTERACTION))
                           .withMethod(HttpMethods.POST)
                           .withEntity(ContentTypes.APPLICATION_JSON, OBJECT_MAPPER.writeValueAsBytes(body));
      final var stage = http.singleRequest(request);
      return stage.thenApply(response -> response);
   }

/*
   private CompletionStage<HttpResponse> postLinkInteractionToGid(final ApiModels.LinkInteractionToGidSyncBody body) throws JsonProcessingException {
      final var request = HttpRequest.create(String.format(Locale.ROOT,
                                                           "http://%s:%d/JeMPI/%s",
                                                           AppConfig.LINKER_IP,
                                                           AppConfig.LINKER_HTTP_PORT,
                                                           GlobalConstants.SEGMENT_PROXY_POST_LINK_INTERACTION_TO_GID))
                                     .withMethod(HttpMethods.POST)
                                     .withEntity(ContentTypes.APPLICATION_JSON, OBJECT_MAPPER.writeValueAsBytes(body));
      final var stage = http.singleRequest(request);
      return stage.thenApply(response -> response);
   }
*/

   private CompletionStage<HttpResponse> getMU() {
      final var request = HttpRequest.create(String.format(Locale.ROOT,
                                                           "http://%s:%d/JeMPI/mu",
                                                           AppConfig.LINKER_IP,
                                                           AppConfig.LINKER_HTTP_PORT)).withMethod(HttpMethods.GET);
      final var stage = http.singleRequest(request);
      return stage.thenApply(response -> response);
   }

   private Route onNotificationResolution(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      return entity(Jackson.unmarshaller(NotificationResolutionProcessorData.class),
                    obj -> onComplete(BackEnd.askOnNotificationResolution(actorSystem, backEnd, obj), response -> {
                       if (response.isSuccess() && Boolean.TRUE.equals(response.get().updated())) {
                          return complete(StatusCodes.OK);
                       } else {
                          final var e = response.failed().get();
                          LOGGER.error(e.getLocalizedMessage(), e);
                          return mapError(new MpiServiceError.InternalError(e.getLocalizedMessage()));
                       }
                    }));
   }

   private Route routeDashboardData(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      return onComplete(BackEnd.askGetDashboardData(actorSystem, backEnd), response -> {
         if (response.isSuccess()) {
            return complete(StatusCodes.OK, response.get(), Jackson.marshaller());
         } else {
            final var e = response.failed().get();
            LOGGER.error(e.getLocalizedMessage(), e);
            return mapError(new MpiServiceError.InternalError(e.getLocalizedMessage()));
         }
      });
   }

   private Route routeLinkInteraction() {
      return entity(Jackson.unmarshaller(ApiModels.LinkInteractionSyncBody.class), obj -> {
         try {
            LOGGER.debug("{}", obj);
            return onComplete(postLinkInteraction(obj),
                              response -> {
                                 if (!response.isSuccess()) {
                                    final var e = response.failed().get();
                                    LOGGER.error(e.getLocalizedMessage(), e);
                                    return mapError(new MpiServiceError.InternalError(e.getLocalizedMessage()));
                                 }
                                 return complete(response.get());
                              });
         } catch (JsonProcessingException e) {
            LOGGER.error(e.getLocalizedMessage(), e);
            return mapError(new MpiServiceError.InternalError(e.getLocalizedMessage()));
         }
      });
   }

/*
   private Route routeLinkInteractionToGid() {
      return entity(Jackson.unmarshaller(ApiModels.LinkInteractionToGidSyncBody.class), obj -> {
         try {
            return onComplete(postLinkInteractionToGid(obj),
                              response -> {
                                 if (!response.isSuccess()) {
                                    LOGGER.warn("IM_A_TEAPOT");
                                 }
                                 return response.isSuccess()
                                       ? complete(response.get())
                                       : complete(ApiModels.getHttpErrorResponse(GlobalConstants.IM_A_TEA_POT));
                              });
         } catch (JsonProcessingException e) {
            LOGGER.error(e.getLocalizedMessage(), e);
         }
         LOGGER.warn("IM_A_TEAPOT");
         return complete(ApiModels.getHttpErrorResponse(GlobalConstants.IM_A_TEA_POT));
      });
   }
*/

   private Route routeMU() {
      return onComplete(getMU(),
                        response -> {
                           if (!response.isSuccess()) {
                              final var e = response.failed().get();
                              LOGGER.error(e.getLocalizedMessage(), e);
                              return mapError(new MpiServiceError.InternalError(e.getLocalizedMessage()));
                           }
                           return complete(response.get());
                        });
   }

   private Route createRoute(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      return pathPrefix("JeMPI",
                        () -> concat(post(() -> concat(path(GlobalConstants.SEGMENT_PROXY_POST_LINK_INTERACTION,
                                                            this::routeLinkInteraction),
//                                                       path(GlobalConstants.SEGMENT_PROXY_POST_LINK_INTERACTION_TO_GID,
//                                                            this::routeLinkInteractionToGid),
                                                       path(GlobalConstants.SEGMENT_PROXY_ON_NOTIFICATION_RESOLUTION,
                                                            () -> onNotificationResolution(actorSystem, backEnd)))),
                                     get(() -> concat(path("mu", this::routeMU),
                                                      path(GlobalConstants.SEGMENT_PROXY_GET_DASHBOARD_DATA,
                                                           () -> routeDashboardData(actorSystem, backEnd))
                                                     ))));
   }

}
