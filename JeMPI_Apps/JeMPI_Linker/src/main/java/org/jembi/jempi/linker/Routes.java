package org.jembi.jempi.linker;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.StatusCode;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.Route;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.libmpi.MpiServiceError;
import org.jembi.jempi.linker.backend.BackEnd;
import org.jembi.jempi.shared.models.ApiModels;
import org.jembi.jempi.shared.models.GlobalConstants;

import static akka.http.javadsl.server.Directives.*;
import static org.jembi.jempi.linker.MapError.mapError;
import static org.jembi.jempi.shared.utils.AppUtils.OBJECT_MAPPER;

final class Routes {

   private static final Logger LOGGER = LogManager.getLogger(Routes.class);

   private Routes() {
   }

   static StatusCode logHttpError(
         final StatusCode code,
         final String log) {
      LOGGER.debug("{}", log);
      return code;
   }

   static Route proxyPostCandidatesWithScore(
        final ActorSystem<Void> actorSystem,
        final ActorRef<BackEnd.Request> backEnd) {
    return entity(Jackson.unmarshaller(ApiModels.ApiInteractionUid.class), request -> {
        try {
            return onComplete(Ask.findCandidates(actorSystem, backEnd, request),
                    response -> {
                        if (!response.isSuccess()) {
                            final var e = response.failed().get();
                            LOGGER.error(e.getLocalizedMessage(), e);
                            return mapError(new MpiServiceError.InternalError(e.getLocalizedMessage()));
                        }
                        return response.get()
                                .candidates()
                                .mapLeft(MapError::mapError)
                                .fold(error -> error,
                                        candidateList -> complete(StatusCodes.OK,
                                                candidateList,
                                                Jackson.marshaller()));
                    });
        } catch (NumberFormatException e) {
            LOGGER.error("Invalid iid provided", e);
            return complete(StatusCodes.BAD_REQUEST, "Invalid iid provided");
        }
    });
}


/*
   static Route proxyPostLinkInteractionToGID(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Request> backEnd) {
      return entity(Jackson.unmarshaller(ApiModels.LinkInteractionToGidSyncBody.class),
                    obj -> onComplete(Ask.postLinkPatientToGid(actorSystem, backEnd, obj),
                                      response -> {
                                         if (!response.isSuccess()) {
                                            LOGGER.warn(IM_A_TEA_POT_LOG);
                                            return complete(ApiModels.getHttpErrorResponse(GlobalConstants.IM_A_TEA_POT));
                                         }
                                         return complete(StatusCodes.OK, response.get(), Jackson.marshaller());
                                      }));
   }
*/

   static Route proxyPostCalculateScores(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Request> backEnd) {
      return entity(Jackson.unmarshaller(ApiModels.ApiCalculateScoresRequest.class),
                    obj -> onComplete(Ask.postCalculateScores(actorSystem, backEnd, obj),
                                      response -> {
                                         if (!response.isSuccess()) {
                                            final var e = response.failed().get();
                                            LOGGER.error(e.getLocalizedMessage(), e);
                                            return mapError(new MpiServiceError.InternalError(e.getLocalizedMessage()));
                                         }
                                         return complete(StatusCodes.OK, response.get(), Jackson.marshaller());
                                      }));
   }

   static Route proxyGetCrCandidates(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Request> backEnd) {
      return entity(Jackson.unmarshaller(OBJECT_MAPPER, ApiModels.ApiCrCandidatesRequest.class),
                    obj -> onComplete(Ask.getCrCandidates(actorSystem, backEnd, obj), response -> {
                       if (!response.isSuccess()) {
                          final var e = response.failed().get();
                          LOGGER.error(e.getLocalizedMessage(), e);
                          return mapError(new MpiServiceError.InternalError(e.getLocalizedMessage()));
                       }
                       final var rsp = response.get();
                       if (rsp.goldenRecords().isLeft()) {
                          return mapError(rsp.goldenRecords().getLeft());
                       }
                       return complete(StatusCodes.OK,
                                       new ApiModels.ApiCrCandidatesResponse(rsp.goldenRecords().get()),
                                       Jackson.marshaller(OBJECT_MAPPER));
                    }));
   }

