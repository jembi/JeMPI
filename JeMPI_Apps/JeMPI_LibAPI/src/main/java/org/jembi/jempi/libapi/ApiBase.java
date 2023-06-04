package org.jembi.jempi.libapi;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.*;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.libmpi.MpiGeneralError;
import org.jembi.jempi.libmpi.MpiServiceError;
import org.jembi.jempi.shared.models.*;
import org.jembi.jempi.shared.utils.AppUtils;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.stream.Stream;

public abstract class ApiBase extends AllDirectives {

   private static final Logger LOGGER = LogManager.getLogger(ApiBase.class);
   private static final Function<Map.Entry<String, String>, String> PARAM_STRING = Map.Entry::getValue;
   private CompletionStage<ServerBinding> binding = null;
   private Http http = null;

   /**
    * @param actorSystem actor system
    */
   public void close(final ActorSystem<Void> actorSystem) {
      binding.thenCompose(ServerBinding::unbind) // trigger unbinding from the port
             .thenAccept(unbound -> actorSystem.terminate()); // and shutdown when done
   }

   protected abstract Route createCorsRoutes(
         ActorSystem<Void> actorSystem,
         ActorRef<BackEnd.Event> backEnd,
         String jsonFields);

   /**
    * @param httpServerHost http server ip
    * @param httpPort       http server port
    * @param actorSystem    actor system
    * @param backEnd        backend
    * @param jsonFields     fields
    */
   public void open(
         final String httpServerHost,
         final int httpPort,
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final String jsonFields) {
      http = Http.get(actorSystem);
      binding = http.newServerAt(httpServerHost, httpPort)
                    .bind(this.createCorsRoutes(actorSystem, backEnd, jsonFields));
      LOGGER.info("Server online at http://{}:{}", httpServerHost, httpPort);
   }

   /*
    *************************** PROXY ***************************
    */

   /**
    * @param body body
    * @return stage
    * @throws JsonProcessingException Json exception
    */
   public CompletionStage<HttpResponse> proxyPostCalculateScores(final CalculateScoresRequest body) throws JsonProcessingException {
      final var request = HttpRequest
            .create("http://linker:50000/JeMPI/calculate-scores")
            .withMethod(HttpMethods.POST)
            .withEntity(ContentTypes.APPLICATION_JSON, AppUtils.OBJECT_MAPPER.writeValueAsBytes(body));
      final var stage = http.singleRequest(request);
      return stage.thenApply(response -> response);
   }

   /*
    *************************** ROUTES ***************************
    */

   private Route mapError(final MpiGeneralError obj) {
      LOGGER.debug("{}", obj);
      return switch (obj) {
         case MpiServiceError.InteractionIdDoesNotExistError e -> complete(StatusCodes.BAD_REQUEST, e, Jackson.marshaller());
         case MpiServiceError.GoldenIdDoesNotExistError e -> complete(StatusCodes.BAD_REQUEST, e, Jackson.marshaller());
         case MpiServiceError.GoldenIdInteractionConflictError e -> complete(StatusCodes.BAD_REQUEST, e, Jackson.marshaller());
         case MpiServiceError.DeletePredicateError e -> complete(StatusCodes.BAD_REQUEST, e, Jackson.marshaller());
         default -> complete(StatusCodes.INTERNAL_SERVER_ERROR);
      };
   }

