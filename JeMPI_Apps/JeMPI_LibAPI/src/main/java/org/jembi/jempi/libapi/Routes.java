package org.jembi.jempi.libapi;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.http.javadsl.Http;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.marshalling.Marshaller;
import akka.http.javadsl.model.*;
import akka.http.javadsl.server.Route;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.libmpi.MpiGeneralError;
import org.jembi.jempi.libmpi.MpiServiceError;
import org.jembi.jempi.shared.models.*;

import java.io.File;
import java.sql.Timestamp;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static akka.http.javadsl.server.Directives.*;
import static akka.http.javadsl.server.PathMatchers.segment;
import static org.jembi.jempi.shared.utils.AppUtils.OBJECT_MAPPER;

public final class Routes {

   private static final Logger LOGGER = LogManager.getLogger(Routes.class);
   private static final Marshaller<Object, RequestEntity> JSON_MARSHALLER = Jackson.marshaller(OBJECT_MAPPER);
   private static final Function<Map.Entry<String, String>, String> PARAM_STRING = Map.Entry::getValue;

   private Routes() {
   }

   static Route mapError(final MpiGeneralError obj) {
      return switch (obj) {
         case MpiServiceError.InteractionIdDoesNotExistError e -> complete(StatusCodes.BAD_REQUEST, e, JSON_MARSHALLER);
         case MpiServiceError.GoldenIdDoesNotExistError e -> complete(StatusCodes.BAD_REQUEST, e, JSON_MARSHALLER);
         case MpiServiceError.GoldenIdInteractionConflictError e -> complete(StatusCodes.BAD_REQUEST, e, JSON_MARSHALLER);
         case MpiServiceError.DeletePredicateError e -> complete(StatusCodes.BAD_REQUEST, e, JSON_MARSHALLER);
         case MpiServiceError.InvalidFunctionError e -> complete(StatusCodes.UNPROCESSABLE_ENTITY, e, JSON_MARSHALLER);
         case MpiServiceError.InvalidOperatorError e -> complete(StatusCodes.UNPROCESSABLE_ENTITY, e, JSON_MARSHALLER);
         case MpiServiceError.NoScoreGivenError e -> complete(StatusCodes.PARTIAL_CONTENT, e, JSON_MARSHALLER);
         case MpiServiceError.NotImplementedError e -> complete(StatusCodes.NOT_IMPLEMENTED, e, JSON_MARSHALLER);
         case MpiServiceError.CRClientExistsError e -> complete(StatusCodes.CONFLICT, e, JSON_MARSHALLER);
         case MpiServiceError.CRUpdateFieldError e -> complete(StatusCodes.BAD_REQUEST, e, JSON_MARSHALLER);
         case MpiServiceError.CRGidDoesNotExistError e -> complete(StatusCodes.NOT_FOUND, e, JSON_MARSHALLER);
         case MpiServiceError.CRLinkUpdateError e -> complete(StatusCodes.BAD_REQUEST, e, JSON_MARSHALLER);
         case MpiServiceError.CRMissingFieldError e -> complete(StatusCodes.BAD_REQUEST, e, JSON_MARSHALLER);
         case MpiServiceError.GeneralError e -> complete(StatusCodes.INTERNAL_SERVER_ERROR, e, JSON_MARSHALLER);
         case MpiServiceError.InternalError e -> complete(StatusCodes.INTERNAL_SERVER_ERROR, e, JSON_MARSHALLER);
         default -> complete(StatusCodes.INTERNAL_SERVER_ERROR);
      };
   }


   private static Route postIidNewGidLink(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final String controllerIp,
         final Integer controllerPort,
         final Http http) {

      return entity(Jackson.unmarshaller(NotificationResolution.class),
                    obj -> onComplete(
                          Ask.postIidNewGidLink(actorSystem, backEnd, obj.currentGoldenId(), obj.interactionId()),
                          result -> {
                             if (!result.isSuccess()) {
                                final var e = result.failed().get();
                                LOGGER.error(e.getLocalizedMessage(), e);
                                return complete(ApiModels.getHttpErrorResponse(StatusCodes.IM_A_TEAPOT));
                             }
                             return result.get()
                                          .linkInfo()
                                          .mapLeft(Routes::mapError)
                                          .fold(error -> error,
                                                linkInfo -> onComplete(
                                                      processOnNotificationResolution(
                                                            controllerIp, controllerPort, http,
                                                            new NotificationResolutionProcessorData(obj, linkInfo)),
                                                      r -> complete(StatusCodes.OK, linkInfo, JSON_MARSHALLER))
                                               );
                          })
                   );
   }

