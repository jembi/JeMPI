package org.jembi.jempi.libapi;


import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;
import akka.http.javadsl.server.directives.FileInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.vavr.control.Either;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.libmpi.LibMPI;
import org.jembi.jempi.libmpi.MpiGeneralError;
import org.jembi.jempi.libmpi.MpiServiceError;
import org.jembi.jempi.shared.models.*;
import org.jembi.jempi.shared.models.dashboard.NotificationStats;
import org.jembi.jempi.shared.models.dashboard.SQLDashboardData;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.jembi.jempi.shared.utils.AppUtils.OBJECT_MAPPER;

public final class BackEnd extends AbstractBehavior<BackEnd.Event> {

   private static final Logger LOGGER = LogManager.getLogger(BackEnd.class);
   private final String pgIP;
   private final Integer pgPort;
   private final String pgUser;
   private final String pgPassword;
   private final String pgNotificationsDb;
   private final String pgAuditDb;
   private final PsqlNotifications psqlNotifications;
   private final PsqlAuditTrail psqlAuditTrail;
   private LibMPI libMPI = null;
   private String[] dgraphHosts = null;
   private int[] dgraphPorts = null;


   private BackEnd(
         final Level debugLevel,
         final ActorContext<Event> context,
         final String[] dgraphHosts,
         final int[] dgraphPorts,
         final String sqlIP,
         final int sqlPort,
         final String sqlUser,
         final String sqlPassword,
         final String sqlNotificationsDb,
         final String sqlAuditDb,
         final String kafkaBootstrapServers,
         final String kafkaClientId) {
      super(context);
      try {
         this.libMPI = null;
         this.dgraphHosts = dgraphHosts;
         this.dgraphPorts = dgraphPorts;
         this.pgIP = sqlIP;
         this.pgPort = sqlPort;
         this.pgUser = sqlUser;
         this.pgPassword = sqlPassword;
         this.pgNotificationsDb = sqlNotificationsDb;
         this.pgAuditDb = sqlAuditDb;
         psqlNotifications = new PsqlNotifications(sqlIP, sqlPort, sqlNotificationsDb, sqlUser, sqlPassword);
         psqlAuditTrail = new PsqlAuditTrail(sqlIP, sqlPort, sqlAuditDb, sqlUser, sqlPassword);
         openMPI(kafkaBootstrapServers, kafkaClientId, debugLevel);
      } catch (Exception e) {
         LOGGER.error(e.getMessage(), e);
         throw e;
      }

   }

   public static Behavior<Event> create(
         final Level level,
         final String[] dgraphHosts,
         final int[] dgraphPorts,
         final String sqlIP,
         final int sqlPort,
         final String sqlUser,
         final String sqlPassword,
         final String sqlNotificationsDb,
         final String sqlAuditDb,
         final String kafkaBootstrapServers,
         final String kafkaClientId) {
      return Behaviors.setup(context -> new BackEnd(level,
                                                    context,
                                                    dgraphHosts,
                                                    dgraphPorts,
                                                    sqlIP,
                                                    sqlPort,
                                                    sqlUser,
                                                    sqlPassword,
                                                    sqlNotificationsDb,
                                                    sqlAuditDb,
                                                    kafkaBootstrapServers,
                                                    kafkaClientId));
   }

   private void openMPI(
         final String kafkaBootstrapServers,
         final String kafkaClientId,
         final Level debugLevel) {
      libMPI = new LibMPI(debugLevel, dgraphHosts, dgraphPorts, kafkaBootstrapServers, kafkaClientId);
   }

   @Override
   public Receive<Event> createReceive() {
      return actor();
   }