   static Route proxyGetCrFind(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Request> backEnd) {
      return entity(Jackson.unmarshaller(OBJECT_MAPPER, ApiModels.ApiCrFindRequest.class),
                    obj -> onComplete(Ask.getCrFind(actorSystem, backEnd, obj), response -> {
                       if (!response.isSuccess()) {
                          final var e = response.failed().get();
                          LOGGER.error(e.getLocalizedMessage(), e);
                          return mapError(new MpiServiceError.InternalError(e.getLocalizedMessage()));
                       }
                       final var rsp = response.get();
                       if (rsp.goldenRecords().isLeft()) {
                          return mapError(rsp.goldenRecords().getLeft());
                       }
                       return complete(StatusCodes.OK,
                                       new ApiModels.ApiCrCandidatesResponse(rsp.goldenRecords().get()),
                                       Jackson.marshaller(OBJECT_MAPPER));
                    }));
   }


   static Route proxyPostCrRegister(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Request> backEnd) {
      return entity(Jackson.unmarshaller(OBJECT_MAPPER, ApiModels.ApiCrRegisterRequest.class),
                    obj -> onComplete(Ask.postCrRegister(actorSystem, backEnd, obj), response -> {
                       if (!response.isSuccess()) {
                          final var e = response.failed().get();
                          LOGGER.error(e.getLocalizedMessage(), e);
                          return mapError(new MpiServiceError.InternalError(e.getLocalizedMessage()));
                       }
                       final var rsp = response.get();
                       if (rsp.linkInfo().isLeft()) {
                          return mapError(rsp.linkInfo().getLeft());
                       } else {
                          return complete(StatusCodes.OK,
                                          new ApiModels.ApiCrRegisterResponse(rsp.linkInfo().get()),
                                          Jackson.marshaller(OBJECT_MAPPER));
                       }
                    }));
   }

   static Route proxyPostCrLinkToGidUpdate(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Request> backEnd) {
      return entity(Jackson.unmarshaller(OBJECT_MAPPER, ApiModels.ApiCrLinkToGidUpdateRequest.class),
                    obj -> onComplete(Ask.postCrLinkToGidUpdate(actorSystem, backEnd, obj), response -> {
                       if (!response.isSuccess()) {
                          final var e = response.failed().get();
                          LOGGER.error(e.getLocalizedMessage(), e);
                          return mapError(new MpiServiceError.InternalError(e.getLocalizedMessage()));
                       }
                       final var rsp = response.get();
                       try {
                          if (rsp.linkInfo().isLeft()) {
                             LOGGER.warn("{}", OBJECT_MAPPER.writeValueAsString(rsp.linkInfo().getLeft()));
                          } else {
                             LOGGER.debug("{}", OBJECT_MAPPER.writeValueAsString(rsp.linkInfo().get()));
                          }
                       } catch (JsonProcessingException e) {
                          LOGGER.error(e.getLocalizedMessage(), e);
                       }
                       if (rsp.linkInfo().isLeft()) {
                          final var error = rsp.linkInfo().getLeft();
                          try {
                             LOGGER.warn("Error: {}", OBJECT_MAPPER.writeValueAsString(error));
                          } catch (JsonProcessingException e) {
                             LOGGER.error(e.getLocalizedMessage(), e);
                          }
                          return mapError(error);
                       } else {
                          final var result = rsp.linkInfo().get();
                          LOGGER.debug("OK: {}", result);
                          return complete(StatusCodes.OK,
                                          new ApiModels.ApiCrLinkUpdateResponse(result),
                                          Jackson.marshaller(OBJECT_MAPPER));
                       }
                    }));
   }