   private static Route postIidGidLink(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final String controllerIp,
         final Integer controllerPort,
         final Http http) {

      return entity(Jackson.unmarshaller(NotificationResolution.class),
                    obj -> onComplete(
                          Ask.postIidGidLink(actorSystem, backEnd, obj.currentGoldenId(), obj.newGoldenId(),
                                             obj.interactionId(), obj.score()),
                          result -> {
                             if (!result.isSuccess()) {
                                final var e = result.failed().get();
                                LOGGER.error(e.getLocalizedMessage(), e);
                                return complete(ApiModels.getHttpErrorResponse(StatusCodes.IM_A_TEAPOT));
                             }
                             return result.get()
                                          .linkInfo()
                                          .mapLeft(Routes::mapError)
                                          .fold(error -> error,
                                                linkInfo -> onComplete(
                                                      processOnNotificationResolution(
                                                            controllerIp, controllerPort, http,
                                                            new NotificationResolutionProcessorData(obj, linkInfo)),
                                                      r -> complete(StatusCodes.OK, linkInfo, JSON_MARSHALLER))
                                               );
                          })
                   );
   }


   private static Route patchGoldenRecord(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final String goldenId) {
      return entity(Jackson.unmarshaller(GoldenRecordUpdateRequestPayload.class),
                    payload -> payload != null
                          ? onComplete(Ask.patchGoldenRecord(actorSystem, backEnd, goldenId, payload),
                                       result -> {
                                          if (result.isSuccess()) {
                                             final var updatedFields = result.get().fields();
                                             if (updatedFields.isEmpty()) {
                                                return complete(StatusCodes.BAD_REQUEST);
                                             } else {
                                                return complete(StatusCodes.OK, result.get(), JSON_MARSHALLER);
                                             }
                                          } else {
                                             return complete(StatusCodes.INTERNAL_SERVER_ERROR);
                                          }
                                       })
                          : complete(StatusCodes.NO_CONTENT));
   }

   private static Route countRecords(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      return onComplete(Ask.countRecords(actorSystem, backEnd),
                        result -> {
                           if (!result.isSuccess()) {
                              final var e = result.failed().get();
                              LOGGER.error(e.getLocalizedMessage(), e);
                              return mapError(new MpiServiceError.InternalError(e.getLocalizedMessage()));
                           }
                           return complete(StatusCodes.OK,
                                           new ApiModels.ApiNumberOfRecords(result.get().goldenRecords(),
                                                                            result.get().patientRecords()),
                                           JSON_MARSHALLER);
                        });
   }

   private static Route getGidsPaged(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      return parameter("offset",
                       offset -> parameter("length",
                                           length -> onComplete(Ask.getGidsPaged(actorSystem, backEnd,
                                                                                 Long.parseLong(offset),
                                                                                 Long.parseLong(length)),
                                                                result -> {
                                                                   if (!result.isSuccess()) {
                                                                      final var e = result.failed().get();
                                                                      LOGGER.error(e.getLocalizedMessage(), e);
                                                                      return mapError(new MpiServiceError.InternalError(e.getLocalizedMessage()));
                                                                   }
                                                                   return complete(StatusCodes.OK, result.get(), JSON_MARSHALLER);
                                                                })));
   }

   private static Route getGoldenRecordAuditTrail(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      return parameter("gid",
                       uid -> onComplete(Ask.getGoldenRecordAuditTrail(actorSystem, backEnd, uid),
                                         result -> result.isSuccess()
                                               ? complete(StatusCodes.OK, result.get().auditTrail(), JSON_MARSHALLER)
                                               : complete(ApiModels.getHttpErrorResponse(StatusCodes.IM_A_TEAPOT))));
   }