   public Receive<Event> actor() {
      ReceiveBuilder<Event> builder = newReceiveBuilder();
      return builder.onMessage(CountGoldenRecordsRequest.class, this::countGoldenRecordsHandler)
                    .onMessage(CountInteractionsRequest.class, this::countInteractionsHandler)
                    .onMessage(CountRecordsRequest.class, this::countRecordsHandler)
                    .onMessage(FindExpandedSourceIdRequest.class, this::findExpandedSourceIdHandler)
                    .onMessage(GetGidsAllRequest.class, this::getGidsAllHandler)
                    .onMessage(GetGidsPagedRequest.class, this::getGidsPagedHandler)
                    .onMessage(GetInteractionRequest.class, this::getInteractionHandler)
                    .onMessage(GetExpandedInteractionsRequest.class, this::getExpandedInteractionsHandler)
                    .onMessage(GetExpandedGoldenRecordRequest.class, this::getExpandedGoldenRecordHandler)
                    .onMessage(GetExpandedGoldenRecordsRequest.class, this::getExpandedGoldenRecordsHandler)
                    .onMessage(GetGoldenRecordAuditTrailRequest.class, this::getGoldenRecordAuditTrailHandler)
                    .onMessage(GetInteractionAuditTrailRequest.class, this::getInteractionAuditTrailHandler)
                    .onMessage(GetNotificationsRequest.class, this::getNotificationsHandler)
                    .onMessage(PatchGoldenRecordRequest.class, this::patchGoldenRecordHandler)
                    .onMessage(PostIidGidLinkRequest.class, this::postIidGidLinkHandler)
                    .onMessage(PostIidNewGidLinkRequest.class, this::postIidNewGidLinkHandler)
                    .onMessage(PostUpdateNotificationRequest.class, this::postUpdateNotificationHandler)
                    .onMessage(PostSimpleSearchGoldenRecordsRequest.class, this::postSimpleSearchGoldenRecordsHandler)
                    .onMessage(PostCustomSearchGoldenRecordsRequest.class, this::postCustomSearchGoldenRecordsHandler)
                    .onMessage(PostSimpleSearchInteractionsRequest.class, this::postSimpleSearchInteractionsHandler)
                    .onMessage(PostCustomSearchInteractionsRequest.class, this::postCustomSearchInteractionsHandler)
                    .onMessage(PostFilterGidsRequest.class, this::postFilterGidsHandler)
                    .onMessage(PostFilterGidsWithInteractionCountRequest.class, this::postFilterGidsWithInteractionCountHandler)
                    .onMessage(PostUploadCsvFileRequest.class, this::postUploadCsvFileHandler)
                    .onMessage(SQLDashboardDataRequest.class, this::getSqlDashboardDataHandler)
                    .build();
   }

   private Behavior<Event> postSimpleSearchGoldenRecordsHandler(final PostSimpleSearchGoldenRecordsRequest request) {
      ApiModels.ApiSimpleSearchRequestPayload payload = request.searchRequestPayload();
      List<ApiModels.ApiSearchParameter> parameters = payload.parameters();
      Integer offset = payload.offset();
      Integer limit = payload.limit();
      String sortBy = payload.sortBy();
      Boolean sortAsc = payload.sortAsc();
      libMPI.startTransaction();
      var recs = libMPI.simpleSearchGoldenRecords(parameters, offset, limit, sortBy, sortAsc);
      libMPI.closeTransaction();
      request.replyTo.tell(new PostSearchGoldenRecordsResponse(recs));
      return Behaviors.same();
   }

   private Behavior<Event> postCustomSearchGoldenRecordsHandler(final PostCustomSearchGoldenRecordsRequest request) {
      CustomSearchRequestPayload payload = request.customSearchRequestPayload();
      List<ApiModels.ApiSimpleSearchRequestPayload> parameters = payload.$or();
      Integer offset = payload.offset();
      Integer limit = payload.limit();
      String sortBy = payload.sortBy();
      Boolean sortAsc = payload.sortAsc();
      libMPI.startTransaction();
      var recs = libMPI.customSearchGoldenRecords(parameters, offset, limit, sortBy, sortAsc);
      libMPI.closeTransaction();
      request.replyTo.tell(new PostSearchGoldenRecordsResponse(recs));
      return Behaviors.same();
   }

   private Behavior<Event> postSimpleSearchInteractionsHandler(final PostSimpleSearchInteractionsRequest request) {
      final var payload = request.searchRequestPayload();
      List<ApiModels.ApiSearchParameter> parameters = payload.parameters();
      Integer offset = payload.offset();
      Integer limit = payload.limit();
      String sortBy = payload.sortBy();
      Boolean sortAsc = payload.sortAsc();
      libMPI.startTransaction();
      var recs = libMPI.simpleSearchInteractions(parameters, offset, limit, sortBy, sortAsc);
      libMPI.closeTransaction();
      request.replyTo.tell(new PostSearchInteractionsResponse(recs));
      return Behaviors.same();
   }

