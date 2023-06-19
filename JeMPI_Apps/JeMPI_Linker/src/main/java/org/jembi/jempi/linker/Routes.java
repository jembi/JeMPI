package org.jembi.jempi.linker;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.Route;
import org.jembi.jempi.libmpi.MpiGeneralError;
import org.jembi.jempi.libmpi.MpiServiceError;
import org.jembi.jempi.linker.backend.BackEnd;
import org.jembi.jempi.shared.models.*;

import static akka.http.javadsl.server.Directives.*;

final class Routes {

   private Routes() {
   }

   static Route mapError(final MpiGeneralError obj) {
      return switch (obj) {
         case MpiServiceError.InteractionIdDoesNotExistError e -> complete(StatusCodes.BAD_REQUEST, e, Jackson.marshaller());
         case MpiServiceError.GoldenIdDoesNotExistError e -> complete(StatusCodes.BAD_REQUEST, e, Jackson.marshaller());
         case MpiServiceError.GoldenIdInteractionConflictError e -> complete(StatusCodes.BAD_REQUEST, e, Jackson.marshaller());
         case MpiServiceError.DeletePredicateError e -> complete(StatusCodes.BAD_REQUEST, e, Jackson.marshaller());
         default -> complete(StatusCodes.INTERNAL_SERVER_ERROR);
      };
   }

   static Route proxyGetCandidatesWithScore(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Request> backEnd) {
      return parameter("iid",
                       patientId -> entity(Jackson.unmarshaller(CustomMU.class),
                                           mu -> onComplete(Ask.findCandidates(actorSystem, backEnd, patientId),
                                                            result -> result.isSuccess()
                                                                  ? result.get()
                                                                          .candidates()
                                                                          .mapLeft(Routes::mapError)
                                                                          .fold(error -> error,
                                                                                candidateList -> complete(StatusCodes.OK,
                                                                                                          candidateList,
                                                                                                          Jackson.marshaller()))
                                                                  : complete(StatusCodes.IM_A_TEAPOT))));
   }

   static Route proxyPostLinkInteraction(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Request> backEnd) {
      return entity(Jackson.unmarshaller(LinkInteractionSyncBody.class),
                    obj -> onComplete(Ask.postLinkInteraction(actorSystem, backEnd, obj), response -> {
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

   static Route proxyPostLinkInteractionToGID(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Request> backEnd) {
      return entity(Jackson.unmarshaller(LinkInteractionToGidSyncBody.class),
                    obj -> onComplete(Ask.postLinkPatientToGid(actorSystem, backEnd, obj),
                                      response -> response.isSuccess()
                                            ? complete(StatusCodes.OK, response.get(), Jackson.marshaller())
                                            : complete(StatusCodes.IM_A_TEAPOT)));
   }

   static Route proxyPostCalculateScores(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Request> backEnd) {
      return entity(Jackson.unmarshaller(ApiModels.ApiCalculateScoresRequest.class),
                    obj -> onComplete(Ask.postCalculateScores(actorSystem, backEnd, obj),
                                      response -> response.isSuccess()
                                            ? complete(StatusCodes.OK, response.get(), Jackson.marshaller())
                                            : complete(StatusCodes.IM_A_TEAPOT)));
   }

   static Route routeMU(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Request> backEnd) {
      return onComplete(Ask.getMU(actorSystem, backEnd),
                        response -> response.isSuccess()
                              ? complete(StatusCodes.OK, response.get().mu(), Jackson.marshaller())
                              : complete(StatusCodes.IM_A_TEAPOT));
   }

}