   private static Route getInteractionAuditTrail(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      return parameter("iid",
                       uid -> onComplete(Ask.getInteractionAuditTrail(actorSystem, backEnd, uid),
                                         result -> result.isSuccess()
                                               ? complete(StatusCodes.OK, result.get().auditTrail(), JSON_MARSHALLER)
                                               : complete(ApiModels.getHttpErrorResponse(StatusCodes.IM_A_TEAPOT))));
   }

   private static Route countGoldenRecords(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      return onComplete(Ask.countGoldenRecords(actorSystem, backEnd),
                        result -> {
                           if (!result.isSuccess()) {
                              final var e = result.failed().get();
                              LOGGER.error(e.getLocalizedMessage(), e);
                              return mapError(new MpiServiceError.InternalError(e.getLocalizedMessage()));
                           }
                           return result.get()
                                        .count()
                                        .mapLeft(Routes::mapError)
                                        .fold(error -> error,
                                              count -> complete(StatusCodes.OK,
                                                                new ApiModels.ApiGoldenRecordCount(count),
                                                                JSON_MARSHALLER));
                        });
   }

   private static Route countInteractions(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      return onComplete(Ask.countInteractions(actorSystem, backEnd),
                        result -> {
                           if (!result.isSuccess()) {
                              final var e = result.failed().get();
                              LOGGER.error(e.getLocalizedMessage(), e);
                              return mapError(new MpiServiceError.InternalError(e.getLocalizedMessage()));
                           }
                           return result.get()
                                        .count()
                                        .mapLeft(Routes::mapError)
                                        .fold(error -> error,
                                              count -> complete(StatusCodes.OK,
                                                                new ApiModels.ApiInteractionCount(count),
                                                                JSON_MARSHALLER));
                        });
   }

   private static Route getGidsAll(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      return onComplete(Ask.getGidsAll(actorSystem, backEnd),
                        result -> {
                           if (!result.isSuccess()) {
                              final var e = result.failed().get();
                              LOGGER.error(e.getLocalizedMessage(), e);
                              return mapError(new MpiServiceError.InternalError(e.getLocalizedMessage()));
                           }
                           return complete(StatusCodes.OK, result.get(), JSON_MARSHALLER);
                        });
   }

   private static Route postCrFindSourceId(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      return parameter("facility",
                       facility -> parameter("client",
                                             client -> onComplete(Ask.findExpandedSourceId(actorSystem,
                                                                                           backEnd,
                                                                                           facility,
                                                                                           client),
                                                                  result -> {
                                                                     if (!result.isSuccess()) {
                                                                        final var e = result.failed().get();
                                                                        LOGGER.error(e.getLocalizedMessage(), e);
                                                                        return mapError(new MpiServiceError.InternalError(e.getLocalizedMessage()));
                                                                     }
                                                                     return complete(StatusCodes.OK,
                                                                                     result.get(),
                                                                                     JSON_MARSHALLER);
                                                                  })));
   }

   private static Route getNotifications(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      return
            parameter("limit", limit ->
                  parameter("offset", offset ->
                        parameter("startDate", startDate ->
                              parameter("endDate", endDate ->
                                    parameter("states", states ->
                                          onComplete(Ask.getNotifications(actorSystem,
                                                                          backEnd,
                                                                          Integer.parseInt(limit),
                                                                          Integer.parseInt(offset),
                                                                          Timestamp.valueOf(startDate),
                                                                          Timestamp.valueOf(endDate),
                                                                          Stream.of(states.split(","))
                                                                                .map(String::trim)
                                                                                .toList()),
                                                     result -> {
                                                        if (!result.isSuccess()) {
                                                           final var e = result.failed().get();
                                                           LOGGER.error(e.getLocalizedMessage(), e);
                                                           return mapError(new MpiServiceError.InternalError(e.getLocalizedMessage()));
                                                        }
                                                        return complete(StatusCodes.OK, result.get(), JSON_MARSHALLER);
                                                     }))))));
   }