   /**
    * @param actorSystem actor
    * @param backEnd     backend
    * @param goldenId    golden id
    * @return Route
    */
   public Route routeUpdateGoldenRecordFields(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final String goldenId) {
      return entity(Jackson.unmarshaller(GoldenRecordUpdateRequestPayload.class),
                    payload -> payload != null
                          ? onComplete(Ask.updateGoldenRecordFields(actorSystem, backEnd, goldenId, payload),
                                       result -> {
                                          if (result.isSuccess()) {
                                             final var updatedFields = result.get().fields();
                                             if (updatedFields.size() == 0) {
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

/*
   private Route routeSessionUpdateGoldenRecordFields(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final String goldenId) {
      return requiredSession(refreshable, sessionTransport, session -> {
         if (session != null) {
            LOGGER.info("Current session: " + session.getEmail());
            return routeUpdateGoldenRecordFields(actorSystem, backEnd, goldenId);
         }
         LOGGER.info("No active session");
         return complete(StatusCodes.FORBIDDEN);
      });
   }
*/

   /**
    * @param actorSystem
    * @param backEnd
    * @return
    */
   public Route routeUpdateLinkToNewGoldenRecord(
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
                                                                                          .mapLeft(this::mapError)
                                                                                          .fold(error -> error,
                                                                                                linkInfo -> complete(StatusCodes.OK,
                                                                                                                     linkInfo,
                                                                                                                     Jackson.marshaller()))
                                                                                  : complete(StatusCodes.IM_A_TEAPOT))));
   }

   /**
    * @param actorSystem
    * @param backEnd
    * @return
    */
   public Route routeUpdateLinkToExistingGoldenRecord(
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
                                                                                           .mapLeft(this::mapError)
                                                                                           .fold(error -> error,
                                                                                                 linkInfo -> complete(
                                                                                                       StatusCodes.OK,
                                                                                                       linkInfo,
                                                                                                       Jackson.marshaller()))
                                                                                   : complete(StatusCodes.IM_A_TEAPOT))))));
   }

   /**
    * @param actorSystem
    * @param backEnd
    * @return
    */
   public Route routeGoldenRecordCount(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      return onComplete(Ask.getGoldenRecordCount(actorSystem, backEnd),
                        result -> result.isSuccess()
                              ? result.get()
                                      .count()
                                      .mapLeft(this::mapError)
                                      .fold(error -> error,
                                            count -> complete(StatusCodes.OK,
                                                              new ApiGoldenRecordCount(count),
                                                              Jackson.marshaller()))
                              : complete(StatusCodes.IM_A_TEAPOT));
   }

   /**
    * @param actorSystem
    * @param backEnd
    * @return
    */
   public Route routeInteractionCount(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      return onComplete(Ask.getInteractionCount(actorSystem, backEnd),
                        result -> result.isSuccess()
                              ? result.get()
                                      .count()
                                      .mapLeft(this::mapError)
                                      .fold(error -> error,
                                            count -> complete(StatusCodes.OK,
                                                              new ApiPatientCount(count),
                                                              Jackson.marshaller()))
                              : complete(StatusCodes.IM_A_TEAPOT));
   }

   /**
    * @param actorSystem
    * @param backEnd
    * @return
    */
   public Route routeNumberOfRecords(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      return onComplete(Ask.getNumberOfRecords(actorSystem, backEnd),
                        result -> result.isSuccess()
                              ? complete(StatusCodes.OK,
                                         new ApiNumberOfRecords(result.get().goldenRecords(), result.get().patientRecords()),
                                         Jackson.marshaller())
                              : complete(StatusCodes.IM_A_TEAPOT));
   }

   /**
    * @param actorSystem
    * @param backEnd
    * @return
    */
   public Route routeGoldenIds(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      return onComplete(Ask.getGoldenIds(actorSystem, backEnd),
                        result -> result.isSuccess()
                              ? complete(StatusCodes.OK, result.get(), Jackson.marshaller())
                              : complete(StatusCodes.IM_A_TEAPOT));
   }

   /**
    * @param actorSystem
    * @param backEnd
    * @return
    */
   public Route routeFindMatchesForReview(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      return onComplete(Ask.findMatchesForReview(actorSystem, backEnd),
                        result -> result.isSuccess()
                              ? complete(StatusCodes.OK, result.get(), Jackson.marshaller())
                              : complete(StatusCodes.IM_A_TEAPOT));
   }

   /**
    * @param actorSystem
    * @param backEnd
    * @return
    */
   public Route routeGoldenRecord(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      return parameterList(params -> {
         final var goldenIds = params.stream().map(PARAM_STRING).toList();
         return onComplete(Ask.findExpandedGoldenRecords(actorSystem, backEnd, goldenIds),
                           result -> result.isSuccess()
                                 ? result.get()
                                         .expandedGoldenRecords()
                                         .mapLeft(this::mapError)
                                         .fold(error -> error,
                                               expandedGoldenRecords -> complete(StatusCodes.OK,
                                                                                 expandedGoldenRecords.stream()
                                                                                                      .map(ApiExpandedGoldenRecord::fromExpandedGoldenRecord)
                                                                                                      .toList(),
                                                                                 Jackson.marshaller()))
                                 : complete(StatusCodes.IM_A_TEAPOT));
      });
   }

   /**
    * @param actorSystem
    * @param backEnd
    * @return
    */
   public Route routeExpandedGoldenRecords(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      return parameter("uidList", items -> {
         final var uidList = Stream.of(items.split(",")).map(String::trim).toList();
         return onComplete(
               Ask.findExpandedGoldenRecords(actorSystem, backEnd, uidList),
               result -> result.isSuccess()
                     ? result.get()
                             .expandedGoldenRecords()
                             .mapLeft(this::mapError)
                             .fold(error -> error,
                                   expandedGoldenRecords -> complete(StatusCodes.OK,
                                                                     expandedGoldenRecords.stream()
                                                                                          .map(ApiExpandedGoldenRecord::fromExpandedGoldenRecord)
                                                                                          .toList(),
                                                                     Jackson.marshaller()))
                     : complete(StatusCodes.IM_A_TEAPOT));
      });
   }

   /**
    * @param actorSystem
    * @param backEnd
    * @return
    */
   public Route routeExpandedPatientRecords(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      return parameter("uidList", items -> {
         final var uidList = Stream.of(items.split(",")).map(String::trim).toList();
         return onComplete(Ask.findExpandedPatientRecords(actorSystem, backEnd, uidList),
                           result -> result.isSuccess()
                                 ? result.get()
                                         .expandedPatientRecords()
                                         .mapLeft(this::mapError)
                                         .fold(error -> error,
                                               expandedPatientRecords -> complete(StatusCodes.OK,
                                                                                  expandedPatientRecords.stream()
                                                                                                        .map(ApiExpandedPatientRecord::fromExpandedPatientRecord)
                                                                                                        .toList(),
                                                                                  Jackson.marshaller()))
                                 : complete(StatusCodes.IM_A_TEAPOT));
      });
   }

   /**
    * @param actorSystem
    * @param backEnd
    * @param goldenId
    * @return
    */
   public Route routeFindExpandedGoldenRecord(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final String goldenId) {
      return onComplete(Ask.findExpandedGoldenRecord(actorSystem, backEnd, goldenId),
                        result -> result.isSuccess()
                              ? result.get()
                                      .goldenRecord()
                                      .mapLeft(this::mapError)
                                      .fold(error -> error,
                                            goldenRecord -> complete(StatusCodes.OK,
                                                                     ApiExpandedGoldenRecord.fromExpandedGoldenRecord(goldenRecord),
                                                                     Jackson.marshaller()))
                              : complete(StatusCodes.IM_A_TEAPOT));
   }

