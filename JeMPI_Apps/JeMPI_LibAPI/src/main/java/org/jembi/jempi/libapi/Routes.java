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
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.stream.Stream;

import static akka.http.javadsl.server.Directives.*;
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
                              : complete(StatusCodes.IM_A_TEAPOT));
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
                                                                      : complete(StatusCodes.IM_A_TEAPOT))));
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
                                               : complete(StatusCodes.IM_A_TEAPOT)));
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
                                               : complete(StatusCodes.IM_A_TEAPOT)));
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
                                                                                  : complete(StatusCodes.IM_A_TEAPOT))));
   }

   public static Route patchIidGidLink(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      return parameter("goldenID",
                       currentGoldenId ->
                             parameter("newGoldenID",
                                       newGoldenId ->
                                             parameter("patientID",
                                                       patientId ->
                                                             parameter("score",
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
                                                                                   : complete(StatusCodes.IM_A_TEAPOT))))));
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
                              : complete(StatusCodes.IM_A_TEAPOT));
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
                              : complete(StatusCodes.IM_A_TEAPOT));
   }

   public static Route getGidsAll(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      return onComplete(Ask.getGidsAll(actorSystem, backEnd),
                        result -> result.isSuccess()
                              ? complete(StatusCodes.OK, result.get(), JSON_MARSHALLER)
                              : complete(StatusCodes.IM_A_TEAPOT));
   }

   public static Route getNotifications(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      return
            parameter("limit", limit ->
                  parameter("offset", offset ->
                        parameter("date", date ->
                              parameter("state", state ->
                                    onComplete(Ask.getNotifications(actorSystem,
                                                                    backEnd,
                                                                    Integer.parseInt(limit),
                                                                    Integer.parseInt(offset),
                                                                    LocalDate.parse(date),
                                                                    state),
                                               result -> result.isSuccess()
                                                     ? complete(StatusCodes.OK,
                                                                result.get(),
                                                                JSON_MARSHALLER)
                                                     : complete(StatusCodes.IM_A_TEAPOT))))));
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
                                 : complete(StatusCodes.IM_A_TEAPOT));
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
                     : complete(StatusCodes.IM_A_TEAPOT));
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
                                 : complete(StatusCodes.IM_A_TEAPOT));
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
                              : complete(StatusCodes.IM_A_TEAPOT));
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
                              : complete(StatusCodes.IM_A_TEAPOT));
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
                          return complete(StatusCodes.IM_A_TEAPOT);
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
                                                                     : complete(StatusCodes.IM_A_TEAPOT))));
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
                                return complete(StatusCodes.IM_A_TEAPOT);
                             }
                          }));
   }

   public static Route postFilterGids(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      LOGGER.info("Filter Guids");
//      final ObjectMapper objectMapper = new ObjectMapper();
//      objectMapper.registerModule(new JavaTimeModule());
      return entity(Jackson.unmarshaller(OBJECT_MAPPER, FilterGidsRequestPayload.class),
                    searchParameters -> onComplete(
                          () -> Ask.postFilterGids(actorSystem, backEnd, searchParameters),
                          response -> {
                             if (response.isSuccess()) {
                                final var eventSearchRsp = response.get();
                                return complete(StatusCodes.OK, eventSearchRsp, JSON_MARSHALLER);
                             } else {
                                return complete(StatusCodes.IM_A_TEAPOT);
                             }
                          }));
   }

   public static Route postFilterGidsWithInteractionCount(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      LOGGER.info("Filter Guids");
//      final ObjectMapper objectMapper = new ObjectMapper();
//      objectMapper.registerModule(new JavaTimeModule());
      return entity(Jackson.unmarshaller(OBJECT_MAPPER, FilterGidsRequestPayload.class),
                    searchParameters -> onComplete(
                          () -> Ask.postFilterGidsWithInteractionCount(actorSystem, backEnd, searchParameters),
                          response -> {
                             if (response.isSuccess()) {
                                final var eventSearchRsp = response.get();
                                return complete(StatusCodes.OK, eventSearchRsp, JSON_MARSHALLER);
                             } else {
                                return complete(StatusCodes.IM_A_TEAPOT);
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
            return complete(StatusCodes.IM_A_TEAPOT);
         }
      }));
   }

   public static CompletionStage<HttpResponse> proxyPostCalculateScores(
         final Http http,
         final ApiModels.ApiCalculateScoresRequest body,
         final String linkerHost) throws JsonProcessingException {
      final String url = String.format("http://%s/JeMPI/%s", linkerHost, GlobalConstants.SEGMENT_PROXY_POST_CALCULATE_SCORES);
      final var request = HttpRequest
            .create(url)
            .withMethod(HttpMethods.POST)
            .withEntity(ContentTypes.APPLICATION_JSON, OBJECT_MAPPER.writeValueAsBytes(body));
      final var stage = http.singleRequest(request);
      return stage.thenApply(response -> response);
   }

   public static Route proxyPostCalculateScores(final Http http, final String linkerHost) {
      return entity(Jackson.unmarshaller(ApiModels.ApiCalculateScoresRequest.class),
                    obj -> {
                       try {
                          return onComplete(proxyPostCalculateScores(http, obj, linkerHost),
                                            response -> response.isSuccess()
                                                  ? complete(response.get())
                                                  : complete(StatusCodes.IM_A_TEAPOT));
                       } catch (JsonProcessingException e) {
                          LOGGER.error(e.getLocalizedMessage(), e);
                          return complete(StatusCodes.IM_A_TEAPOT);
                       }
                    });
   }


   private static CompletionStage<HttpResponse> proxyGetCandidatesWithScore(
         final Http http,
         final String iid,
         final String linkerHost) throws JsonProcessingException {
      final String url = String.format("http://%s/JeMPI/%s", linkerHost, GlobalConstants.SEGMENT_PROXY_GET_CANDIDATES_WITH_SCORES);
      final var uri = Uri
            .create(url)
            .query(Query.create(Pair.create("iid", iid)));
      final var request = HttpRequest.GET(uri.path());
      final var stage = http.singleRequest(request);
      return stage.thenApply(response -> response);
   }

   public static Route proxyGetCandidatesWithScore(final Http http, final String linkerHost) {
      return parameter("iid",
                       iid -> {
                          try {
                             return onComplete(proxyGetCandidatesWithScore(http, iid, linkerHost),
                                               response -> response.isSuccess()
                                                     ? complete(response.get())
                                                     : complete(StatusCodes.IM_A_TEAPOT));
                          } catch (JsonProcessingException e) {
                             LOGGER.error(e.getLocalizedMessage(), e);
                             return complete(StatusCodes.IM_A_TEAPOT);
                          }
                       });
   }

   private static CompletionStage<HttpResponse> patchCrUpdateFieldsProxy(
         final Http http,
         final ApiModels.ApiCrUpdateFieldsRequest body,
         final String linkerHost) throws JsonProcessingException {
      final String url = String.format("http://%s/JeMPI/%s", linkerHost, GlobalConstants.SEGMENT_PROXY_CR_UPDATE_FIELDS);
      final var request = HttpRequest
            .create(url)
            .withMethod(HttpMethods.PATCH)
            .withEntity(ContentTypes.APPLICATION_JSON, OBJECT_MAPPER.writeValueAsBytes(body));
      final var stage = http.singleRequest(request);
      return stage.thenApply(response -> response);
   }

   private static CompletionStage<HttpResponse> postCrRegisterProxy(
         final Http http,
         final ApiModels.ApiCrRegisterRequest body,
         final String linkerHost) throws JsonProcessingException {
      final String url = String.format("http://%s/JeMPI/%s", linkerHost, GlobalConstants.SEGMENT_PROXY_CR_REGISTER);
      final var request = HttpRequest
            .create(url)
            .withMethod(HttpMethods.POST)
            .withEntity(ContentTypes.APPLICATION_JSON, OBJECT_MAPPER.writeValueAsBytes(body));
      final var stage = http.singleRequest(request);
      return stage.thenApply(response -> response);
   }

   private static CompletionStage<HttpResponse> postCrCandidatesProxy(
         final Http http,
         final ApiModels.ApiCrCandidatesRequest body,
         final String linkerHost) throws JsonProcessingException {
      final String url = String.format("http://%s/JeMPI/%s", linkerHost, GlobalConstants.SEGMENT_PROXY_CR_CANDIDATES);
      final var request = HttpRequest
            .create(url)
            .withMethod(HttpMethods.POST)
            .withEntity(ContentTypes.APPLICATION_JSON, OBJECT_MAPPER.writeValueAsBytes(body));
      final var stage = http.singleRequest(request);
      return stage.thenApply(response -> {
         LOGGER.debug("{}", response);
         return response;
      });
   }

   private static CompletionStage<HttpResponse> postCrFindProxy(
         final Http http,
         final ApiModels.ApiCrFindRequest body,
         final String linkerHost) throws JsonProcessingException {
      final String url = String.format("http://%s/JeMPI/%s", linkerHost, GlobalConstants.SEGMENT_PROXY_CR_FIND);
      final var request = HttpRequest
            .create(url)
            .withMethod(HttpMethods.POST)
            .withEntity(ContentTypes.APPLICATION_JSON, OBJECT_MAPPER.writeValueAsBytes(body));
      final var stage = http.singleRequest(request);
      return stage.thenApply(response -> {
         LOGGER.debug("{}", response);
         return response;
      });
   }

   public static Route patchCrUpdateFields(final Http http, final String linkerHost) {
      return entity(Jackson.unmarshaller(ApiModels.ApiCrUpdateFieldsRequest.class),
                    apiCrUpdateFields -> {
                       LOGGER.debug("{}", apiCrUpdateFields);
                       try {
                          return onComplete(patchCrUpdateFieldsProxy(http, apiCrUpdateFields, linkerHost),
                                            response -> response.isSuccess()
                                                  ? complete(response.get())
                                                  : complete(StatusCodes.IM_A_TEAPOT));
                       } catch (JsonProcessingException e) {
                          LOGGER.error(e.getLocalizedMessage(), e);
                          return complete(StatusCodes.IM_A_TEAPOT);
                       }
                    });
   }

   public static Route postCrFind(final Http http, final String linkerHost) {
      return entity(
            Jackson.unmarshaller(OBJECT_MAPPER, ApiModels.ApiCrFindRequest.class),
            apiCrFind -> {
               LOGGER.debug("{}", apiCrFind);
               try {
                  return onComplete(postCrFindProxy(http, apiCrFind, linkerHost),
                                    response -> response.isSuccess()
                                          ? complete(response.get())
                                          : complete(StatusCodes.IM_A_TEAPOT));
               } catch (JsonProcessingException e) {
                  LOGGER.error(e.getLocalizedMessage(), e);
                  return complete(StatusCodes.IM_A_TEAPOT);
               }
            });
   }

   public static Route postCrCandidates(final Http http, final String linkerHost) {
      return entity(Jackson.unmarshaller(OBJECT_MAPPER, ApiModels.ApiCrCandidatesRequest.class),
                    apiCrCandidates -> {
                       LOGGER.debug("{}", apiCrCandidates);
                       try {
                          return onComplete(postCrCandidatesProxy(http, apiCrCandidates, linkerHost),
                                            response -> response.isSuccess()
                                                  ? complete(response.get())
                                                  : complete(StatusCodes.IM_A_TEAPOT));
                       } catch (JsonProcessingException e) {
                          LOGGER.error(e.getLocalizedMessage(), e);
                          return complete(StatusCodes.IM_A_TEAPOT);
                       }
                    });
   }

   public static Route postCrRegister(final Http http, final String linkerHost) {
      return entity(Jackson.unmarshaller(OBJECT_MAPPER, ApiModels.ApiCrRegisterRequest.class),
                    apiCrRegister -> {
                       LOGGER.debug("{}", apiCrRegister);
                       try {
                          return onComplete(postCrRegisterProxy(http, apiCrRegister, linkerHost),
                                            response -> response.isSuccess()
                                                  ? complete(response.get())
                                                  : complete(StatusCodes.IM_A_TEAPOT));
                       } catch (JsonProcessingException e) {
                          LOGGER.error(e.getLocalizedMessage(), e);
                          return complete(StatusCodes.NO_CONTENT);
                       }
                    });
   }

}