   private static Route getExpandedGoldenRecordsUsingParameterList(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      return parameterList(params -> {
         final var goldenIds = params.stream().map(PARAM_STRING).toList();
         return onComplete(Ask.getExpandedGoldenRecords(actorSystem, backEnd, goldenIds),
                           result -> {
                              if (!result.isSuccess()) {
                                 final var e = result.failed().get();
                                 LOGGER.error(e.getLocalizedMessage(), e);
                                 return mapError(new MpiServiceError.InternalError(e.getLocalizedMessage()));
                              }
                              return result.get()
                                           .expandedGoldenRecords()
                                           .mapLeft(Routes::mapError)
                                           .fold(error -> error,
                                                 expandedGoldenRecords -> complete(StatusCodes.OK,
                                                                                   expandedGoldenRecords.stream()
                                                                                                        .map(ApiModels.ApiExpandedGoldenRecord::fromExpandedGoldenRecord)
                                                                                                        .toList(),
                                                                                   JSON_MARSHALLER));
                           });
      });
   }

   private static Route getExpandedGoldenRecordsFromUsingCSV(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      return parameter("uidList", items -> {
         final var uidList = Stream.of(items.split(",")).map(String::trim).toList();
         return onComplete(Ask.getExpandedGoldenRecords(actorSystem, backEnd, uidList),
                           result -> {
                              if (!result.isSuccess()) {
                                 final var e = result.failed().get();
                                 LOGGER.error(e.getLocalizedMessage(), e);
                                 return mapError(new MpiServiceError.InternalError(e.getLocalizedMessage()));
                              }
                              return result.get()
                                           .expandedGoldenRecords()
                                           .mapLeft(Routes::mapError)
                                           .fold(error -> error,
                                                 expandedGoldenRecords -> complete(StatusCodes.OK,
                                                                                   expandedGoldenRecords.stream()
                                                                                                        .map(ApiModels.ApiExpandedGoldenRecord::fromExpandedGoldenRecord)
                                                                                                        .toList(),
                                                                                   JSON_MARSHALLER));
                           });
      });
   }

   private static Route getExpandedInteractionsUsingCSV(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      return parameter("uidList", items -> {
         final var iidList = Stream.of(items.split(",")).map(String::trim).toList();
         return onComplete(Ask.getExpandedInteractions(actorSystem, backEnd, iidList),
                           result -> {
                              if (!result.isSuccess()) {
                                 final var e = result.failed().get();
                                 LOGGER.error(e.getLocalizedMessage(), e);
                                 return mapError(new MpiServiceError.InternalError(e.getLocalizedMessage()));
                              }
                              return result.get()
                                           .expandedPatientRecords()
                                           .mapLeft(Routes::mapError)
                                           .fold(error -> error,
                                                 expandedPatientRecords -> complete(StatusCodes.OK,
                                                                                    expandedPatientRecords.stream()
                                                                                                          .map(ApiModels.ApiExpandedInteraction::fromExpandedInteraction)
                                                                                                          .toList(),
                                                                                    JSON_MARSHALLER));
                           });
      });
   }

   private static Route getExpandedGoldenRecord(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final String gid) {
      return onComplete(Ask.getExpandedGoldenRecord(actorSystem, backEnd, gid),
                        result -> {
                           if (!result.isSuccess()) {
                              final var e = result.failed().get();
                              LOGGER.error(e.getLocalizedMessage(), e);
                              return mapError(new MpiServiceError.InternalError(e.getLocalizedMessage()));
                           }
                           return result.get()
                                        .goldenRecord()
                                        .mapLeft(Routes::mapError)
                                        .fold(error -> error,
                                              goldenRecord -> complete(StatusCodes.OK,
                                                                       ApiModels.ApiExpandedGoldenRecord
                                                                             .fromExpandedGoldenRecord(goldenRecord),
                                                                       Jackson.marshaller(OBJECT_MAPPER)));
                        });
   }