/*
   private Route routeSessionFindExpandedGoldenRecord(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final String goldenId) {
      return requiredSession(refreshable,
                             sessionTransport,
                             session -> routeFindExpandedGoldenRecord(actorSystem, backEnd, goldenId));
   }
*/

   /**
    * @param actorSystem
    * @param backEnd
    * @param patientId
    * @return
    */
   public Route routeFindPatientRecord(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final String patientId) {
      return onComplete(Ask.findPatientRecord(actorSystem, backEnd, patientId),
                        result -> result.isSuccess()
                              ? result.get()
                                      .patient()
                                      .mapLeft(this::mapError)
                                      .fold(error -> error,
                                            patientRecord -> complete(StatusCodes.OK,
                                                                      ApiPatientRecord.fromPatientRecord(patientRecord),
                                                                      Jackson.marshaller()))
                              : complete(StatusCodes.IM_A_TEAPOT));
   }

/*
   private Route routeSessionFindPatientRecord(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final String patientId) {
      return requiredSession(refreshable, sessionTransport, session -> routeFindPatientRecord(actorSystem, backEnd, patientId));
   }
*/

/*
   private Route routeGetPatientResource(
           final ActorSystem<Void> actorSystem,
           final ActorRef<BackEnd.Event> backEnd,
           final String patientResourceId) {
      return onComplete(askFindPatientResource(actorSystem, backEnd, patientResourceId),
              result -> result.isSuccess()
                      ? result.get()
                      .patientResource()
                      .mapLeft(this::mapError)
                      .fold(error -> error,
                              patientResource -> complete(StatusCodes.OK,
                                      patientResource
                              ))
                      : complete(StatusCodes.IM_A_TEAPOT));
   }
*/

