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
import org.jembi.jempi.shared.models.LinkInteractionSyncBody;
import org.jembi.jempi.shared.models.LinkInteractionToGidSyncBody;
import org.jembi.jempi.shared.utils.AppUtils;

import java.util.concurrent.CompletionStage;

public final class FrontEndStreamSync extends AllDirectives {

   private static final Logger LOGGER = LogManager.getLogger(FrontEndStreamSync.class);

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
                                 AppConfig.HTTP_SERVER_PORT)
                    .bind(this.createRoute(system, backEnd));
      LOGGER.info("Server online at http://{}:{}", "0.0.0.0", AppConfig.HTTP_SERVER_PORT);
   }

   private CompletionStage<HttpResponse> postLinkInteraction(final LinkInteractionSyncBody body) throws JsonProcessingException {
      final HttpRequest request;
      request = HttpRequest
            .create("http://linker:50000/JeMPI/link_patient")
            .withMethod(HttpMethods.POST)
            .withEntity(ContentTypes.APPLICATION_JSON, AppUtils.OBJECT_MAPPER.writeValueAsBytes(body));
      final var stage = http.singleRequest(request);
      return stage.thenApply(response -> response);
   }

   private CompletionStage<HttpResponse> postLinkInteractionToGid(final LinkInteractionToGidSyncBody body) throws JsonProcessingException {
      final var request = HttpRequest
            .create("http://linker:50000/JeMPI/link_patient_to_gid")
            .withMethod(HttpMethods.POST)
            .withEntity(ContentTypes.APPLICATION_JSON, AppUtils.OBJECT_MAPPER.writeValueAsBytes(body));
      final var stage = http.singleRequest(request);
      return stage.thenApply(response -> response);
   }

   private CompletionStage<HttpResponse> getMU() {
      final var request = HttpRequest
            .create("http://linker:50000/JeMPI/mu")
            .withMethod(HttpMethods.GET);
      final var stage = http.singleRequest(request);
      return stage.thenApply(response -> response);
   }

   private Route routeLinkPatient() {
      return entity(Jackson.unmarshaller(LinkInteractionSyncBody.class),
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

   private Route routeLinkPatientToGid() {
      return entity(Jackson.unmarshaller(LinkInteractionToGidSyncBody.class),
                    obj -> {
                       try {
                          return onComplete(postLinkInteractionToGid(obj),
                                            response -> response.isSuccess()
                                                  ? complete(response.get())
                                                  : complete(StatusCodes.IM_A_TEAPOT));
                       } catch (JsonProcessingException e) {
                          LOGGER.error(e.getLocalizedMessage(), e);
                          return complete(StatusCodes.IM_A_TEAPOT);
                       }
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
                                    path("link_patient", this::routeLinkPatient),
                                    path("link_patient_to_gid", this::routeLinkPatientToGid))),
                              get(() -> path("mu", this::routeMU))));
   }

}
