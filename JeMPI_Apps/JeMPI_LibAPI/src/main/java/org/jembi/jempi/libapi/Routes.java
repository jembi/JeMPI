package org.jembi.jempi.libapi;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.http.javadsl.Http;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.marshalling.Marshaller;
import akka.http.javadsl.model.*;
import akka.http.javadsl.server.Route;
import akka.japi.Pair;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.libmpi.MpiGeneralError;
import org.jembi.jempi.libmpi.MpiServiceError;
import org.jembi.jempi.shared.models.*;

import java.io.File;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.stream.Stream;

import static akka.http.javadsl.server.Directives.*;
import static org.jembi.jempi.shared.utils.AppUtils.OBJECT_MAPPER;

import java.util.regex.Pattern;
import static akka.http.javadsl.server.PathMatchers.segment;

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
         default -> complete(StatusCodes.INTERNAL_SERVER_ERROR);
      };
   }

   public static Route patchGoldenRecord(
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

   public static Route countRecords(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      return onComplete(Ask.countRecords(actorSystem, backEnd),
            result -> result.isSuccess()
                  ? complete(StatusCodes.OK,
                        new ApiModels.ApiNumberOfRecords(result.get().goldenRecords(),
                              result.get().patientRecords()),
                        JSON_MARSHALLER)
                  : complete(ApiModels.getHttpErrorResponse(StatusCodes.IM_A_TEAPOT)));
   }

   public static Route getGidsPaged(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      return parameter("offset",
            offset -> parameter("length",
                  length -> onComplete(Ask.getGidsPaged(actorSystem,
                        backEnd,
                        Long.parseLong(offset),
                        Long.parseLong(length)),
                        result -> result.isSuccess()
                              ? complete(StatusCodes.OK,
                                    result.get(),
                                    JSON_MARSHALLER)
                              : complete(ApiModels.getHttpErrorResponse(StatusCodes.IM_A_TEAPOT)))));
   }

   public static Route getGoldenRecordAuditTrail(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      return parameter("gid",
            uid -> onComplete(Ask.getGoldenRecordAuditTrail(actorSystem, backEnd, uid),
                  result -> result.isSuccess()
                        ? complete(StatusCodes.OK,
                              ApiModels.ApiAuditTrail.fromAuditTrail(result.get().auditTrail()),
                              JSON_MARSHALLER)
                        : complete(ApiModels.getHttpErrorResponse(StatusCodes.IM_A_TEAPOT))));
   }

   public static Route getInteractionAuditTrail(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      return parameter("iid",
            uid -> onComplete(Ask.getInteractionAuditTrail(actorSystem, backEnd, uid),
                  result -> result.isSuccess()
                        ? complete(StatusCodes.OK,
                              ApiModels.ApiAuditTrail.fromAuditTrail(result.get().auditTrail()),
                              JSON_MARSHALLER)
                        : complete(ApiModels.getHttpErrorResponse(StatusCodes.IM_A_TEAPOT))));
   }

   public static Route patchIidNewGidLink(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      return parameter("goldenID",
            currentGoldenId -> parameter("patientID",
                  patientId -> onComplete(Ask.patchIidNewGidLink(actorSystem,
                        backEnd,
                        currentGoldenId,
                        patientId),
                        result -> result.isSuccess()
                              ? result.get()
                                    .linkInfo()
                                    .mapLeft(Routes::mapError)
                                    .fold(error -> error,
                                          linkInfo -> complete(StatusCodes.OK,
                                                linkInfo,
                                                JSON_MARSHALLER))
                              : complete(ApiModels.getHttpErrorResponse(StatusCodes.IM_A_TEAPOT)))));
   }

   public static Route patchIidGidLink(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      return parameter("goldenID",
            currentGoldenId -> parameter("newGoldenID",
                  newGoldenId -> parameter("patientID",
                        patientId -> parameter("score",
                              score -> onComplete(
                                    Ask.patchIidGidLink(
                                          actorSystem,
                                          backEnd,
                                          currentGoldenId,
                                          newGoldenId,
                                          patientId,
                                          Float.parseFloat(score)),
                                    result -> result.isSuccess()
                                          ? result.get()
                                                .linkInfo()
                                                .mapLeft(Routes::mapError)
                                                .fold(error -> error,
                                                      linkInfo -> complete(
                                                            StatusCodes.OK,
                                                            linkInfo,
                                                            JSON_MARSHALLER))
                                          : complete(ApiModels.getHttpErrorResponse(StatusCodes.IM_A_TEAPOT)))))));
   }

   public static Route countGoldenRecords(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      return onComplete(Ask.countGoldenRecords(actorSystem, backEnd),
            result -> result.isSuccess()
                  ? result.get()
                        .count()
                        .mapLeft(Routes::mapError)
                        .fold(error -> error,
                              count -> complete(StatusCodes.OK,
                                    new ApiModels.ApiGoldenRecordCount(count),
                                    JSON_MARSHALLER))
                  : complete(ApiModels.getHttpErrorResponse(StatusCodes.IM_A_TEAPOT)));
   }

   public static Route countInteractions(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      return onComplete(Ask.countInteractions(actorSystem, backEnd),
            result -> result.isSuccess()
                  ? result.get()
                        .count()
                        .mapLeft(Routes::mapError)
                        .fold(error -> error,
                              count -> complete(StatusCodes.OK,
                                    new ApiModels.ApiInteractionCount(count),
                                    JSON_MARSHALLER))
                  : complete(ApiModels.getHttpErrorResponse(StatusCodes.IM_A_TEAPOT)));
   }

   public static Route getGidsAll(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      return onComplete(Ask.getGidsAll(actorSystem, backEnd),
            result -> result.isSuccess()
                  ? complete(StatusCodes.OK, result.get(), JSON_MARSHALLER)
                  : complete(ApiModels.getHttpErrorResponse(StatusCodes.IM_A_TEAPOT)));
   }

   public static Route getNotifications(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      return parameter("limit", limit -> parameter("offset",
            offset -> parameter("date", date -> parameter("state", state -> onComplete(Ask.getNotifications(actorSystem,
                  backEnd,
                  Integer.parseInt(limit),
                  Integer.parseInt(offset),
                  LocalDate.parse(date),
                  state),
                  result -> result.isSuccess()
                        ? complete(StatusCodes.OK,
                              result.get(),
                              JSON_MARSHALLER)
                        : complete(ApiModels.getHttpErrorResponse(StatusCodes.IM_A_TEAPOT)))))));
   }

   public static Route getExpandedGoldenRecordsUsingParameterList(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      return parameterList(params -> {
         final var goldenIds = params.stream().map(PARAM_STRING).toList();
         return onComplete(Ask.getExpandedGoldenRecords(actorSystem, backEnd, goldenIds),
               result -> result.isSuccess()
                     ? result.get()
                           .expandedGoldenRecords()
                           .mapLeft(Routes::mapError)
                           .fold(error -> error,
                                 expandedGoldenRecords -> complete(StatusCodes.OK,
                                       expandedGoldenRecords.stream()
                                             .map(ApiModels.ApiExpandedGoldenRecord::fromExpandedGoldenRecord)
                                             .toList(),
                                       JSON_MARSHALLER))
                     : complete(ApiModels.getHttpErrorResponse(StatusCodes.IM_A_TEAPOT)));
      });
   }

   public static Route getExpandedGoldenRecordsFromUsingCSV(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      return parameter("uidList", items -> {
         final var uidList = Stream.of(items.split(",")).map(String::trim).toList();
         return onComplete(
               Ask.getExpandedGoldenRecords(actorSystem, backEnd, uidList),
               result -> result.isSuccess()
                     ? result.get()
                           .expandedGoldenRecords()
                           .mapLeft(Routes::mapError)
                           .fold(error -> error,
                                 expandedGoldenRecords -> complete(StatusCodes.OK,
                                       expandedGoldenRecords.stream()
                                             .map(ApiModels.ApiExpandedGoldenRecord::fromExpandedGoldenRecord)
                                             .toList(),
                                       JSON_MARSHALLER))
                     : complete(ApiModels.getHttpErrorResponse(StatusCodes.IM_A_TEAPOT)));
      });
   }

   public static Route getExpandedInteractionsUsingCSV(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      return parameter("uidList", items -> {
         final var iidList = Stream.of(items.split(",")).map(String::trim).toList();
         return onComplete(Ask.getExpandedInteractions(actorSystem, backEnd, iidList),
               result -> result.isSuccess()
                     ? result.get()
                           .expandedPatientRecords()
                           .mapLeft(Routes::mapError)
                           .fold(error -> error,
                                 expandedPatientRecords -> complete(StatusCodes.OK,
                                       expandedPatientRecords.stream()
                                             .map(ApiModels.ApiExpandedInteraction::fromExpandedInteraction)
                                             .toList(),
                                       JSON_MARSHALLER))
                     : complete(ApiModels.getHttpErrorResponse(StatusCodes.IM_A_TEAPOT)));
      });
   }

   public static Route getExpandedGoldenRecord(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final String gid) {
      return onComplete(Ask.getExpandedGoldenRecord(actorSystem, backEnd, gid),
            result -> result.isSuccess()
                  ? result.get()
                        .goldenRecord()
                        .mapLeft(Routes::mapError)
                        .fold(error -> error,
                              goldenRecord -> complete(StatusCodes.OK,
                                    ApiModels.ApiExpandedGoldenRecord.fromExpandedGoldenRecord(
                                          goldenRecord),
                                    Jackson.marshaller(OBJECT_MAPPER)))
                  : complete(ApiModels.getHttpErrorResponse(StatusCodes.IM_A_TEAPOT)));
   }

   public static Route getInteraction(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final String iid) {
      return onComplete(Ask.getInteraction(actorSystem, backEnd, iid),
            result -> result.isSuccess()
                  ? result.get()
                        .patient()
                        .mapLeft(Routes::mapError)
                        .fold(error -> error,
                              patientRecord -> complete(StatusCodes.OK,
                                    ApiModels.ApiInteraction.fromInteraction(patientRecord),
                                    JSON_MARSHALLER))
                  : complete(ApiModels.getHttpErrorResponse(StatusCodes.IM_A_TEAPOT)));
   }

   public static Route postUpdateNotification(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      return entity(Jackson.unmarshaller(NotificationRequest.class),
            obj -> onComplete(Ask.postUpdateNotification(actorSystem, backEnd, obj), response -> {
               if (response.isSuccess()) {
                  final var updateResponse = response.get();
                  return complete(StatusCodes.OK, updateResponse, JSON_MARSHALLER);
               } else {
                  return complete(ApiModels.getHttpErrorResponse(StatusCodes.IM_A_TEAPOT));
               }
            }));
   }

   public static Route postUploadCsvFile(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      return withSizeLimit(
            1024 * 1024 * 6,
            () -> storeUploadedFile("csv",
                  (info) -> {
                     try {
                        return File.createTempFile("import-", ".csv");
                     } catch (Exception e) {
                        LOGGER.error(e.getMessage(), e);
                        return null;
                     }
                  },
                  (info, file) -> onComplete(Ask.postUploadCsvFile(actorSystem, backEnd, info, file),
                        response -> response.isSuccess()
                              ? complete(StatusCodes.OK)
                              : complete(ApiModels.getHttpErrorResponse(StatusCodes.IM_A_TEAPOT)))));
   }

   public static Route postSimpleSearch(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final RecordType recordType) {
      LOGGER.info("Simple search on {}", recordType);
      return entity(Jackson.unmarshaller(ApiModels.ApiSimpleSearchRequestPayload.class),
            searchParameters -> onComplete(
                  () -> {
                     if (recordType == RecordType.GoldenRecord) {
                        return Ask.postSimpleSearchGoldenRecords(actorSystem, backEnd, searchParameters);
                     } else {
                        return Ask.postSimpleSearchInteractions(actorSystem, backEnd, searchParameters);
                     }
                  },
                  response -> {
                     if (response.isSuccess()) {
                        final var eventSearchRsp = response.get();
                        return complete(StatusCodes.OK, eventSearchRsp, JSON_MARSHALLER);
                     } else {
                        return complete(ApiModels.getHttpErrorResponse(StatusCodes.IM_A_TEAPOT));
                     }
                  }));
   }

   public static Route postFilterGids(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      LOGGER.info("Filter Guids");
      // final ObjectMapper objectMapper = new ObjectMapper();
      // objectMapper.registerModule(new JavaTimeModule());
      return entity(Jackson.unmarshaller(OBJECT_MAPPER, FilterGidsRequestPayload.class),
            searchParameters -> onComplete(
                  () -> Ask.postFilterGids(actorSystem, backEnd, searchParameters),
                  response -> {
                     if (response.isSuccess()) {
                        final var eventSearchRsp = response.get();
                        return complete(StatusCodes.OK, eventSearchRsp, JSON_MARSHALLER);
                     } else {
                        return complete(ApiModels.getHttpErrorResponse(StatusCodes.IM_A_TEAPOT));
                     }
                  }));
   }

   public static Route postFilterGidsWithInteractionCount(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      LOGGER.info("Filter Guids");
      // final ObjectMapper objectMapper = new ObjectMapper();
      // objectMapper.registerModule(new JavaTimeModule());
      return entity(Jackson.unmarshaller(OBJECT_MAPPER, FilterGidsRequestPayload.class),
            searchParameters -> onComplete(
                  () -> Ask.postFilterGidsWithInteractionCount(actorSystem, backEnd, searchParameters),
                  response -> {
                     if (response.isSuccess()) {
                        final var eventSearchRsp = response.get();
                        return complete(StatusCodes.OK, eventSearchRsp, JSON_MARSHALLER);
                     } else {
                        return complete(ApiModels.getHttpErrorResponse(StatusCodes.IM_A_TEAPOT));
                     }
                  }));
   }

   public static Route postCustomSearch(
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
         if (response.isSuccess()) {
            final var eventSearchRsp = response.get();
            return complete(StatusCodes.OK, eventSearchRsp, JSON_MARSHALLER);
         } else {
            return complete(ApiModels.getHttpErrorResponse(StatusCodes.IM_A_TEAPOT));
         }
      }));
   }

   public static CompletionStage<HttpResponse> proxyPostCalculateScores(
         final String linkerIP,
         final Integer linkerPort,
         final Http http,
         final ApiModels.ApiCalculateScoresRequest body) throws JsonProcessingException {
      final var request = HttpRequest
            .create(String.format(Locale.ROOT,
                  "http://%s:%d/JeMPI/%s",
                  linkerIP,
                  linkerPort,
                  GlobalConstants.SEGMENT_PROXY_POST_CALCULATE_SCORES))
            .withMethod(HttpMethods.POST)
            .withEntity(ContentTypes.APPLICATION_JSON, OBJECT_MAPPER.writeValueAsBytes(body));
      final var stage = http.singleRequest(request);
      return stage.thenApply(response -> response);
   }

   public static Route proxyPostCalculateScores(
         final String linkerIp,
         final Integer linkerPort,
         final Http http) {
      return entity(Jackson.unmarshaller(ApiModels.ApiCalculateScoresRequest.class),
            obj -> {
               try {
                  return onComplete(proxyPostCalculateScores(linkerIp,
                        linkerPort,
                        http, obj),
                        response -> response.isSuccess()
                              ? complete(response.get())
                              : complete(ApiModels.getHttpErrorResponse(StatusCodes.IM_A_TEAPOT)));
               } catch (JsonProcessingException e) {
                  LOGGER.error(e.getLocalizedMessage(), e);
                  return complete(ApiModels.getHttpErrorResponse(StatusCodes.IM_A_TEAPOT));
               }
            });
   }

   private static CompletionStage<HttpResponse> proxyGetCandidatesWithScore(
         final String linkerIP,
         final Integer linkerPort,
         final Http http,
         final String iid) throws JsonProcessingException {
      final var uri = Uri
            .create(String.format(Locale.ROOT,
                  "http://%s:%d/JeMPI/%s",
                  linkerIP,
                  linkerPort,
                  GlobalConstants.SEGMENT_PROXY_GET_CANDIDATES_WITH_SCORES))
            .query(Query.create(Pair.create("iid", iid)));
      final var request = HttpRequest.GET(uri.path());
      final var stage = http.singleRequest(request);
      return stage.thenApply(response -> response);
   }

   public static Route proxyGetCandidatesWithScore(
         final String linkerIP,
         final Integer linkerPort,
         final Http http) {
      return parameter("iid",
            iid -> {
               try {
                  return onComplete(proxyGetCandidatesWithScore(linkerIP, linkerPort, http, iid),
                        response -> response.isSuccess()
                              ? complete(response.get())
                              : complete(ApiModels.getHttpErrorResponse(StatusCodes.IM_A_TEAPOT)));
               } catch (JsonProcessingException e) {
                  LOGGER.error(e.getLocalizedMessage(), e);
                  return complete(ApiModels.getHttpErrorResponse(StatusCodes.IM_A_TEAPOT));
               }
            });
   }

   private static CompletionStage<HttpResponse> patchCrUpdateFieldsProxy(
         final String linkerIP,
         final Integer linkerPort,
         final Http http,
         final ApiModels.ApiCrUpdateFieldsRequest body) throws JsonProcessingException {
      final var request = HttpRequest
            .create(String.format(Locale.ROOT,
                  "http://%s:%d/JeMPI/%s",
                  linkerIP,
                  linkerPort,
                  GlobalConstants.SEGMENT_PROXY_PATCH_CR_UPDATE_FIELDS))
            .withMethod(HttpMethods.PATCH)
            .withEntity(ContentTypes.APPLICATION_JSON, OBJECT_MAPPER.writeValueAsBytes(body));
      final var stage = http.singleRequest(request);
      return stage.thenApply(response -> response);
   }

   private static CompletionStage<HttpResponse> postCrRegisterProxy(
         final String linkerIP,
         final Integer linkerPort,
         final Http http,
         final ApiModels.ApiCrRegisterRequest body) throws JsonProcessingException {
      final var request = HttpRequest
            .create(String.format(Locale.ROOT,
                  "http://%s:%d/JeMPI/%s",
                  linkerIP,
                  linkerPort,
                  GlobalConstants.SEGMENT_PROXY_POST_CR_REGISTER))
            .withMethod(HttpMethods.POST)
            .withEntity(ContentTypes.APPLICATION_JSON, OBJECT_MAPPER.writeValueAsBytes(body));
      final var stage = http.singleRequest(request);
      return stage.thenApply(response -> response);
   }

   private static CompletionStage<HttpResponse> postLinkInteractionProxy(
         final String linkerIP,
         final Integer linkerPort,
         final Http http,
         final ApiModels.LinkInteractionSyncBody body) throws JsonProcessingException {
      final var request = HttpRequest
            .create(String.format(Locale.ROOT,
                  "http://%s:%d/JeMPI/%s",
                  linkerIP,
                  linkerPort,
                  GlobalConstants.SEGMENT_PROXY_POST_LINK_INTERACTION))
            .withMethod(HttpMethods.POST)
            .withEntity(ContentTypes.APPLICATION_JSON, OBJECT_MAPPER.writeValueAsBytes(body));
      final var stage = http.singleRequest(request);
      return stage.thenApply(response -> response);
   }

   private static CompletionStage<HttpResponse> postLinkInteractionToGidProxy(
         final String linkerIP,
         final Integer linkerPort,
         final Http http,
         final ApiModels.LinkInteractionToGidSyncBody body) throws JsonProcessingException {
      final var request = HttpRequest
            .create(String.format(Locale.ROOT,
                  "http://%s:%d/JeMPI/%s",
                  linkerIP,
                  linkerPort,
                  GlobalConstants.SEGMENT_PROXY_POST_LINK_INTERACTION_TO_GID))
            .withMethod(HttpMethods.POST)
            .withEntity(ContentTypes.APPLICATION_JSON, OBJECT_MAPPER.writeValueAsBytes(body));
      final var stage = http.singleRequest(request);
      return stage.thenApply(response -> response);
   }

   private static CompletionStage<HttpResponse> postCrCandidatesProxy(
         final String linkerIP,
         final Integer linkerPort,
         final Http http,
         final ApiModels.ApiCrCandidatesRequest body) throws JsonProcessingException {
      final var request = HttpRequest
            .create(String.format(Locale.ROOT,
                  "http://%s:%d/JeMPI/%s",
                  linkerIP,
                  linkerPort,
                  GlobalConstants.SEGMENT_PROXY_POST_CR_CANDIDATES))
            .withMethod(HttpMethods.POST)
            .withEntity(ContentTypes.APPLICATION_JSON, OBJECT_MAPPER.writeValueAsBytes(body));
      final var stage = http.singleRequest(request);
      return stage.thenApply(response -> {
         LOGGER.debug("{}", response);
         return response;
      });
   }

   private static CompletionStage<HttpResponse> postCrFindProxy(
         final String linkerIP,
         final Integer linkerPort,
         final Http http,
         final ApiModels.ApiCrFindRequest body) throws JsonProcessingException {
      final var request = HttpRequest
            .create(String.format(Locale.ROOT,
                  "http://%s:%d/JeMPI/%s",
                  linkerIP,
                  linkerPort,
                  GlobalConstants.SEGMENT_PROXY_POST_CR_FIND))
            .withMethod(HttpMethods.POST)
            .withEntity(ContentTypes.APPLICATION_JSON, OBJECT_MAPPER.writeValueAsBytes(body));
      final var stage = http.singleRequest(request);
      return stage.thenApply(response -> {
         LOGGER.debug("{}", response);
         return response;
      });
   }

   public static Route patchCrUpdateFields(
         final String linkerIP,
         final Integer linkerPort,
         final Http http) {
      return entity(Jackson.unmarshaller(ApiModels.ApiCrUpdateFieldsRequest.class),
            apiCrUpdateFields -> {
               LOGGER.debug("{}", apiCrUpdateFields);
               try {
                  return onComplete(patchCrUpdateFieldsProxy(linkerIP, linkerPort, http, apiCrUpdateFields),
                        response -> response.isSuccess()
                              ? complete(response.get())
                              : complete(ApiModels.getHttpErrorResponse(StatusCodes.IM_A_TEAPOT)));
               } catch (JsonProcessingException e) {
                  LOGGER.error(e.getLocalizedMessage(), e);
                  return complete(ApiModels.getHttpErrorResponse(StatusCodes.IM_A_TEAPOT));
               }
            });
   }

   public static Route postCrFind(
         final String linkerIP,
         final Integer linkerPort,
         final Http http) {
      return entity(
            Jackson.unmarshaller(OBJECT_MAPPER, ApiModels.ApiCrFindRequest.class),
            apiCrFind -> {
               LOGGER.debug("{}", apiCrFind);
               try {
                  return onComplete(postCrFindProxy(linkerIP, linkerPort, http, apiCrFind),
                        response -> response.isSuccess()
                              ? complete(response.get())
                              : complete(ApiModels.getHttpErrorResponse(StatusCodes.IM_A_TEAPOT)));
               } catch (JsonProcessingException e) {
                  LOGGER.error(e.getLocalizedMessage(), e);
                  return complete(StatusCodes.IM_A_TEAPOT);
               }
            });
   }

   public static Route postCrCandidates(
         final String linkerIP,
         final Integer linkerPort,
         final Http http) {
      return entity(Jackson.unmarshaller(OBJECT_MAPPER, ApiModels.ApiCrCandidatesRequest.class),
            apiCrCandidates -> {
               LOGGER.debug("{}", apiCrCandidates);
               try {
                  return onComplete(postCrCandidatesProxy(linkerIP, linkerPort, http, apiCrCandidates),
                        response -> response.isSuccess()
                              ? complete(response.get())
                              : complete(ApiModels.getHttpErrorResponse(StatusCodes.IM_A_TEAPOT)));
               } catch (JsonProcessingException e) {
                  LOGGER.error(e.getLocalizedMessage(), e);
                  return complete(ApiModels.getHttpErrorResponse(StatusCodes.IM_A_TEAPOT));
               }
            });
   }

   public static Route postCrRegister(
         final String linkerIP,
         final Integer linkerPort,
         final Http http) {
      return entity(Jackson.unmarshaller(OBJECT_MAPPER, ApiModels.ApiCrRegisterRequest.class),
            apiCrRegister -> {
               LOGGER.debug("{}", apiCrRegister);
               try {
                  return onComplete(postCrRegisterProxy(linkerIP, linkerPort, http, apiCrRegister),
                        response -> response.isSuccess()
                              ? complete(response.get())
                              : complete(ApiModels.getHttpErrorResponse(StatusCodes.IM_A_TEAPOT)));
               } catch (JsonProcessingException e) {
                  LOGGER.error(e.getLocalizedMessage(), e);
                  return complete(StatusCodes.NO_CONTENT);
               }
            });
   }

   public static Route postLinkInteraction(
         final String linkerIP,
         final Integer linkerPort,
         final Http http) {
      return entity(Jackson.unmarshaller(OBJECT_MAPPER, ApiModels.LinkInteractionSyncBody.class),
            linkInteractionSyncBody -> {
               try {
                  return onComplete(postLinkInteractionProxy(linkerIP, linkerPort, http, linkInteractionSyncBody),
                        response -> response.isSuccess()
                              ? complete(response.get())
                              : complete(ApiModels.getHttpErrorResponse(StatusCodes.IM_A_TEAPOT)));
               } catch (JsonProcessingException e) {
                  LOGGER.error(e.getLocalizedMessage(), e);
                  return complete(StatusCodes.NO_CONTENT);
               }
            });
   }

   public static Route postLinkInteractionToGid(
         final String linkerIP,
         final Integer linkerPort,
         final Http http) {
      return entity(Jackson.unmarshaller(OBJECT_MAPPER, ApiModels.LinkInteractionToGidSyncBody.class),
            apiLinkInteractionToGid -> {
               try {
                  return onComplete(postLinkInteractionToGidProxy(linkerIP, linkerPort, http, apiLinkInteractionToGid),
                        response -> response.isSuccess()
                              ? complete(response.get())
                              : complete(ApiModels.getHttpErrorResponse(StatusCodes.IM_A_TEAPOT)));
               } catch (JsonProcessingException e) {
                  LOGGER.error(e.getLocalizedMessage(), e);
                  return complete(StatusCodes.NO_CONTENT);
               }
            });
   }

   public static Route createCoreAPIRoutes(
           final ActorSystem<Void> actorSystem,
           final ActorRef<BackEnd.Event> backEnd,
           final String jsonFields,
           final String linkerIP,
           final Integer linkerPort,
           final Http http) {
      return concat(post(() -> concat(path(GlobalConstants.SEGMENT_POST_UPDATE_NOTIFICATION,
                              () -> Routes.postUpdateNotification(actorSystem, backEnd)),
                      path(segment(GlobalConstants.SEGMENT_POST_SIMPLE_SEARCH).slash(segment(Pattern.compile(
                                      "^(golden|patient)$"))),
                              type -> Routes.postSimpleSearch(actorSystem,
                                      backEnd,
                                      type.equals("golden")
                                              ? RecordType.GoldenRecord
                                              : RecordType.Interaction)),
                      path(segment(GlobalConstants.SEGMENT_POST_CUSTOM_SEARCH).slash(segment(Pattern.compile(
                                      "^(golden|patient)$"))),
                              type -> Routes.postCustomSearch(actorSystem,
                                      backEnd,
                                      type.equals("golden")
                                              ? RecordType.GoldenRecord
                                              : RecordType.Interaction)),
                      path(GlobalConstants.SEGMENT_POST_UPLOAD_CSV_FILE,
                              () -> Routes.postUploadCsvFile(actorSystem, backEnd)),
                      path(GlobalConstants.SEGMENT_PROXY_POST_CALCULATE_SCORES,
                              () -> Routes.proxyPostCalculateScores(linkerIP, linkerPort, http)),
                      path(GlobalConstants.SEGMENT_POST_FILTER_GIDS,
                              () -> Routes.postFilterGids(actorSystem, backEnd)),
                      path(GlobalConstants.SEGMENT_PROXY_POST_LINK_INTERACTION,
                              () -> Routes.postLinkInteraction(linkerIP, linkerPort, http)),
                      path(GlobalConstants.SEGMENT_PROXY_POST_LINK_INTERACTION_TO_GID,
                              () -> Routes.postLinkInteractionToGid(linkerIP, linkerPort, http)),
                      path(GlobalConstants.SEGMENT_PROXY_POST_CR_REGISTER,
                              () -> Routes.postCrRegister(linkerIP, linkerPort, http)),
                      path(GlobalConstants.SEGMENT_PROXY_POST_CR_FIND,
                              () -> Routes.postCrFind(linkerIP, linkerPort, http)),
                      path(GlobalConstants.SEGMENT_PROXY_POST_CR_CANDIDATES,
                              () -> Routes.postCrCandidates(linkerIP, linkerPort, http)),
                      path(GlobalConstants.SEGMENT_POST_FILTER_GIDS_WITH_INTERACTION_COUNT,
                              () -> Routes.postFilterGidsWithInteractionCount(actorSystem, backEnd)))),
              patch(() -> concat(path(segment(GlobalConstants.SEGMENT_PATCH_GOLDEN_RECORD).slash(segment(Pattern.compile(
                              "^[A-z0-9]+$"))), gid -> Routes.patchGoldenRecord(actorSystem, backEnd, gid)),
                      path(GlobalConstants.SEGMENT_PATCH_IID_NEW_GID_LINK,
                              () -> Routes.patchIidNewGidLink(actorSystem, backEnd)),
                      path(GlobalConstants.SEGMENT_PATCH_IID_GID_LINK,
                              () -> Routes.patchIidGidLink(actorSystem, backEnd)),
                      path(GlobalConstants.SEGMENT_PROXY_PATCH_CR_UPDATE_FIELDS,
                              () -> Routes.patchCrUpdateFields(linkerIP, linkerPort, http)))),
              get(() -> concat(path(GlobalConstants.SEGMENT_COUNT_GOLDEN_RECORDS,
                              () -> Routes.countGoldenRecords(actorSystem, backEnd)),
                      path(GlobalConstants.SEGMENT_COUNT_INTERACTIONS,
                              () -> Routes.countInteractions(actorSystem, backEnd)),
                      path(GlobalConstants.SEGMENT_COUNT_RECORDS,
                              () -> Routes.countRecords(actorSystem, backEnd)),
                      path(GlobalConstants.SEGMENT_GET_GIDS_ALL,
                              () -> Routes.getGidsAll(actorSystem, backEnd)),
                      path(GlobalConstants.SEGMENT_GET_GIDS_PAGED,
                              () -> Routes.getGidsPaged(actorSystem, backEnd)),
                      path(segment(GlobalConstants.SEGMENT_GET_INTERACTION).slash(segment(Pattern.compile(
                              "^[A-z0-9]+$"))), iid -> Routes.getInteraction(actorSystem, backEnd, iid)),
                      path(segment(GlobalConstants.SEGMENT_GET_EXPANDED_GOLDEN_RECORD).slash(segment(Pattern.compile(
                              "^[A-z0-9]+$"))), gid -> Routes.getExpandedGoldenRecord(actorSystem, backEnd, gid)),
                      path(GlobalConstants.SEGMENT_GET_EXPANDED_GOLDEN_RECORDS_USING_PARAMETER_LIST,
                              () -> Routes.getExpandedGoldenRecordsUsingParameterList(actorSystem, backEnd)),
                      path(GlobalConstants.SEGMENT_GET_EXPANDED_GOLDEN_RECORDS_USING_CSV,
                              () -> Routes.getExpandedGoldenRecordsFromUsingCSV(actorSystem, backEnd)),
                      path(GlobalConstants.SEGMENT_GET_EXPANDED_INTERACTIONS_USING_CSV,
                              () -> Routes.getExpandedInteractionsUsingCSV(actorSystem, backEnd)),
                      path(GlobalConstants.SEGMENT_GET_GOLDEN_RECORD_AUDIT_TRAIL,
                              () -> Routes.getGoldenRecordAuditTrail(actorSystem, backEnd)),
                      path(GlobalConstants.SEGMENT_GET_INTERACTION_AUDIT_TRAIL,
                              () -> Routes.getInteractionAuditTrail(actorSystem, backEnd)),
                      path(GlobalConstants.SEGMENT_GET_NOTIFICATIONS,
                              () -> Routes.getNotifications(actorSystem, backEnd)),
                      path(segment(GlobalConstants.SEGMENT_GET_INTERACTION).slash(segment(Pattern.compile(
                              "^[A-z0-9]+$"))), iid -> Routes.getInteraction(actorSystem, backEnd, iid)),
                      path(segment(GlobalConstants.SEGMENT_GET_EXPANDED_GOLDEN_RECORD).slash(segment(Pattern.compile(
                              "^[A-z0-9]+$"))), gid -> Routes.getExpandedGoldenRecord(actorSystem, backEnd, gid)),
                      path(GlobalConstants.SEGMENT_GET_FIELDS_CONFIG, () -> complete(StatusCodes.OK, jsonFields)),
                      path(GlobalConstants.SEGMENT_PROXY_GET_CANDIDATES_WITH_SCORES,
                              () -> Routes.proxyGetCandidatesWithScore(linkerIP, linkerPort, http)))));
   }

}
