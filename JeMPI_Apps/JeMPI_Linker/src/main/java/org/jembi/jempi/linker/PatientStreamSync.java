package org.jembi.jempi.linker;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.AskPattern;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.AppConfig;
import org.jembi.jempi.shared.models.CalculateScoresRequest;
import org.jembi.jempi.shared.models.ExtendedLinkInfo;
import org.jembi.jempi.shared.models.LinkPatientSyncBody;
import org.jembi.jempi.shared.models.LinkPatientToGidSyncBody;

import java.util.concurrent.CompletionStage;

final class PatientStreamSync extends AllDirectives {

   private static final Logger LOGGER = LogManager.getLogger(PatientStreamSync.class);
   private CompletionStage<ServerBinding> binding = null;

   private PatientStreamSync() {
   }

   static PatientStreamSync create() {
      return new PatientStreamSync();
   }

   void close(final ActorSystem<Void> system) {
      binding.thenCompose(ServerBinding::unbind) // trigger unbinding from the port
             .thenAccept(unbound -> system.terminate()); // and shutdown when done
   }

   void open(
         final ActorSystem<Void> system,
         final ActorRef<BackEnd.Event> backEnd) {
      final Http http = Http.get(system);
      binding = http.newServerAt(AppConfig.HTTP_SERVER_HOST, AppConfig.HTTP_SERVER_PORT).bind(this.createRoute(system, backEnd));
      LOGGER.info("Server online at http://{}:{}", AppConfig.HTTP_SERVER_HOST, AppConfig.HTTP_SERVER_PORT);
   }

   private CompletionStage<BackEnd.EventLinkPatientSyncRsp> postLinkPatient(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final LinkPatientSyncBody body) {
      CompletionStage<BackEnd.EventLinkPatientSyncRsp> stage = AskPattern.ask(backEnd,
                                                                              replyTo -> new BackEnd.EventLinkPatientSyncReq(body,
                                                                                                                             replyTo),
                                                                              java.time.Duration.ofSeconds(11),
                                                                              actorSystem.scheduler());
      return stage.thenApply(response -> response);
   }

   private Route routeLinkPatient(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      return entity(Jackson.unmarshaller(LinkPatientSyncBody.class),
                    obj -> onComplete(postLinkPatient(actorSystem, backEnd, obj), response -> {
                       if (response.isSuccess()) {
                          final var eventLinkPatientSyncRsp = response.get();
                          return complete(StatusCodes.OK,
                                          new ExtendedLinkInfo(eventLinkPatientSyncRsp.stan(),
                                                               eventLinkPatientSyncRsp.linkInfo(),
                                                               eventLinkPatientSyncRsp.externalLinkCandidateList()),
                                          Jackson.marshaller());
                       } else {
                          return complete(StatusCodes.IM_A_TEAPOT);
                       }
                    }));
   }

   private CompletionStage<BackEnd.EventLinkPatientToGidSyncRsp> postLinkPatientToGid(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final LinkPatientToGidSyncBody body) {
      CompletionStage<BackEnd.EventLinkPatientToGidSyncRsp> stage = AskPattern.ask(backEnd,
                                                                                   replyTo -> new BackEnd.EventLinkPatientToGidSyncReq(
                                                                                         body,
                                                                                         replyTo),
                                                                                   java.time.Duration.ofSeconds(11),
                                                                                   actorSystem.scheduler());
      return stage.thenApply(response -> response);
   }

   private Route routeLinkPatientToGid(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      return entity(Jackson.unmarshaller(LinkPatientToGidSyncBody.class),
                    obj -> onComplete(postLinkPatientToGid(actorSystem, backEnd, obj),
                                      response -> response.isSuccess()
                                            ? complete(StatusCodes.OK, response.get(), Jackson.marshaller())
                                            : complete(StatusCodes.IM_A_TEAPOT)));
   }

   private CompletionStage<BackEnd.EventGetMURsp> getMU(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      CompletionStage<BackEnd.EventGetMURsp> stage =
            AskPattern.ask(backEnd, BackEnd.EventGetMUReq::new, java.time.Duration.ofSeconds(11), actorSystem.scheduler());
      return stage.thenApply(response -> response);
   }

   private Route routeMU(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      return onComplete(getMU(actorSystem, backEnd),
                        response -> response.isSuccess()
                              ? complete(StatusCodes.OK, response.get().mu(), Jackson.marshaller())
                              : complete(StatusCodes.IM_A_TEAPOT));
   }

   private CompletionStage<BackEnd.EventCalculateScoresRsp> postCalculateScores(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final CalculateScoresRequest body) {
      CompletionStage<BackEnd.EventCalculateScoresRsp> stage = AskPattern.ask(
            backEnd,
            replyTo -> new BackEnd.EventCalculateScoresReq(body, replyTo),
            java.time.Duration.ofSeconds(11),
            actorSystem.scheduler());
      return stage.thenApply(response -> response);
   }

   private Route routeCalculateScores(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      return entity(Jackson.unmarshaller(CalculateScoresRequest.class),
                    obj -> onComplete(postCalculateScores(actorSystem, backEnd, obj),
                                      response -> response.isSuccess()
                                            ? complete(StatusCodes.OK, response.get(), Jackson.marshaller())
                                            : complete(StatusCodes.IM_A_TEAPOT)));
   }

   private Route createRoute(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      return pathPrefix("JeMPI",
                        () -> concat(
                              post(() -> concat(
                                    path("link_patient", () -> routeLinkPatient(actorSystem, backEnd)),
                                    path("link_patient_to_gid", () -> routeLinkPatientToGid(actorSystem, backEnd)),
                                    path("calculate-scores", () -> routeCalculateScores(actorSystem, backEnd)))),
                              get(() -> concat(
                                    path("mu", () -> routeMU(actorSystem, backEnd))))));
   }

}