   private Behavior<Event> postCustomSearchInteractionsHandler(final PostCustomSearchInteractionsRequest request) {
      CustomSearchRequestPayload payload = request.customSearchRequestPayload();
      List<ApiModels.ApiSimpleSearchRequestPayload> parameters = payload.$or();
      Integer offset = payload.offset();
      Integer limit = payload.limit();
      String sortBy = payload.sortBy();
      Boolean sortAsc = payload.sortAsc();
      libMPI.startTransaction();
      var recs = libMPI.customSearchInteractions(parameters, offset, limit, sortBy, sortAsc);
      libMPI.closeTransaction();
      request.replyTo.tell(new PostSearchInteractionsResponse(recs));
      return Behaviors.same();
   }

   private Behavior<Event> postFilterGidsHandler(final PostFilterGidsRequest request) {
      final var payload = request.filterGidsRequestPayload();
      final var parameters = payload.parameters();
      final var createdAt = payload.createdAt();
      Integer offset = payload.offset();
      Integer limit = payload.limit();
      String sortBy = payload.sortBy();
      Boolean sortAsc = payload.sortAsc();
      PaginationOptions paginationOptions = new PaginationOptions(offset, limit, sortBy, sortAsc);
      libMPI.startTransaction();
      var recs = libMPI.filterGids(parameters, createdAt, paginationOptions);
      libMPI.closeTransaction();
      request.replyTo.tell(new PostFilterGidsResponse(recs));
      return Behaviors.same();
   }

   private Behavior<Event> postFilterGidsWithInteractionCountHandler(final PostFilterGidsWithInteractionCountRequest request) {
      FilterGidsRequestPayload payload = request.filterGidsRequestPayload();
      List<ApiModels.ApiSearchParameter> parameters = payload.parameters();
      LocalDateTime createdAt = payload.createdAt();
      Integer offset = payload.offset();
      Integer limit = payload.limit();
      String sortBy = payload.sortBy();
      Boolean sortAsc = payload.sortAsc();
      PaginationOptions paginationOptions = new PaginationOptions(offset, limit, sortBy, sortAsc);
      libMPI.startTransaction();
      var recs = libMPI.filterGidsWithInteractionCount(parameters, createdAt, paginationOptions);
      libMPI.closeTransaction();
      request.replyTo.tell(new PostFilterGidsWithInteractionCountResponse(recs));
      return Behaviors.same();
   }

   private Behavior<Event> getNotificationsHandler(final GetNotificationsRequest request) {
      MatchesForReviewResult result = psqlNotifications.getMatchesForReview(request.limit(),
                                                                            request.offset(),
                                                                            request.startDate(),
                                                                            request.endDate(),
                                                                            request.states());
      request.replyTo.tell(new GetNotificationsResponse(result.getCount(),
                                                        result.getSkippedRecords(),
                                                        result.getNotifications()));
      return Behaviors.same();
   }

   private Behavior<Event> countGoldenRecordsHandler(final CountGoldenRecordsRequest request) {
      try {
         libMPI.startTransaction();
         final long count = libMPI.countGoldenRecords();
         libMPI.closeTransaction();

         request.replyTo.tell(new CountGoldenRecordsResponse(Either.right(count)));
      } catch (Exception exception) {
         LOGGER.error("libMPI.countGoldenRecords failed with error message: {}", exception.getMessage());
         request.replyTo.tell(new CountGoldenRecordsResponse(Either.left(new MpiServiceError.GeneralError(exception.getMessage()))));
      }
      return Behaviors.same();
   }

   private Behavior<Event> countInteractionsHandler(final CountInteractionsRequest request) {
      try {
         libMPI.startTransaction();
         final long count = libMPI.countInteractions();
         libMPI.closeTransaction();

         request.replyTo.tell(new CountInteractionsResponse(Either.right(count)));
      } catch (Exception exception) {
         LOGGER.error("libMPI.countPatientRecords failed with error message: {}", exception.getMessage());
         request.replyTo.tell(new CountInteractionsResponse(Either.left(new MpiServiceError.GeneralError(exception.getMessage()))));
      }
      return Behaviors.same();
   }