   private static Route getInteraction(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final String iid) {
      return onComplete(Ask.getInteraction(actorSystem, backEnd, iid),
                        result -> {
                           if (!result.isSuccess()) {
                              final var e = result.failed().get();
                              LOGGER.error(e.getLocalizedMessage(), e);
                              return mapError(new MpiServiceError.InternalError(e.getLocalizedMessage()));
                           }
                           return result.get()
                                        .patient()
                                        .mapLeft(Routes::mapError)
                                        .fold(error -> error,
                                              patientRecord -> complete(StatusCodes.OK,
                                                                        ApiModels.ApiInteraction.fromInteraction(patientRecord),
                                                                        JSON_MARSHALLER));
                        });
   }

   private static Route postUpdateNotification(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      return entity(Jackson.unmarshaller(NotificationRequest.class),
                    obj -> onComplete(Ask.postUpdateNotification(actorSystem, backEnd, obj), response -> {
                       if (!response.isSuccess()) {
                          final var e = response.failed().get();
                          LOGGER.error(e.getLocalizedMessage(), e);
                          return mapError(new MpiServiceError.InternalError(e.getLocalizedMessage()));
                       }
                       return complete(StatusCodes.OK, response.get(), JSON_MARSHALLER);
                    }));
   }

   private static Route postUploadCsvFile(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      return withSizeLimit(1024L * 1024 * 2048, () ->
            storeUploadedFile("csv",
                              info -> {
                                 try {
                                    return File.createTempFile("import-", ".csv");
                                 } catch (Exception e) {
                                    LOGGER.error(e.getMessage(), e);
                                    return null;
                                 }
                              },
                              (info, file) -> onComplete(Ask.postUploadCsvFile(
                                                               actorSystem,
                                                               backEnd,
                                                               info,
                                                               file),
                                                         response -> {
                                                            if (!response.isSuccess()) {
                                                               final var e = response.failed().get();
                                                               LOGGER.error(e.getLocalizedMessage(), e);
                                                               return mapError(new MpiServiceError.InternalError(e.getLocalizedMessage()));
                                                            }
                                                            return complete(StatusCodes.OK);
                                                         })));
   }

   private static Route postSimpleSearch(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final RecordType recordType) {
      LOGGER.info("Simple search on {}", recordType);
      return entity(Jackson.unmarshaller(ApiModels.ApiSimpleSearchRequestPayload.class), searchParameters -> onComplete(() -> {
         if (recordType == RecordType.GoldenRecord) {
            return Ask.postSimpleSearchGoldenRecords(actorSystem, backEnd, searchParameters);
         } else {
            return Ask.postSimpleSearchInteractions(actorSystem, backEnd, searchParameters);
         }
      }, response -> {
         if (!response.isSuccess()) {
            final var e = response.failed().get();
            LOGGER.error(e.getLocalizedMessage(), e);
            return mapError(new MpiServiceError.InternalError(e.getLocalizedMessage()));
         }
         return complete(StatusCodes.OK, response.get(), JSON_MARSHALLER);
      }));
   }

   private static Route postFilterGids(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      LOGGER.info("Filter Guids");
      return entity(Jackson.unmarshaller(OBJECT_MAPPER, FilterGidsRequestPayload.class),
                    searchParameters -> onComplete(() -> Ask.postFilterGids(actorSystem, backEnd, searchParameters), response -> {
                       if (!response.isSuccess()) {
                          final var e = response.failed().get();
                          LOGGER.error(e.getLocalizedMessage(), e);
                          return mapError(new MpiServiceError.InternalError(e.getLocalizedMessage()));
                       }
                       return complete(StatusCodes.OK, response.get(), JSON_MARSHALLER);
                    }));
   }

   private static Route postFilterGidsWithInteractionCount(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      LOGGER.info("Filter Guids");
      return entity(Jackson.unmarshaller(OBJECT_MAPPER, FilterGidsRequestPayload.class),
                    searchParameters -> onComplete(() -> Ask.postFilterGidsWithInteractionCount(actorSystem,
                                                                                                backEnd,
                                                                                                searchParameters), response -> {
                       if (!response.isSuccess()) {
                          final var e = response.failed().get();
                          LOGGER.error(e.getLocalizedMessage(), e);
                          return mapError(new MpiServiceError.InternalError(e.getLocalizedMessage()));
                       }
                       return complete(StatusCodes.OK, response.get(), JSON_MARSHALLER);
                    }));
   }