   static Route proxyPostCrLinkBySourceId(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Request> backEnd) {
      return entity(Jackson.unmarshaller(OBJECT_MAPPER, ApiModels.ApiCrLinkBySourceIdRequest.class),
                    obj -> onComplete(Ask.postCrLinkBySourceId(actorSystem, backEnd, obj), response -> {
                       if (!response.isSuccess()) {
                          final var e = response.failed().get();
                          LOGGER.error(e.getLocalizedMessage(), e);
                          return mapError(new MpiServiceError.InternalError(e.getLocalizedMessage()));
                       }
                       final var rsp = response.get();
                       try {
                          if (rsp.linkInfo().isLeft()) {
                             LOGGER.warn("{}", OBJECT_MAPPER.writeValueAsString(rsp.linkInfo().getLeft()));
                          } else {
                             LOGGER.debug("{}", OBJECT_MAPPER.writeValueAsString(rsp.linkInfo().get()));
                          }
                       } catch (JsonProcessingException e) {
                          LOGGER.error(e.getLocalizedMessage(), e);
                       }
                       if (rsp.linkInfo().isLeft()) {
                          final var error = rsp.linkInfo().getLeft();
                          try {
                             LOGGER.warn("Error: {}", OBJECT_MAPPER.writeValueAsString(error));
                          } catch (JsonProcessingException e) {
                             LOGGER.error(e.getLocalizedMessage(), e);
                          }
                          return mapError(error);
                       } else {
                          final var result = rsp.linkInfo().get();
                          LOGGER.debug("OK: {}", result);
                          return complete(StatusCodes.OK,
                                          new ApiModels.ApiCrLinkUpdateResponse(result),
                                          Jackson.marshaller(OBJECT_MAPPER));
                       }
                    }));
   }

   static Route proxyPostCrLinkBySourceIdUpdate(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Request> backEnd) {
      return entity(Jackson.unmarshaller(OBJECT_MAPPER, ApiModels.ApiCrLinkBySourceIdUpdateRequest.class),
                    obj -> onComplete(Ask.postCrLinkBySourceIdUpdate(actorSystem, backEnd, obj), response -> {
                       if (!response.isSuccess()) {
                          final var e = response.failed().get();
                          LOGGER.error(e.getLocalizedMessage(), e);
                          return mapError(new MpiServiceError.InternalError(e.getLocalizedMessage()));
                       }
                       final var rsp = response.get();
                       try {
                          if (rsp.linkInfo().isLeft()) {
                             LOGGER.warn("{}", OBJECT_MAPPER.writeValueAsString(rsp.linkInfo().getLeft()));
                          } else {
                             LOGGER.debug("{}", OBJECT_MAPPER.writeValueAsString(rsp.linkInfo().get()));
                          }
                       } catch (JsonProcessingException e) {
                          LOGGER.error(e.getLocalizedMessage(), e);
                       }
                       if (rsp.linkInfo().isLeft()) {
                          final var error = rsp.linkInfo().getLeft();
                          try {
                             LOGGER.warn("Error: {}", OBJECT_MAPPER.writeValueAsString(error));
                          } catch (JsonProcessingException e) {
                             LOGGER.error(e.getLocalizedMessage(), e);
                          }
                          return mapError(error);
                       } else {
                          final var result = rsp.linkInfo().get();
                          LOGGER.debug("OK: {}", result);
                          return complete(StatusCodes.OK,
                                          new ApiModels.ApiCrLinkUpdateResponse(result),
                                          Jackson.marshaller(OBJECT_MAPPER));
                       }
                    }));
   }

