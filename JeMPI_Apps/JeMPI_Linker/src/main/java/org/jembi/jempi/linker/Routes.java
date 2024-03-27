package org.jembi.jempi.linker;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.marshalling.Marshaller;
import akka.http.javadsl.model.RequestEntity;
import akka.http.javadsl.model.StatusCode;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.Route;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.libmpi.MpiGeneralError;
import org.jembi.jempi.libmpi.MpiServiceError;
import org.jembi.jempi.linker.backend.BackEnd;
import org.jembi.jempi.shared.models.ApiModels;
import org.jembi.jempi.shared.models.CustomMU;
import org.jembi.jempi.shared.models.GlobalConstants;

import static akka.http.javadsl.server.Directives.*;
import static org.jembi.jempi.shared.models.GlobalConstants.IM_A_TEA_POT_LOG;
import static org.jembi.jempi.shared.utils.AppUtils.OBJECT_MAPPER;

final class Routes {

   private static final Logger LOGGER = LogManager.getLogger(Routes.class);

   private static final Marshaller<Object, RequestEntity> JSON_MARSHALLER = Jackson.marshaller(OBJECT_MAPPER);

   private Routes() {
   }

   static StatusCode logHttpError(
         final StatusCode code,
         final String log) {
      LOGGER.debug("{}", log);
      return code;
   }

   static Route mapError(final MpiGeneralError obj) {
      return switch (obj) {
         case MpiServiceError.InteractionIdDoesNotExistError e -> complete(StatusCodes.BAD_REQUEST, e, JSON_MARSHALLER);
         case MpiServiceError.GoldenIdDoesNotExistError e -> complete(StatusCodes.BAD_REQUEST, e, JSON_MARSHALLER);
         case MpiServiceError.GoldenIdInteractionConflictError e -> complete(StatusCodes.BAD_REQUEST, e, JSON_MARSHALLER);
         case MpiServiceError.DeletePredicateError e -> complete(StatusCodes.BAD_REQUEST, e, JSON_MARSHALLER);
         case MpiServiceError.NotImplementedError e -> complete(StatusCodes.NOT_IMPLEMENTED, e, JSON_MARSHALLER);
         case MpiServiceError.CRClientExistsError e -> complete(StatusCodes.CONFLICT, e, JSON_MARSHALLER);
         case MpiServiceError.CRUpdateFieldError e -> complete(StatusCodes.BAD_REQUEST, e, JSON_MARSHALLER);
         case MpiServiceError.CRGidDoesNotExistError e -> complete(StatusCodes.NOT_FOUND, e, JSON_MARSHALLER);
         case MpiServiceError.CRLinkUpdateError e -> complete(StatusCodes.BAD_REQUEST, e, JSON_MARSHALLER);
         case MpiServiceError.CRMissingFieldError e -> complete(StatusCodes.BAD_REQUEST, e, JSON_MARSHALLER);
         case MpiServiceError.InvalidFunctionError e -> complete(StatusCodes.UNPROCESSABLE_ENTITY, e, JSON_MARSHALLER);
         case MpiServiceError.InvalidOperatorError e -> complete(StatusCodes.UNPROCESSABLE_ENTITY, e, JSON_MARSHALLER);
         case MpiServiceError.GeneralError e -> complete(StatusCodes.INTERNAL_SERVER_ERROR, e, JSON_MARSHALLER);
         default -> complete(StatusCodes.INTERNAL_SERVER_ERROR);
      };
   }

   static Route proxyGetCandidatesWithScore(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Request> backEnd) {
      return parameter("iid",
                       patientId -> entity(Jackson.unmarshaller(CustomMU.class),
                                           mu -> onComplete(Ask.findCandidates(actorSystem, backEnd, patientId),
                                                            result -> {
                                                               if (!result.isSuccess()) {
                                                                  LOGGER.warn(IM_A_TEA_POT_LOG);
                                                                  return complete(ApiModels.getHttpErrorResponse(GlobalConstants.IM_A_TEA_POT));
                                                               }
                                                               return result.get()
                                                                            .candidates()
                                                                            .mapLeft(Routes::mapError)
                                                                            .fold(error -> error,
                                                                                  candidateList -> complete(StatusCodes.OK,
                                                                                                            candidateList,
                                                                                                            Jackson.marshaller()));
                                                            })));
   }

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

   static Route proxyPostCalculateScores(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Request> backEnd) {
      return entity(Jackson.unmarshaller(ApiModels.ApiCalculateScoresRequest.class),
                    obj -> onComplete(Ask.postCalculateScores(actorSystem, backEnd, obj),
                                      response -> {
                                         if (!response.isSuccess()) {
                                            LOGGER.warn(IM_A_TEA_POT_LOG);
                                            return complete(ApiModels.getHttpErrorResponse(GlobalConstants.IM_A_TEA_POT));
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
                          LOGGER.warn(IM_A_TEA_POT_LOG);
                          return complete(ApiModels.getHttpErrorResponse(GlobalConstants.IM_A_TEA_POT));
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
                          LOGGER.warn(IM_A_TEA_POT_LOG);
                          return complete(ApiModels.getHttpErrorResponse(GlobalConstants.IM_A_TEA_POT));
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
                          LOGGER.warn(IM_A_TEA_POT_LOG);
                          return complete(ApiModels.getHttpErrorResponse(GlobalConstants.IM_A_TEA_POT));
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

   static Route proxyPostCrLinkUpdate(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Request> backEnd) {
      return entity(Jackson.unmarshaller(OBJECT_MAPPER, ApiModels.ApiCrLinkUpdateRequest.class),
                    obj -> onComplete(Ask.postCrLinkUpdate(actorSystem, backEnd, obj), response -> {
                       if (!response.isSuccess()) {
                          LOGGER.warn(IM_A_TEA_POT_LOG);
                          return complete(ApiModels.getHttpErrorResponse(GlobalConstants.IM_A_TEA_POT));
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
                          LOGGER.warn(IM_A_TEA_POT_LOG);
                          return complete(ApiModels.getHttpErrorResponse(GlobalConstants.IM_A_TEA_POT));
                       }
                       final var eventLinkPatientSyncRsp = response.get();
                       return complete(StatusCodes.OK,
                                       new ApiModels.ApiExtendedLinkInfo(eventLinkPatientSyncRsp.stan(),
                                                                         eventLinkPatientSyncRsp.linkInfo(),
                                                                         eventLinkPatientSyncRsp.externalLinkCandidateList()),
                                       Jackson.marshaller());
                    }));
   }

   static Route proxyPatchCrUpdateField(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Request> backEnd) {
      return entity(Jackson.unmarshaller(ApiModels.ApiCrUpdateFieldsRequest.class),
                    obj -> onComplete(Ask.patchCrUpdateField(actorSystem, backEnd, obj), response -> {
                       if (!response.isSuccess()) {
                          LOGGER.warn(IM_A_TEA_POT_LOG);
                          return complete(ApiModels.getHttpErrorResponse(GlobalConstants.IM_A_TEA_POT));
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


}