   private Behavior<Event> countRecordsHandler(final CountRecordsRequest request) {
      libMPI.startTransaction();
      final var recs = libMPI.countGoldenRecords();
      final var docs = libMPI.countInteractions();
      libMPI.closeTransaction();
      request.replyTo.tell(new CountRecordsResponse(recs, docs));
      return Behaviors.same();
   }

   private Behavior<Event> findExpandedSourceIdHandler(final FindExpandedSourceIdRequest request) {
      libMPI.startTransaction();
      final var sourceIdList = libMPI.findExpandedSourceIdList(request.facility, request.client);
      libMPI.closeTransaction();
      request.replyTo.tell(new FindExpandedSourceIdResponse(sourceIdList));

      return Behaviors.same();
   }

   private Behavior<Event> getGidsAllHandler(final GetGidsAllRequest request) {
      libMPI.startTransaction();
      var recs = libMPI.findGoldenIds();
      request.replyTo.tell(new GetGidsAllResponse(recs));
      libMPI.closeTransaction();
      return Behaviors.same();
   }

   private Behavior<Event> getExpandedGoldenRecordHandler(final GetExpandedGoldenRecordRequest request) {
      ExpandedGoldenRecord expandedGoldenRecord = null;
      try {
         libMPI.startTransaction();
         expandedGoldenRecord = libMPI.findExpandedGoldenRecord(request.goldenId);
         libMPI.closeTransaction();
      } catch (Exception e) {
         LOGGER.error(e.getLocalizedMessage(), e);
         LOGGER.error("libMPI.findExpandedGoldenRecord failed for goldenId: {} with error: {}",
                      request.goldenId,
                      e.getMessage());
      }

      if (expandedGoldenRecord == null) {
         request.replyTo.tell(new GetExpandedGoldenRecordResponse(Either.left(new MpiServiceError.GoldenIdDoesNotExistError(
               "Golden Record does not exist",
               request.goldenId))));
      } else {
         request.replyTo.tell(new GetExpandedGoldenRecordResponse(Either.right(expandedGoldenRecord)));
      }

      return Behaviors.same();
   }

   private Behavior<Event> getExpandedGoldenRecordsHandler(final GetExpandedGoldenRecordsRequest request) {
      List<ExpandedGoldenRecord> goldenRecords = null;
      try {
         libMPI.startTransaction();
         goldenRecords = libMPI.findExpandedGoldenRecords(request.goldenIds);
         libMPI.closeTransaction();
      } catch (Exception exception) {
         LOGGER.error("libMPI.findExpandedGoldenRecords failed for goldenIds: {} with error: {}",
                      request.goldenIds,
                      exception.getMessage());
      }

      if (goldenRecords == null) {
         request.replyTo.tell(new GetExpandedGoldenRecordsResponse(Either.left(new MpiServiceError.GoldenIdDoesNotExistError(
               "Golden Records do not exist",
               Collections.singletonList(request.goldenIds).toString()))));
      } else {
         request.replyTo.tell(new GetExpandedGoldenRecordsResponse(Either.right(goldenRecords)));
      }

      return Behaviors.same();
   }

   private Behavior<Event> getExpandedInteractionsHandler(final GetExpandedInteractionsRequest request) {
      List<ExpandedInteraction> expandedInteractions = null;
      try {
         libMPI.startTransaction();
         expandedInteractions = libMPI.findExpandedInteractions(request.patientIds);
         libMPI.closeTransaction();
      } catch (Exception exception) {
         LOGGER.error("libMPI.findExpandedPatientRecords failed for patientIds: {} with error: {}",
                      request.patientIds,
                      exception.getMessage());
      }

      if (expandedInteractions == null) {
         request.replyTo.tell(new GetExpandedInteractionsResponse(Either.left(new MpiServiceError.InteractionIdDoesNotExistError(
               "Patient Records do not exist",
               Collections.singletonList(request.patientIds).toString()))));
      } else {
         request.replyTo.tell(new GetExpandedInteractionsResponse(Either.right(expandedInteractions)));
      }
      return Behaviors.same();
   }

