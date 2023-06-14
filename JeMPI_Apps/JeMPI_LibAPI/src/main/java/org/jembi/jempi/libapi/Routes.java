package org.jembi.jempi.libapi;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.http.javadsl.Http;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.*;
import akka.http.javadsl.server.Route;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.libmpi.MpiGeneralError;
import org.jembi.jempi.libmpi.MpiServiceError;
import org.jembi.jempi.shared.models.*;
import org.jembi.jempi.shared.utils.AppUtils;

import java.io.File;
import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.stream.Stream;

import static akka.http.javadsl.server.Directives.*;

public final class Routes {

   private static final Logger LOGGER = LogManager.getLogger(Routes.class);
   private static final Function<Map.Entry<String, String>, String> PARAM_STRING = Map.Entry::getValue;


   private Routes() {
   }

   static Route mapError(final MpiGeneralError obj) {
      LOGGER.debug("{}", obj);
      return switch (obj) {
         case MpiServiceError.InteractionIdDoesNotExistError e -> complete(StatusCodes.BAD_REQUEST, e, Jackson.marshaller());
         case MpiServiceError.GoldenIdDoesNotExistError e -> complete(StatusCodes.BAD_REQUEST, e, Jackson.marshaller());
         case MpiServiceError.GoldenIdInteractionConflictError e -> complete(StatusCodes.BAD_REQUEST, e, Jackson.marshaller());
         case MpiServiceError.DeletePredicateError e -> complete(StatusCodes.BAD_REQUEST, e, Jackson.marshaller());
         default -> complete(StatusCodes.INTERNAL_SERVER_ERROR);
      };
   }

   public static Route routeUpdateGoldenRecordFields(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final String goldenId) {
      return entity(Jackson.unmarshaller(GoldenRecordUpdateRequestPayload.class),
                    payload -> payload != null
                          ? onComplete(Ask.updateGoldenRecordFields(actorSystem, backEnd, goldenId, payload),
                                       result -> {
                                          if (result.isSuccess()) {
                                             final var updatedFields = result.get().fields();
                                             if (updatedFields.isEmpty()) {
                                                return complete(StatusCodes.BAD_REQUEST);
                                             } else {
                                                return complete(StatusCodes.OK, result.get(), Jackson.marshaller());
                                             }
                                          } else {
                                             return complete(StatusCodes.INTERNAL_SERVER_ERROR);
                                          }
                                       })
                          : complete(StatusCodes.NO_CONTENT));
   }

   public static Route routeNumberOfRecords(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      return onComplete(Ask.getNumberOfRecords(actorSystem, backEnd),
                        result -> result.isSuccess()
                              ? complete(StatusCodes.OK,
                                         new ApiModels.ApiNumberOfRecords(result.get().goldenRecords(),
                                                                          result.get().patientRecords()),
                                         Jackson.marshaller())
                              : complete(StatusCodes.IM_A_TEAPOT));
   }

   public static Route routeFetchGoldenIds(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      return parameter("offset",
                       offset -> parameter("length",
                                           length -> onComplete(Ask.fetchGoldenIds(actorSystem,
                                                                                   backEnd,
                                                                                   Long.parseLong(offset),
                                                                                   Long.parseLong(length)),
                                                                result -> result.isSuccess()
                                                                      ? complete(StatusCodes.OK,
                                                                                 result.get(),
                                                                                 Jackson.marshaller())
                                                                      : complete(StatusCodes.IM_A_TEAPOT))));
   }

   public static Route routeGoldenRecordAuditTrail(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      return parameter("uid",
                       uid -> onComplete(Ask.goldenRecordAuditTrail(actorSystem, backEnd, uid),
                                         result -> result.isSuccess()
                                               ? complete(StatusCodes.OK,
                                                          ApiModels.ApiAuditTrail.fromAuditTrail(result.get().auditTrail()),
                                                          Jackson.marshaller())
                                               : complete(StatusCodes.IM_A_TEAPOT)));
   }

   public static Route routeInteractionAuditTrail(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      return parameter("uid",
                       uid -> onComplete(Ask.interactionAuditTrail(actorSystem, backEnd, uid),
                                         result -> result.isSuccess()
                                               ? complete(StatusCodes.OK,
                                                          ApiModels.ApiAuditTrail.fromAuditTrail(result.get().auditTrail()),
                                                          Jackson.marshaller())
                                               : complete(StatusCodes.IM_A_TEAPOT)));
   }