   private static Route postCustomSearch(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final RecordType recordType) {
      return entity(Jackson.unmarshaller(CustomSearchRequestPayload.class), searchParameters -> onComplete(() -> {
         if (recordType == RecordType.GoldenRecord) {
            return Ask.postCustomSearchGoldenRecords(actorSystem, backEnd, searchParameters);
         } else {
            return Ask.postCustomSearchInteractions(actorSystem, backEnd, searchParameters);
         }
      }, response -> {
         if (!response.isSuccess()) {
            final var e = response.failed().get();
            LOGGER.error(e.getLocalizedMessage(), e);
            return mapError(new MpiServiceError.InternalError(e.getLocalizedMessage()));
         }
         return complete(StatusCodes.OK, response.get(), JSON_MARSHALLER);

      }));
   }

   private static CompletionStage<Boolean> processOnNotificationResolution(
         final String linkerIP,
         final Integer linkerPort,
         final Http http,
         final NotificationResolutionProcessorData body) {
      try {
         final var request = HttpRequest
               .create(String.format(Locale.ROOT,
                                     "http://%s:%d/JeMPI/%s",
                                     linkerIP,
                                     linkerPort,
                                     GlobalConstants.SEGMENT_PROXY_ON_NOTIFICATION_RESOLUTION))
               .withMethod(HttpMethods.POST)
               .withEntity(ContentTypes.APPLICATION_JSON, OBJECT_MAPPER.writeValueAsBytes(body));
         final var stage = http.singleRequest(request);
         return stage.thenApply(response -> {
            if (response.status() != StatusCodes.OK) {
               LOGGER.error(String.format("An error occurred while processing the notification resolution. Notification id: %s",
                                          body.notificationResolution().notificationId()));
            }
            return true;
         });
      } catch (Exception e) {
         LOGGER.error(String.format("An error occurred while processing the notification resolution.  Notification id: %s",
                                    body.notificationResolution().notificationId()), e);
         return CompletableFuture.completedFuture(true);
      }


   }

/*
   private static CompletionStage<HttpResponse> postLinkInteractionToGidProxy(
         final String linkerIP,
         final Integer linkerPort,
         final Http http,
         final ApiModels.LinkInteractionToGidSyncBody body) throws JsonProcessingException {
      final HttpRequest request;
      final byte[] json;
      try {
         json = OBJECT_MAPPER.writeValueAsBytes(body);
      } catch (JsonProcessingException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
         throw e;
      }
      request = HttpRequest.create(String.format(Locale.ROOT,
                                                 "http://%s:%d/JeMPI/%s",
                                                 linkerIP,
                                                 linkerPort,
                                                 GlobalConstants.SEGMENT_PROXY_POST_LINK_INTERACTION_TO_GID))
                           .withMethod(HttpMethods.POST)
                           .withEntity(ContentTypes.APPLICATION_JSON, json);
      final var stage = http.singleRequest(request);
      return stage.thenApply(response -> response);
   }
*/

/*
   private static Route postLinkInteractionToGid(
         final String linkerIP,
         final Integer linkerPort,
         final Http http) {
      return entity(Jackson.unmarshaller(OBJECT_MAPPER, ApiModels.LinkInteractionToGidSyncBody.class),
                    obj -> {
                       try {
                          return onComplete(postLinkInteractionToGidProxy(linkerIP, linkerPort, http, obj),
                                            response -> {
                                               if (!response.isSuccess()) {
                                                  LOGGER.warn(IM_A_TEA_POT_LOG);
                                                  return complete(ApiModels.getHttpErrorResponse(GlobalConstants.IM_A_TEA_POT));
                                               }
                                               return complete(response.get());
                                            });
                       } catch (JsonProcessingException e) {
                          LOGGER.error(e.getLocalizedMessage(), e);
                          return complete(ApiModels.getHttpErrorResponse(StatusCodes.UNPROCESSABLE_ENTITY));
                       }
                    });
   }
*/