   private Behavior<Event> getInteractionHandler(final GetInteractionRequest request) {
      Interaction interaction = null;
      try {
         libMPI.startTransaction();
         interaction = libMPI.findInteraction(request.iid);
         libMPI.closeTransaction();
      } catch (Exception exception) {
         LOGGER.error("libMPI.findPatientRecord failed for patientId: {} with error: {}", request.iid, exception.getMessage());
      }

      if (interaction == null) {
         request.replyTo.tell(new GetInteractionResponse(Either.left(new MpiServiceError.InteractionIdDoesNotExistError(
               "Interaction not found",
               request.iid))));
      } else {
         request.replyTo.tell(new GetInteractionResponse(Either.right(interaction)));
      }

      return Behaviors.same();
   }

   private Behavior<Event> patchGoldenRecordHandler(final PatchGoldenRecordRequest request) {
      final var fields = request.fields();
      final var goldenId = request.goldenId;
      libMPI.startTransaction();
      final var updatedFields = new ArrayList<GoldenRecordUpdateRequestPayload.Field>();
      for (final GoldenRecordUpdateRequestPayload.Field field : fields) {
         final var result = libMPI.updateGoldenRecordField(null, goldenId, field.name(), field.oldValue(), field.newValue());
         if (result) {
            updatedFields.add(field);
         } else {
            LOGGER.error("Golden record field update {} update has failed.", field);
         }
      }
      request.replyTo.tell(new PatchGoldenRecordResponse(updatedFields));
      libMPI.closeTransaction();
      return Behaviors.same();
   }

   private Behavior<Event> postIidGidLinkHandler(final PostIidGidLinkRequest request) {
      LOGGER.debug("{} {} {} {}", request.currentGoldenId, request.newGoldenId, request.patientId, request.score);
      final var linkInfo = libMPI.updateLink(request.currentGoldenId, request.newGoldenId, request.patientId, request.score);
      try {
         LOGGER.debug("{}", linkInfo.isLeft()
               ? OBJECT_MAPPER.writeValueAsString(linkInfo.getLeft())
               : OBJECT_MAPPER.writeValueAsString(linkInfo.get()));
      } catch (JsonProcessingException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
      }
      request.replyTo.tell(new PostIidGidLinkResponse(linkInfo));
      return Behaviors.same();
   }

   private Behavior<Event> postIidNewGidLinkHandler(final PostIidNewGidLinkRequest request) {
      LOGGER.debug("{} {} {}", request.currentGoldenId, request.patientId, request.score);
      final var linkInfo = libMPI.linkToNewGoldenRecord(request.currentGoldenId, request.patientId, request.score);
      try {
         LOGGER.debug("{}", linkInfo.isLeft()
               ? OBJECT_MAPPER.writeValueAsString(linkInfo.getLeft())
               : OBJECT_MAPPER.writeValueAsString(linkInfo.get()));
      } catch (JsonProcessingException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
      }
      request.replyTo.tell(new PostIidNewGidLinkResponse(linkInfo));
      return Behaviors.same();
   }

   private Behavior<Event> getGoldenRecordAuditTrailHandler(final GetGoldenRecordAuditTrailRequest request) {
      final var auditTrail = psqlAuditTrail.goldenRecordAuditTrail(request.uid);
      request.replyTo.tell(new GetGoldenRecordAuditTrailResponse(auditTrail));
      return Behaviors.same();
   }

   private Behavior<Event> getInteractionAuditTrailHandler(final GetInteractionAuditTrailRequest request) {
      final var auditTrail = psqlAuditTrail.interactionRecordAuditTrail(request.uid);
      request.replyTo.tell(new GetInteractionAuditTrailResponse(auditTrail));
      return Behaviors.same();
   }

   private Behavior<Event> getGidsPagedHandler(final GetGidsPagedRequest request) {
      libMPI.startTransaction();
      final var recs = libMPI.fetchGoldenIds(request.offset, request.length);
      request.replyTo.tell(new GetGidsPagedResponse(recs));
      libMPI.closeTransaction();
      return Behaviors.same();
   }

   private Behavior<Event> postUpdateNotificationHandler(final PostUpdateNotificationRequest request) {
      try {
         libMPI.startTransaction();
         psqlNotifications.updateNotificationState(request.notificationId, request.oldGoldenId, request.currentGoldenId);
         libMPI.sendUpdatedNotificationEvent(request.notificationId, request.oldGoldenId, request.currentGoldenId);
         libMPI.closeTransaction();
      } catch (SQLException exception) {
         LOGGER.error(exception.getMessage());
      }
      request.replyTo.tell(new PostUpdateNotificationResponse());
      return Behaviors.same();
   }