   public static Route routeUpdateLinkToNewGoldenRecord(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      return parameter("goldenID",
                       currentGoldenId -> parameter("patientID",
                                                    patientId -> onComplete(Ask.updateLinkToNewGoldenRecord(actorSystem,
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
                                                                                                                     Jackson.marshaller()))
                                                                                  : complete(StatusCodes.IM_A_TEAPOT))));
   }

   public static Route routeUpdateLinkToExistingGoldenRecord(
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
                                                                             Ask.updateLinkToExistingGoldenRecord(
                                                                                   actorSystem,
                                                                                   backEnd,
                                                                                   currentGoldenId,
                                                                                   newGoldenId,
                                                                                   patientId,
                                                                                   Float.parseFloat(
                                                                                         score)),
                                                                             result -> result.isSuccess()
                                                                                   ? result.get()
                                                                                           .linkInfo()
                                                                                           .mapLeft(Routes::mapError)
                                                                                           .fold(error -> error,
                                                                                                 linkInfo -> complete(
                                                                                                       StatusCodes.OK,
                                                                                                       linkInfo,
                                                                                                       Jackson.marshaller()))
                                                                                   : complete(StatusCodes.IM_A_TEAPOT))))));
   }

   public static Route routeGoldenRecordCount(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      return onComplete(Ask.getGoldenRecordCount(actorSystem, backEnd),
                        result -> result.isSuccess()
                              ? result.get()
                                      .count()
                                      .mapLeft(Routes::mapError)
                                      .fold(error -> error,
                                            count -> complete(StatusCodes.OK,
                                                              new ApiModels.ApiGoldenRecordCount(count),
                                                              Jackson.marshaller()))
                              : complete(StatusCodes.IM_A_TEAPOT));
   }

   public static Route routeInteractionCount(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      return onComplete(Ask.getInteractionCount(actorSystem, backEnd),
                        result -> result.isSuccess()
                              ? result.get()
                                      .count()
                                      .mapLeft(Routes::mapError)
                                      .fold(error -> error,
                                            count -> complete(StatusCodes.OK,
                                                              new ApiModels.ApiPatientCount(count),
                                                              Jackson.marshaller()))
                              : complete(StatusCodes.IM_A_TEAPOT));
   }

   public static Route routeGoldenIds(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      return onComplete(Ask.getGoldenIds(actorSystem, backEnd),
                        result -> result.isSuccess()
                              ? complete(StatusCodes.OK, result.get(), Jackson.marshaller())
                              : complete(StatusCodes.IM_A_TEAPOT));
   }

   public static Route routeFindMatchesForReview(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final int limit,
         final int offset,
         final LocalDate date) {
      return onComplete(Ask.findMatchesForReview(actorSystem, backEnd, limit, offset, date),
                        result -> result.isSuccess()
                              ? complete(StatusCodes.OK, result.get(), Jackson.marshaller())
                              : complete(StatusCodes.IM_A_TEAPOT));
   }

   public static Route routeGoldenRecord(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      return parameterList(params -> {
         final var goldenIds = params.stream().map(PARAM_STRING).toList();
         return onComplete(Ask.findExpandedGoldenRecords(actorSystem, backEnd, goldenIds),
                           result -> result.isSuccess()
                                 ? result.get()
                                         .expandedGoldenRecords()
                                         .mapLeft(Routes::mapError)
                                         .fold(error -> error,
                                               expandedGoldenRecords -> complete(StatusCodes.OK,
                                                                                 expandedGoldenRecords.stream()
                                                                                                      .map(ApiModels.ApiExpandedGoldenRecord::fromExpandedGoldenRecord)
                                                                                                      .toList(),
                                                                                 Jackson.marshaller()))
                                 : complete(StatusCodes.IM_A_TEAPOT));
      });
   }

   public static Route routeExpandedGoldenRecords(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      return parameter("uidList", items -> {
         final var uidList = Stream.of(items.split(",")).map(String::trim).toList();
         return onComplete(
               Ask.findExpandedGoldenRecords(actorSystem, backEnd, uidList),
               result -> result.isSuccess()
                     ? result.get()
                             .expandedGoldenRecords()
                             .mapLeft(Routes::mapError)
                             .fold(error -> error,
                                   expandedGoldenRecords -> complete(StatusCodes.OK,
                                                                     expandedGoldenRecords.stream()
                                                                                          .map(ApiModels.ApiExpandedGoldenRecord::fromExpandedGoldenRecord)
                                                                                          .toList(),
                                                                     Jackson.marshaller()))
                     : complete(StatusCodes.IM_A_TEAPOT));
      });
   }

   public static Route routeExpandedPatientRecords(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      return parameter("uidList", items -> {
         final var uidList = Stream.of(items.split(",")).map(String::trim).toList();
         return onComplete(Ask.findExpandedPatientRecords(actorSystem, backEnd, uidList),
                           result -> result.isSuccess()
                                 ? result.get()
                                         .expandedPatientRecords()
                                         .mapLeft(Routes::mapError)
                                         .fold(error -> error,
                                               expandedPatientRecords -> complete(StatusCodes.OK,
                                                                                  expandedPatientRecords.stream()
                                                                                                        .map(ApiModels.ApiExpandedPatientRecord::fromExpandedPatientRecord)
                                                                                                        .toList(),
                                                                                  Jackson.marshaller()))
                                 : complete(StatusCodes.IM_A_TEAPOT));
      });
   }

   public static Route routeFindExpandedGoldenRecord(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final String goldenId) {
      return onComplete(Ask.findExpandedGoldenRecord(actorSystem, backEnd, goldenId),
                        result -> result.isSuccess()
                              ? result.get()
                                      .goldenRecord()
                                      .mapLeft(Routes::mapError)
                                      .fold(error -> error,
                                            goldenRecord -> complete(StatusCodes.OK,
                                                                     ApiModels.ApiExpandedGoldenRecord.fromExpandedGoldenRecord(
                                                                           goldenRecord),
                                                                     Jackson.marshaller()))
                              : complete(StatusCodes.IM_A_TEAPOT));
   }

   public static Route routeFindPatientRecord(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final String patientId) {
      return onComplete(Ask.findPatientRecord(actorSystem, backEnd, patientId),
                        result -> result.isSuccess()
                              ? result.get()
                                      .patient()
                                      .mapLeft(Routes::mapError)
                                      .fold(error -> error,
                                            patientRecord -> complete(StatusCodes.OK,
                                                                      ApiModels.ApiPatientRecord.fromPatientRecord(patientRecord),
                                                                      Jackson.marshaller()))
                              : complete(StatusCodes.IM_A_TEAPOT));
   }

   public static Route routeFindCandidates(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      return parameter("uid",
                       patientId -> entity(Jackson.unmarshaller(CustomMU.class),
                                           mu -> onComplete(Ask.findCandidates(actorSystem, backEnd, patientId, mu),
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

   public static Route routeUpdateNotificationState(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      return entity(Jackson.unmarshaller(NotificationRequest.class),
                    obj -> onComplete(Ask.updateNotificationState(actorSystem, backEnd, obj), response -> {
                       if (response.isSuccess()) {
                          final var updateResponse = response.get();
                          return complete(StatusCodes.OK, updateResponse, Jackson.marshaller());
                       } else {
                          return complete(StatusCodes.IM_A_TEAPOT);
                       }
                    }));
   }

   public static Route routeUploadCsvFile(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      return withSizeLimit(
            1024 * 1024 * 6,
            () -> storeUploadedFile("csv",
                                    (info) -> {
                                       try {
                                          LOGGER.debug(GlobalConstants.SEGMENT_UPLOAD);
                                          return File.createTempFile("import-", ".csv");
                                       } catch (Exception e) {
                                          LOGGER.error(e.getMessage(), e);
                                          return null;
                                       }
                                    },
                                    (info, file) -> onComplete(Ask.uploadCsvFile(actorSystem, backEnd, info, file),
                                                               response -> response.isSuccess()
                                                                     ? complete(StatusCodes.OK)
                                                                     : complete(StatusCodes.IM_A_TEAPOT))));
   }

   public static Route routeSimpleSearch(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final RecordType recordType) {
      LOGGER.info("Simple search on {}", recordType);
      return entity(Jackson.unmarshaller(SimpleSearchRequestPayload.class),
                    searchParameters -> onComplete(
                          () -> {
                             if (recordType == RecordType.GoldenRecord) {
                                return Ask.simpleSearchGoldenRecords(actorSystem, backEnd, searchParameters);
                             } else {
                                return Ask.simpleSearchInteractions(actorSystem, backEnd, searchParameters);
                             }
                          },
                          response -> {
                             if (response.isSuccess()) {
                                final var eventSearchRsp = response.get();
                                return complete(StatusCodes.OK, eventSearchRsp, Jackson.marshaller());
                             } else {
                                return complete(StatusCodes.IM_A_TEAPOT);
                             }
                          }));
   }

   public static Route routeCustomSearch(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final RecordType recordType) {
      return entity(Jackson.unmarshaller(CustomSearchRequestPayload.class), searchParameters -> onComplete(() -> {
         if (recordType == RecordType.GoldenRecord) {
            return Ask.customSearchGoldenRecords(actorSystem, backEnd, searchParameters);
         } else {
            return Ask.customSearchInteractions(actorSystem, backEnd, searchParameters);
         }
      }, response -> {
         if (response.isSuccess()) {
            final var eventSearchRsp = response.get();
            return complete(StatusCodes.OK, eventSearchRsp, Jackson.marshaller());
         } else {
            return complete(StatusCodes.IM_A_TEAPOT);
         }
      }));
   }

   public static CompletionStage<HttpResponse> proxyPostCalculateScores(
         final Http http,
         final CalculateScoresRequest body) throws JsonProcessingException {
      final var request = HttpRequest
            .create("http://linker:50000/JeMPI/calculate-scores")
            .withMethod(HttpMethods.POST)
            .withEntity(ContentTypes.APPLICATION_JSON, AppUtils.OBJECT_MAPPER.writeValueAsBytes(body));
      final var stage = http.singleRequest(request);
      return stage.thenApply(response -> response);
   }

   public static Route routeCalculateScores(final Http http) {
      return entity(Jackson.unmarshaller(CalculateScoresRequest.class),
                    obj -> {
                       try {
                          return onComplete(proxyPostCalculateScores(http, obj),
                                            response -> response.isSuccess()
                                                  ? complete(response.get())
                                                  : complete(StatusCodes.IM_A_TEAPOT));
                       } catch (JsonProcessingException e) {
                          LOGGER.error(e.getLocalizedMessage(), e);
                          return complete(StatusCodes.IM_A_TEAPOT);
                       }
                    });
   }

}