/*
   private Route routeSessionGetPatientResource(
           final ActorSystem<Void> actorSystem,
           final ActorRef<BackEnd.Event> backEnd,
           final String patientResourceId) {
      return requiredSession(refreshable, sessionTransport, session -> routeGetPatientResource(actorSystem, backEnd,
      patientResourceId));
   }
*/

   /**
    * @param actorSystem
    * @param backEnd
    * @return
    */
   public Route routeFindCandidates(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      return parameter("uid",
                       patientId -> entity(Jackson.unmarshaller(CustomMU.class),
                                           mu -> onComplete(Ask.findCandidates(actorSystem, backEnd, patientId, mu),
                                                            result -> result.isSuccess()
                                                                  ? result.get()
                                                                          .candidates()
                                                                          .mapLeft(this::mapError)
                                                                          .fold(error -> error,
                                                                                candidateList -> complete(StatusCodes.OK,
                                                                                                          candidateList,
                                                                                                          Jackson.marshaller()))
                                                                  : complete(StatusCodes.IM_A_TEAPOT))));
   }

   /**
    * @param actorSystem
    * @param backEnd
    * @return
    */
   public Route routeUpdateNotificationState(
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

/*
   private Route routeLoginWithKeycloakRequest(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final CheckHeader<UserSession> checkHeader) {
      return entity(
            Jackson.unmarshaller(OAuthCodeRequestPayload.class),
            obj -> onComplete(askLoginWithKeycloak(actorSystem, backEnd, obj), response -> {
               if (response.isSuccess()) {
                  final var eventLoginWithKeycloakResponse = response.get();
                  final User user = eventLoginWithKeycloakResponse.user();
                  if (user != null) {
                     return setSession(refreshable,
                                       sessionTransport,
                                       new UserSession(user),
                                       () -> setNewCsrfToken(checkHeader,
                                                             () -> complete(StatusCodes.OK, user, Jackson.marshaller())));
                  } else {
                     return complete(StatusCodes.FORBIDDEN);
                  }
               } else {
                  return complete(StatusCodes.IM_A_TEAPOT);
               }
            }));
   }
*/

/*
   private Route routeCurrentUser() {
      return requiredSession(refreshable, sessionTransport, session -> {
         if (session != null) {
            LOGGER.info("Current session: " + session.getEmail());
            return complete(StatusCodes.OK, session, Jackson.marshaller());
         }
         LOGGER.info("No active session");
         return complete(StatusCodes.FORBIDDEN);
      });
   }
*/

/*
   private Route routeLogout() {
      return requiredSession(refreshable,
                             sessionTransport,
                             session -> invalidateSession(refreshable, sessionTransport, () -> extractRequestContext(ctx -> {
                                LOGGER.info("Logging out {}", session.getUsername());
                                return onSuccess(() -> ctx.completeWith(HttpResponse.create()),
                                                 routeResult -> complete("success"));
                             })));
   }
*/

   /**
    * @param actorSystem
    * @param backEnd
    * @return
    */
   public Route routeUploadCsvFile(
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

/*
   private Route routeSessionUploadCsvFile(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      return withSizeLimit(AppConfig.JEMPI_FILE_IMPORT_MAX_SIZE_BYTE,
                           () -> requiredSession(refreshable, sessionTransport, session -> {
                              if (session != null) {
                                 LOGGER.info("Current session: " + session.getEmail());
                                 return storeUploadedFile("csv",
                                                          (info) -> {
                                                             try {
                                                                LOGGER.debug(GlobalConstants.SEGMENT_UPLOAD);
                                                                return File.createTempFile("import-", ".csv");
                                                             } catch (Exception e) {
                                                                LOGGER.error("error", e);
                                                                return null;
                                                             }
                                                          },
                                                          (info, file) -> onComplete(askUploadCsvFile(actorSystem, backEnd,
                                                                                                      info, file),
                                                                                     response -> response.isSuccess()
                                                                                           ? complete(StatusCodes.OK)
                                                                                           : complete(StatusCodes.IM_A_TEAPOT)));
                              }
                              LOGGER.info("No active session");
                              return complete(StatusCodes.FORBIDDEN);
                           }));
   }
*/

   /**
    * @param actorSystem
    * @param backEnd
    * @param recordType
    * @return
    */
   public Route routeSimpleSearch(
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

/*
   private Route routeSessionSimpleSearch(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final RecordType recordType) {
      return requiredSession(refreshable, sessionTransport, session -> routeSimpleSearch(actorSystem, backEnd, recordType));
   }
*/

   /**
    * @param actorSystem
    * @param backEnd
    * @param recordType
    * @return
    */
   public Route routeCustomSearch(
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

   /**
    * @return Route
    */
   public Route routeCalculateScores() {
      return entity(Jackson.unmarshaller(CalculateScoresRequest.class),
                    obj -> {
                       try {
                          return onComplete(proxyPostCalculateScores(obj),
                                            response -> response.isSuccess()
                                                  ? complete(response.get())
                                                  : complete(StatusCodes.IM_A_TEAPOT));
                       } catch (JsonProcessingException e) {
                          LOGGER.error(e.getLocalizedMessage(), e);
                          return complete(StatusCodes.IM_A_TEAPOT);
                       }
                    });
   }

/*
   private Route routeSessionCustomSearch(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final RecordType recordType) {
      return requiredSession(refreshable, sessionTransport, session -> {
         LOGGER.info("Custom search on {}", recordType);
         // Simple search for golden records
         return routeCustomSearch(actorSystem, backEnd, recordType);
      });
   }
*/


/*
   private Route routeFindPatientRecord(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final String patientId) {
      return onComplete(askFindPatientRecord(actorSystem, backEnd, patientId),
                        result -> result.isSuccess()
                              ? result.get()
                                      .patient()
                                      .mapLeft(this::mapError)
                                      .fold(error -> error,
                                            patientRecord -> complete(StatusCodes.OK,
                                                                      ApiPatientRecord.fromPatientRecord(patientRecord),
                                                                      Jackson.marshaller()))
                              : complete(StatusCodes.IM_A_TEAPOT));
   }
*/


   public interface ApiPaginatedResultSet {
   }

   public record ApiGoldenRecordCount(Long count) {
   }

   public record ApiPatientCount(Long count) {
   }

   @JsonInclude(JsonInclude.Include.NON_NULL)
   public record ApiPagination(@JsonProperty("total") Integer total) {
      static ApiPagination fromLibMPIPagination(final LibMPIPagination pagination) {
         return new ApiPagination(pagination.total());
      }
   }

   public record ApiExpandedGoldenRecordsPaginatedResultSet(
         List<ApiExpandedGoldenRecord> data,
         ApiPagination pagination) implements ApiPaginatedResultSet {
      public static ApiExpandedGoldenRecordsPaginatedResultSet fromLibMPIPaginatedResultSet(
            final LibMPIPaginatedResultSet<ExpandedGoldenRecord> resultSet) {
         final var data = resultSet.data()
                                   .stream()
                                   .map(ApiExpandedGoldenRecord::fromExpandedGoldenRecord)
                                   .toList();
         return new ApiExpandedGoldenRecordsPaginatedResultSet(data, ApiPagination.fromLibMPIPagination(resultSet.pagination()));
      }
   }

   public record ApiPatientRecordsPaginatedResultSet(
         List<ApiPatientRecord> data,
         ApiPagination pagination) implements ApiPaginatedResultSet {
      public static ApiPatientRecordsPaginatedResultSet fromLibMPIPaginatedResultSet(
            final LibMPIPaginatedResultSet<Interaction> resultSet) {
         final var data = resultSet.data()
                                   .stream()
                                   .map(ApiPatientRecord::fromPatientRecord)
                                   .toList();
         return new ApiPatientRecordsPaginatedResultSet(data, ApiPagination.fromLibMPIPagination(resultSet.pagination()));
      }
   }

   @JsonInclude(JsonInclude.Include.NON_NULL)
   public record ApiGoldenRecord(
         String uid,
         List<SourceId> sourceId,
         CustomDemographicData demographicData) {
      static ApiGoldenRecord fromGoldenRecord(final GoldenRecord goldenRecord) {
         return new ApiGoldenRecord(goldenRecord.goldenId(), goldenRecord.sourceId(), goldenRecord.demographicData());
      }
   }

   @JsonInclude(JsonInclude.Include.NON_NULL)
   public record ApiGoldenRecordWithScore(
         ApiGoldenRecord goldenRecord,
         Float score) {
      static ApiGoldenRecordWithScore fromGoldenRecordWithScore(final GoldenRecordWithScore goldenRecordWithScore) {
         return new ApiGoldenRecordWithScore(ApiGoldenRecord.fromGoldenRecord(goldenRecordWithScore.goldenRecord()),
                                             goldenRecordWithScore.score());
      }
   }

   public record ApiExpandedGoldenRecord(
         ApiGoldenRecord goldenRecord,
         List<ApiPatientRecordWithScore> mpiPatientRecords) {
      public static ApiExpandedGoldenRecord fromExpandedGoldenRecord(final ExpandedGoldenRecord expandedGoldenRecord) {
         return new ApiExpandedGoldenRecord(ApiGoldenRecord.fromGoldenRecord(expandedGoldenRecord.goldenRecord()),
                                            expandedGoldenRecord.interactionsWithScore()
                                                                .stream()
                                                                .map(ApiPatientRecordWithScore::fromPatientRecordWithScore)
                                                                .toList());
      }
   }

   public record ApiExpandedPatientRecord(
         ApiPatientRecord patientRecord,
         List<ApiGoldenRecordWithScore> goldenRecordsWithScore) {
      public static ApiExpandedPatientRecord fromExpandedPatientRecord(final ExpandedInteraction expandedInteraction) {
         return new ApiExpandedPatientRecord(ApiPatientRecord.fromPatientRecord(expandedInteraction.interaction()),
                                             expandedInteraction.goldenRecordsWithScore()
                                                                .stream()
                                                                .map(ApiGoldenRecordWithScore::fromGoldenRecordWithScore)
                                                                .toList());
      }
   }

   @JsonInclude(JsonInclude.Include.NON_NULL)
   public record ApiPatientRecord(
         String uid,
         SourceId sourceId,
         CustomDemographicData demographicData) {
      public static ApiPatientRecord fromPatientRecord(final Interaction interaction) {
         return new ApiPatientRecord(interaction.interactionId(), interaction.sourceId(), interaction.demographicData());
      }
   }

   @JsonInclude(JsonInclude.Include.NON_NULL)
   public record ApiPatientRecordWithScore(
         ApiPatientRecord patientRecord,
         Float score) {
      static ApiPatientRecordWithScore fromPatientRecordWithScore(final InteractionWithScore interactionWithScore) {
         return new ApiPatientRecordWithScore(ApiPatientRecord.fromPatientRecord(interactionWithScore.interaction()),
                                              interactionWithScore.score());
      }
   }

   public record ApiNumberOfRecords(
         Long goldenRecords,
         Long patientRecords) {
   }

}