   private Behavior<Event> postUploadCsvFileHandler(final PostUploadCsvFileRequest request) {
      final File file = request.file();
      try {
         String userCSVPath = System.getenv("UPLOAD_CSV_PATH");
         Files.copy(file.toPath(),
                    Paths.get((userCSVPath != null
                                     ? userCSVPath
                                     : "/app/csv") + "/" + file.getName()));
         Files.delete(file.toPath());
      } catch (NoSuchFileException e) {
         LOGGER.error("No such file");
      } catch (SecurityException | IOException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
      }
      request.replyTo.tell(new PostUploadCsvFileResponse());
      return Behaviors.same();
   }

   private Behavior<Event> getSqlDashboardDataHandler(final SQLDashboardDataRequest request) {
      final int openNotifications = psqlNotifications.getNotificationCount("OPEN");
      final int closedNotifications = psqlNotifications.getNotificationCount("CLOSED");
      request.replyTo.tell(new SQLDashboardDataResponse(new SQLDashboardData(new NotificationStats(openNotifications,
                                                                                                   closedNotifications))));
      return Behaviors.same();
   }

   public interface Event {
   }

   public interface EventResponse {
   }

   public record CountGoldenRecordsRequest(ActorRef<CountGoldenRecordsResponse> replyTo) implements Event {
   }

   public record CountGoldenRecordsResponse(Either<MpiGeneralError, Long> count) implements EventResponse {
   }

   public record CountInteractionsRequest(ActorRef<CountInteractionsResponse> replyTo) implements Event {
   }

   public record CountInteractionsResponse(Either<MpiGeneralError, Long> count) implements EventResponse {
   }

   public record CountRecordsRequest(ActorRef<CountRecordsResponse> replyTo) implements Event {
   }

   public record CountRecordsResponse(
         long goldenRecords,
         long patientRecords) implements EventResponse {
   }

   public record GetGidsPagedRequest(
         ActorRef<GetGidsPagedResponse> replyTo,
         long offset,
         long length) implements Event {
   }

   public record GetGidsPagedResponse(List<String> goldenIds) implements EventResponse {
   }

   public record GetGoldenRecordAuditTrailRequest(
         ActorRef<GetGoldenRecordAuditTrailResponse> replyTo,
         String uid) implements Event {
   }

   public record GetGoldenRecordAuditTrailResponse(List<ApiModels.ApiAuditTrail.LinkingAuditEntry> auditTrail) {
   }

   public record GetInteractionAuditTrailRequest(
         ActorRef<GetInteractionAuditTrailResponse> replyTo,
         String uid) implements Event {
   }

   public record SQLDashboardDataResponse(SQLDashboardData dashboardData) {
   }

   public record SQLDashboardDataRequest(
         ActorRef<SQLDashboardDataResponse> replyTo) implements Event {
   }

   public record GetInteractionAuditTrailResponse(List<ApiModels.ApiAuditTrail.LinkingAuditEntry> auditTrail) {
   }


   public record GetGidsAllRequest(ActorRef<GetGidsAllResponse> replyTo) implements Event {
   }

   public record GetGidsAllResponse(List<String> records) implements EventResponse {
   }

   public record FindExpandedSourceIdRequest(
         ActorRef<FindExpandedSourceIdResponse> replyTo,
         String facility,
         String client) implements Event {
   }

   public record FindExpandedSourceIdResponse(List<ExpandedSourceId> records) implements EventResponse {
   }

   public record GetExpandedGoldenRecordRequest(
         ActorRef<GetExpandedGoldenRecordResponse> replyTo,
         String goldenId) implements Event {
   }

   public record GetExpandedGoldenRecordResponse(Either<MpiGeneralError, ExpandedGoldenRecord> goldenRecord) implements EventResponse {
   }

   public record GetExpandedGoldenRecordsRequest(
         ActorRef<GetExpandedGoldenRecordsResponse> replyTo,
         List<String> goldenIds) implements Event {
   }

   public record GetExpandedGoldenRecordsResponse(Either<MpiGeneralError, List<ExpandedGoldenRecord>> expandedGoldenRecords) implements EventResponse {
   }

   public record GetExpandedInteractionsRequest(
         ActorRef<GetExpandedInteractionsResponse> replyTo,
         List<String> patientIds) implements Event {
   }