   public static Route createCoreAPIRoutes(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final String jsonFields,
         final String linkerIP,
         final Integer linkerPort,
         final String controllerIP,
         final Integer controllerPort,
         final Http http) {
      return concat(post(() -> concat(
                          /* proxy for linker/controller services*/
                          path(GlobalConstants.SEGMENT_PROXY_POST_CALCULATE_SCORES,
                               () -> ProxyRoutes.proxyPostCalculateScores(linkerIP, linkerPort, http)),
                          path(GlobalConstants.SEGMENT_PROXY_POST_LINK_INTERACTION,
                               () -> ProxyRoutes.proxyPostLinkInteraction(linkerIP, linkerPort, http)),
                          path(GlobalConstants.SEGMENT_PROXY_POST_CR_REGISTER,
                               () -> ProxyRoutes.proxyPostCrRegister(linkerIP, linkerPort, http)),
                          path(GlobalConstants.SEGMENT_PROXY_POST_CR_FIND,
                               () -> ProxyRoutes.proxyPostCrFind(linkerIP, linkerPort, http)),
                          path(GlobalConstants.SEGMENT_PROXY_POST_CR_CANDIDATES,
                               () -> ProxyRoutes.proxyPostCrCandidates(linkerIP, linkerPort, http)),
                          path(GlobalConstants.SEGMENT_PROXY_POST_CR_LINK_TO_GID_UPDATE,
                               () -> ProxyRoutes.proxyPostCrLinkToGidUpdate(linkerIP, linkerPort, http)),
                          path(GlobalConstants.SEGMENT_PROXY_POST_CR_LINK_BY_SOURCE_ID,
                               () -> ProxyRoutes.proxyPostCrLinkBySourceId(linkerIP, linkerPort, http)),
                          path(GlobalConstants.SEGMENT_PROXY_POST_CR_LINK_BY_SOURCE_ID_UPDATE,
                               () -> ProxyRoutes.proxyPostCrLinkBySourceIdUpdate(linkerIP, linkerPort, http)),
//                        path(GlobalConstants.SEGMENT_PROXY_POST_LINK_INTERACTION_TO_GID,
//                             () -> Routes.postLinkInteractionToGid(linkerIP, linkerPort, http)),

                          /* serviced by api */
                          path(GlobalConstants.SEGMENT_POST_IID_NEW_GID_LINK,
                               () -> Routes.postIidNewGidLink(actorSystem, backEnd, controllerIP, controllerPort, http)),
                          path(GlobalConstants.SEGMENT_POST_IID_GID_LINK,
                               () -> Routes.postIidGidLink(actorSystem, backEnd, controllerIP, controllerPort, http)),
                          path(GlobalConstants.SEGMENT_POST_FILTER_GIDS, () -> Routes.postFilterGids(actorSystem, backEnd)),
                          path(segment(GlobalConstants.SEGMENT_POST_SIMPLE_SEARCH)
                                     .slash(segment(Pattern.compile("^(golden|patient)$"))),
                               type -> Routes.postSimpleSearch(actorSystem, backEnd, type.equals("golden")
                                     ? RecordType.GoldenRecord
                                     : RecordType.Interaction)),
                          path(GlobalConstants.SEGMENT_POST_CR_FIND_SOURCE_ID,
                               () -> Routes.postCrFindSourceId(actorSystem, backEnd)),
                          path(GlobalConstants.SEGMENT_POST_UPDATE_NOTIFICATION,
                               () -> Routes.postUpdateNotification(actorSystem, backEnd)),
                          path(segment(GlobalConstants.SEGMENT_POST_CUSTOM_SEARCH)
                                     .slash(segment(Pattern.compile("^(golden|patient)$"))),
                               type -> Routes.postCustomSearch(actorSystem, backEnd, type.equals("golden")
                                     ? RecordType.GoldenRecord
                                     : RecordType.Interaction)),
                          path(GlobalConstants.SEGMENT_POST_UPLOAD_CSV_FILE,
                               () -> Routes.postUploadCsvFile(actorSystem, backEnd)),
                          path(GlobalConstants.SEGMENT_POST_FILTER_GIDS_WITH_INTERACTION_COUNT,
                               () -> Routes.postFilterGidsWithInteractionCount(actorSystem, backEnd)))),
                    patch(() -> concat(
                          /* proxy for linker/controller services*/
                          path(GlobalConstants.SEGMENT_PROXY_PATCH_CR_UPDATE_FIELDS,
                               () -> ProxyRoutes.proxyPatchCrUpdateFields(linkerIP, linkerPort, http)),
                          /* serviced by api */
                          path(segment(GlobalConstants.SEGMENT_PATCH_GOLDEN_RECORD).slash(segment(Pattern.compile("^[A-z0-9]+$"))),
                               gid -> Routes.patchGoldenRecord(actorSystem, backEnd, gid)))),
                    get(() -> concat(
                          /* proxy for linker/controller services*/
                          path(GlobalConstants.SEGMENT_PROXY_GET_DASHBOARD_DATA,
                               () -> ProxyRoutes.proxyGetDashboardData(actorSystem, backEnd, controllerIP, controllerPort, http)),
                          path(GlobalConstants.SEGMENT_PROXY_GET_CANDIDATES_WITH_SCORES,       // <------------------------ CHECK
                               () -> ProxyRoutes.proxyGetCandidatesWithScore(linkerIP, linkerPort, http)),

                          /* serviced by api */
                          path(GlobalConstants.SEGMENT_GET_FIELDS_CONFIG, () -> complete(StatusCodes.OK, jsonFields)),
                          path(GlobalConstants.SEGMENT_COUNT_INTERACTIONS, () -> Routes.countInteractions(actorSystem, backEnd)),
                          path(GlobalConstants.SEGMENT_COUNT_GOLDEN_RECORDS,
                               () -> Routes.countGoldenRecords(actorSystem, backEnd)),
                          path(GlobalConstants.SEGMENT_COUNT_RECORDS, () -> Routes.countRecords(actorSystem, backEnd)),
                          path(GlobalConstants.SEGMENT_GET_GIDS_ALL, () -> Routes.getGidsAll(actorSystem, backEnd)),
                          path(GlobalConstants.SEGMENT_GET_GIDS_PAGED, () -> Routes.getGidsPaged(actorSystem, backEnd)),
                          path(segment(GlobalConstants.SEGMENT_GET_EXPANDED_GOLDEN_RECORD)
                                     .slash(segment(Pattern.compile("^[A-z0-9]+$"))),
                               gid -> Routes.getExpandedGoldenRecord(actorSystem, backEnd, gid)),
                          path(GlobalConstants.SEGMENT_GET_EXPANDED_GOLDEN_RECORDS_USING_PARAMETER_LIST,
                               () -> Routes.getExpandedGoldenRecordsUsingParameterList(actorSystem, backEnd)),
                          path(GlobalConstants.SEGMENT_GET_EXPANDED_GOLDEN_RECORDS_USING_CSV,
                               () -> Routes.getExpandedGoldenRecordsFromUsingCSV(actorSystem, backEnd)),
                          path(GlobalConstants.SEGMENT_GET_GOLDEN_RECORD_AUDIT_TRAIL,
                               () -> Routes.getGoldenRecordAuditTrail(actorSystem, backEnd)),
                          path(segment(GlobalConstants.SEGMENT_GET_INTERACTION).slash(segment(Pattern.compile(
                                "^[A-z0-9]+$"))), iid -> Routes.getInteraction(actorSystem, backEnd, iid)),
                          path(GlobalConstants.SEGMENT_GET_EXPANDED_INTERACTIONS_USING_CSV,
                               () -> Routes.getExpandedInteractionsUsingCSV(actorSystem, backEnd)),
                          path(GlobalConstants.SEGMENT_GET_INTERACTION_AUDIT_TRAIL,
                               () -> Routes.getInteractionAuditTrail(actorSystem, backEnd)),
                          path(GlobalConstants.SEGMENT_GET_NOTIFICATIONS, () -> Routes.getNotifications(actorSystem, backEnd)))));
   }

}
