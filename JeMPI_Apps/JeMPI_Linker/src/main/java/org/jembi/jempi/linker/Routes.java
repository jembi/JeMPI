package org.jembi.jempi.linker;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.Route;
import org.jembi.jempi.libmpi.MpiGeneralError;
import org.jembi.jempi.libmpi.MpiServiceError;
import org.jembi.jempi.linker.backend.BackEnd;
import org.jembi.jempi.shared.libs.m_and_u.MuModel;
import org.jembi.jempi.shared.models.ApiModels;
import org.jembi.jempi.shared.models.CustomMU;
import org.jembi.jempi.shared.models.LinkInteractionSyncBody;
import org.jembi.jempi.shared.models.LinkInteractionToGidSyncBody;

import static akka.http.javadsl.server.Directives.*;
import static org.jembi.jempi.shared.utils.AppUtils.OBJECT_MAPPER;

final class Routes {

   private Routes() {
   }

   static Route mapError(final MpiGeneralError obj) {
      return switch (obj) {
         case MpiServiceError.InteractionIdDoesNotExistError e -> complete(StatusCodes.BAD_REQUEST, e, Jackson.marshaller());
         case MpiServiceError.GoldenIdDoesNotExistError e -> complete(StatusCodes.BAD_REQUEST, e, Jackson.marshaller());
         case MpiServiceError.GoldenIdInteractionConflictError e -> complete(StatusCodes.BAD_REQUEST, e, Jackson.marshaller());
         case MpiServiceError.DeletePredicateError e -> complete(StatusCodes.BAD_REQUEST, e, Jackson.marshaller());
         case MpiServiceError.NotImplementedError e -> complete(StatusCodes.NOT_IMPLEMENTED, e, Jackson.marshaller());
         case MpiServiceError.CRClientExistsError e -> complete(StatusCodes.CONFLICT, e, Jackson.marshaller());
         case MpiServiceError.CRUpdateFieldError e -> complete(StatusCodes.BAD_REQUEST, e, Jackson.marshaller());
         case MpiServiceError.CRMissingFieldError e -> complete(StatusCodes.BAD_REQUEST, e, Jackson.marshaller());
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
                                          new ApiModels.ApiExtendedLinkInfo(eventLinkPatientSyncRsp.stan(),
                                                                            eventLinkPatientSyncRsp.linkInfo(),
                                                                            eventLinkPatientSyncRsp.externalLinkCandidateList()),
                                          Jackson.marshaller());
                       } else {
                          return complete(StatusCodes.IM_A_TEAPOT);
                       }
                    }));
   }

    static Route updateMandUOnNotificationResolution(
            final ActorSystem<Void> actorSystem,
            final ActorRef<BackEnd.Request> backEnd) {
        return entity(Jackson.unmarshaller(MuModel.MuNotificationResolutionDetails.class),
                obj -> onComplete(Ask.updateMandUOnNotificationResolution(actorSystem, backEnd, obj), response -> {
                    if (response.isSuccess() && Boolean.TRUE.equals(response.get().updated())) {
                        return complete(StatusCodes.OK);
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

//   static Route routeMU(
//         final ActorSystem<Void> actorSystem,
//         final ActorRef<BackEnd.Request> backEnd) {
//      return onComplete(Ask.getMU(actorSystem, backEnd),
//                        response -> response.isSuccess()
//                              ? complete(StatusCodes.OK, response.get().mu(), Jackson.marshaller())
//                              : complete(StatusCodes.IM_A_TEAPOT));
//   }

   static Route proxyGetCrCandidates(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Request> backEnd) {
      return entity(Jackson.unmarshaller(OBJECT_MAPPER, ApiModels.ApiCrCandidatesRequest.class),
                    obj -> onComplete(Ask.getCrCandidates(actorSystem, backEnd, obj), response -> {
                       if (response.isSuccess()) {
                          final var rsp = response.get();
                          if (rsp.goldenRecords().isLeft()) {
                             return mapError(rsp.goldenRecords().getLeft());
                          }
                          return complete(StatusCodes.OK,
                                          new ApiModels.ApiCrCandidatesResponse(rsp.goldenRecords().get()),
                                          Jackson.marshaller(OBJECT_MAPPER));
                       } else {
                          return complete(StatusCodes.IM_A_TEAPOT);
                       }
                    }));
   }

   static Route proxyGetCrFind(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Request> backEnd) {
      return entity(Jackson.unmarshaller(OBJECT_MAPPER, ApiModels.ApiCrFindRequest.class),
                    obj -> onComplete(Ask.getCrFind(actorSystem, backEnd, obj), response -> {
                       if (response.isSuccess()) {
                          final var rsp = response.get();
                          if (rsp.goldenRecords().isLeft()) {
                             return mapError(rsp.goldenRecords().getLeft());
                          }
                          return complete(StatusCodes.OK,
                                          new ApiModels.ApiCrCandidatesResponse(rsp.goldenRecords().get()),
                                          Jackson.marshaller(OBJECT_MAPPER));
                       } else {
                          return complete(StatusCodes.IM_A_TEAPOT);
                       }
                    }));
   }


   static Route proxyPostCrRegister(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Request> backEnd) {
      return entity(Jackson.unmarshaller(OBJECT_MAPPER, ApiModels.ApiCrRegisterRequest.class),
                    obj -> onComplete(Ask.postCrRegister(actorSystem, backEnd, obj), response -> {
                       if (response.isSuccess()) {
                          final var rsp = response.get();
                          if (rsp.linkInfo().isLeft()) {
                             return mapError(rsp.linkInfo().getLeft());
                          } else {
                             return complete(StatusCodes.OK,
                                             new ApiModels.ApiCrRegisterResponse(rsp.linkInfo().get()),
                                             Jackson.marshaller(OBJECT_MAPPER));
                          }
                       } else {
                          return complete(StatusCodes.IM_A_TEAPOT);
                       }
                    }));
   }

   static Route proxyPatchCrUpdateField(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Request> backEnd) {
      return entity(Jackson.unmarshaller(ApiModels.ApiCrUpdateFieldsRequest.class),
                    obj -> onComplete(Ask.patchCrUpdateField(actorSystem, backEnd, obj), response -> {
                       if (response.isSuccess()) {
                          final var rsp = response.get();
                          if (rsp.response().isLeft()) {
                             return mapError(rsp.response().getLeft());
                          } else {
                             final var r = rsp.response().get();
                             return complete(StatusCodes.OK,
                                             new ApiModels.ApiCrUpdateFieldsResponse(r.goldenId(), r.updated(), r.failed()),
                                             Jackson.marshaller());
                          }
                       } else {
                          return complete(StatusCodes.IM_A_TEAPOT);
                       }
                    }));
   }


}