   public record GetExpandedInteractionsResponse(Either<MpiGeneralError, List<ExpandedInteraction>> expandedPatientRecords) implements EventResponse {
   }

   public record GetInteractionRequest(
         ActorRef<GetInteractionResponse> replyTo,
         String iid) implements Event {
   }

   public record GetInteractionResponse(Either<MpiGeneralError, Interaction> patient) implements EventResponse {
   }

   public record GetNotificationsRequest(
         ActorRef<GetNotificationsResponse> replyTo,
         int limit,
         int offset,
         Timestamp startDate,
         Timestamp endDate,
         List<String> states) implements Event {
   }

   public record GetNotificationsResponse(
         int count,
         int skippedRecords,
         List<HashMap<String, Object>> records) implements EventResponse {
   }

   public record PatchGoldenRecordRequest(
         ActorRef<PatchGoldenRecordResponse> replyTo,
         String goldenId,
         List<GoldenRecordUpdateRequestPayload.Field> fields) implements Event {
   }

   public record PatchGoldenRecordResponse(List<GoldenRecordUpdateRequestPayload.Field> fields) implements EventResponse {
   }

   public record PostIidGidLinkRequest(
         ActorRef<PostIidGidLinkResponse> replyTo,
         String currentGoldenId,
         String newGoldenId,
         String patientId,
         Float score) implements Event {
   }

   public record PostIidGidLinkResponse(Either<MpiGeneralError, LinkInfo> linkInfo) implements EventResponse {
   }

   public record PostIidNewGidLinkRequest(
         ActorRef<PostIidNewGidLinkResponse> replyTo,
         String currentGoldenId,
         String patientId,
         Float score) implements Event {
   }

   public record PostIidNewGidLinkResponse(Either<MpiGeneralError, LinkInfo> linkInfo) implements EventResponse {
   }

   public record PostUpdateNotificationRequest(
         ActorRef<PostUpdateNotificationResponse> replyTo,
         String notificationId,
         String oldGoldenId,
         String currentGoldenId) implements Event {
   }

   public record PostUpdateNotificationResponse() implements EventResponse {
   }

   /**
    * Search events
    */
   public record PostSimpleSearchGoldenRecordsRequest(
         ActorRef<PostSearchGoldenRecordsResponse> replyTo,
         ApiModels.ApiSimpleSearchRequestPayload searchRequestPayload) implements Event {
   }

   public record PostFilterGidsRequest(
         ActorRef<PostFilterGidsResponse> replyTo,
         FilterGidsRequestPayload filterGidsRequestPayload) implements Event {
   }

   public record PostFilterGidsResponse(
         LibMPIPaginatedResultSet<String> goldenIds) implements EventResponse {
   }

   public record PostFilterGidsWithInteractionCountRequest(
         ActorRef<PostFilterGidsWithInteractionCountResponse> replyTo,
         FilterGidsRequestPayload filterGidsRequestPayload) implements Event {
   }

   public record PostFilterGidsWithInteractionCountResponse(
         PaginatedGIDsWithInteractionCount goldenIds) implements EventResponse {
   }

   public record PostCustomSearchGoldenRecordsRequest(
         ActorRef<PostSearchGoldenRecordsResponse> replyTo,
         CustomSearchRequestPayload customSearchRequestPayload) implements Event {
   }

   public record PostSearchGoldenRecordsResponse(
         LibMPIPaginatedResultSet<ExpandedGoldenRecord> records) implements EventResponse {
   }

   public record PostSimpleSearchInteractionsRequest(
         ActorRef<PostSearchInteractionsResponse> replyTo,
         ApiModels.ApiSimpleSearchRequestPayload searchRequestPayload) implements Event {
   }

   public record PostCustomSearchInteractionsRequest(
         ActorRef<PostSearchInteractionsResponse> replyTo,
         CustomSearchRequestPayload customSearchRequestPayload) implements Event {
   }

   public record PostSearchInteractionsResponse(
         LibMPIPaginatedResultSet<Interaction> records) implements EventResponse {
   }

   public record PostUploadCsvFileRequest(
         ActorRef<PostUploadCsvFileResponse> replyTo,
         FileInfo info,
         File file) implements Event {
   }

   public record PostUploadCsvFileResponse() implements EventResponse {
   }

}