   static Route proxyPostLinkInteraction(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Request> backEnd) {
      return entity(Jackson.unmarshaller(OBJECT_MAPPER, ApiModels.LinkInteractionSyncBody.class),
                    obj -> onComplete(Ask.postLinkInteraction(actorSystem, backEnd, obj), response -> {
                       if (!response.isSuccess()) {
                          final var e = response.failed().get();
                          LOGGER.error(e.getLocalizedMessage(), e);
                          return mapError(new MpiServiceError.InternalError(e.getLocalizedMessage()));
                       }
                       final var eventLinkPatientSyncRsp = response.get();
                       return complete(StatusCodes.OK,
                                       new ApiModels.ApiExtendedLinkInfo(eventLinkPatientSyncRsp.stan(),
                                                                         eventLinkPatientSyncRsp.linkInfo(),
                                                                         eventLinkPatientSyncRsp.externalLinkCandidateList()),
                                       Jackson.marshaller());
                    }));
   }

   static Route proxyPostCrUpdateField(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Request> backEnd) {
      return entity(Jackson.unmarshaller(ApiModels.ApiCrUpdateFieldsRequest.class),
                    obj -> onComplete(Ask.patchCrUpdateField(actorSystem, backEnd, obj), response -> {
                       if (!response.isSuccess()) {
                          final var e = response.failed().get();
                          LOGGER.error(e.getLocalizedMessage(), e);
                          return mapError(new MpiServiceError.InternalError(e.getLocalizedMessage()));
                       }
                       final var rsp = response.get();
                       if (rsp.response().isLeft()) {
                          return mapError(rsp.response().getLeft());
                       } else {
                          final var r = rsp.response().get();
                          return complete(StatusCodes.OK,
                                          new ApiModels.ApiCrUpdateFieldsResponse(r.goldenId(), r.updated(), r.failed()),
                                          Jackson.marshaller());
                       }
                    }));
   }

   static Route createRoute(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Request> backEnd) {
      return pathPrefix("JeMPI",
                        () -> concat(post(() -> concat(path(GlobalConstants.SEGMENT_PROXY_POST_CR_LINK,
                                                            () -> proxyPostLinkInteraction(actorSystem, backEnd)),
//                                                     path(GlobalConstants.SEGMENT_PROXY_POST_LINK_INTERACTION_TO_GID,
//                                                            () -> proxyPostLinkInteractionToGID(actorSystem, backEnd)),
                                                       path(GlobalConstants.SEGMENT_PROXY_POST_SCORES,
                                                            () -> proxyPostCalculateScores(actorSystem, backEnd)),
                                                       path(GlobalConstants.SEGMENT_PROXY_POST_CR_CANDIDATES,
                                                            () -> proxyGetCrCandidates(actorSystem, backEnd)),
                                                       path(GlobalConstants.SEGMENT_PROXY_POST_CR_FIND,
                                                            () -> proxyGetCrFind(actorSystem, backEnd)),
                                                       path(GlobalConstants.SEGMENT_PROXY_POST_CR_REGISTER,
                                                            () -> proxyPostCrRegister(actorSystem, backEnd)),
                                                       path(GlobalConstants.SEGMENT_PROXY_POST_CR_LINK_TO_GID_UPDATE,
                                                            () -> proxyPostCrLinkToGidUpdate(actorSystem, backEnd)),
                                                       path(GlobalConstants.SEGMENT_PROXY_POST_CR_LINK_BY_SOURCE_ID,
                                                            () -> proxyPostCrLinkBySourceId(actorSystem, backEnd)),
                                                       path(GlobalConstants.SEGMENT_PROXY_POST_CR_LINK_BY_SOURCE_ID_UPDATE,
                                                            () -> proxyPostCrLinkBySourceIdUpdate(actorSystem, backEnd)))),
                                                       path(GlobalConstants.SEGMENT_PROXY_POST_CANDIDATE_GOLDEN_RECORDS,
                                                            () -> proxyPostCandidatesWithScore(actorSystem, backEnd)),
                                                       path(GlobalConstants.SEGMENT_PROXY_POST_CR_UPDATE_FIELDS,
                                                            () -> proxyPostCrUpdateField(actorSystem, backEnd))));
   }


}
