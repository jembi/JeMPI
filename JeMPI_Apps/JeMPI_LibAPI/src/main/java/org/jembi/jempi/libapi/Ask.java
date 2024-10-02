package org.jembi.jempi.libapi;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.AskPattern;
import akka.http.javadsl.server.directives.FileInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.shared.models.*;
import org.jembi.jempi.shared.models.ConfigurationModel.Configuration;

import java.io.File;
import java.util.List;
import java.util.concurrent.CompletionStage;

public final class Ask {

   private static final Logger LOGGER = LogManager.getLogger(Ask.class);

   private Ask() {
   }

   static CompletionStage<BackEnd.CountGoldenRecordsResponse> countGoldenRecords(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      CompletionStage<BackEnd.CountGoldenRecordsResponse> stage = AskPattern
            .ask(backEnd,
                 BackEnd.CountGoldenRecordsRequest::new,
                 java.time.Duration.ofSeconds(GlobalConstants.TIMEOUT_DGRAPH_QUERY_SECS),
                 actorSystem.scheduler());
      return stage.thenApply(response -> response);
   }

   static CompletionStage<BackEnd.CountInteractionsResponse> countInteractions(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      CompletionStage<BackEnd.CountInteractionsResponse> stage = AskPattern
            .ask(backEnd,
                 BackEnd.CountInteractionsRequest::new,
                 java.time.Duration.ofSeconds(GlobalConstants.TIMEOUT_DGRAPH_QUERY_SECS),
                 actorSystem.scheduler());
      return stage.thenApply(response -> response);
   }

   static CompletionStage<BackEnd.CountRecordsResponse> countRecords(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      CompletionStage<BackEnd.CountRecordsResponse> stage = AskPattern
            .ask(backEnd,
                 BackEnd.CountRecordsRequest::new,
                 java.time.Duration.ofSeconds(GlobalConstants.TIMEOUT_DGRAPH_QUERY_SECS),
                 actorSystem.scheduler());
      return stage.thenApply(response -> response);
   }

   static CompletionStage<BackEnd.GetGidsAllResponse> getGidsAll(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      CompletionStage<BackEnd.GetGidsAllResponse> stage = AskPattern
            .ask(backEnd,
                 BackEnd.GetGidsAllRequest::new,
                 java.time.Duration.ofSeconds(GlobalConstants.TIMEOUT_DGRAPH_QUERY_SECS),
                 actorSystem.scheduler());
      return stage.thenApply(response -> response);
   }

   static CompletionStage<BackEnd.FindExpandedSourceIdResponse> findExpandedSourceId(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final String facility,
         final String client) {
      CompletionStage<BackEnd.FindExpandedSourceIdResponse> stage = AskPattern
            .ask(backEnd,
                 replyTo -> new BackEnd.FindExpandedSourceIdRequest(replyTo, facility, client),
                 java.time.Duration.ofSeconds(GlobalConstants.TIMEOUT_DGRAPH_QUERY_SECS),
                 actorSystem.scheduler());
      return stage.thenApply(response -> response);
   }

   static CompletionStage<BackEnd.GetNotificationsResponse> getNotifications(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final ApiModels.ApiNotifications payload) {
      CompletionStage<BackEnd.GetNotificationsResponse> stage = AskPattern
            .ask(backEnd,
                 replyTo -> new BackEnd.GetNotificationsRequest(replyTo,
                                                                payload.limit(),
                                                                payload.offset(),
                                                                payload.startDate(),
                                                                payload.endDate(),
                                                                payload.states()),
                 java.time.Duration.ofSeconds(GlobalConstants.TIMEOUT_GENERAL_SECS),
                 actorSystem.scheduler());
      return stage.thenApply(response -> response);
   }

   static CompletionStage<BackEnd.GetExpandedGoldenRecordResponse> getExpandedGoldenRecord(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final String gid) {
      final CompletionStage<BackEnd.GetExpandedGoldenRecordResponse> stage = AskPattern
            .ask(backEnd,
                 replyTo -> new BackEnd.GetExpandedGoldenRecordRequest(replyTo, gid),
                 java.time.Duration.ofSeconds(GlobalConstants.TIMEOUT_DGRAPH_QUERY_SECS),
                 actorSystem.scheduler());
      return stage.thenApply(response -> response);
   }

   static CompletionStage<BackEnd.GetInteractionResponse> getInteraction(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final String iid) {
      final CompletionStage<BackEnd.GetInteractionResponse> stage = AskPattern
            .ask(backEnd,
                 replyTo -> new BackEnd.GetInteractionRequest(replyTo, iid),
                 java.time.Duration.ofSeconds(GlobalConstants.TIMEOUT_DGRAPH_QUERY_SECS),
                 actorSystem.scheduler());
      return stage.thenApply(response -> response);
   }

   static CompletionStage<BackEnd.GetExpandedGoldenRecordsResponse> getExpandedGoldenRecords(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final List<String> gidList) {
      CompletionStage<BackEnd.GetExpandedGoldenRecordsResponse> stage = AskPattern
            .ask(backEnd,
                 replyTo -> new BackEnd.GetExpandedGoldenRecordsRequest(replyTo, gidList),
                 java.time.Duration.ofSeconds(GlobalConstants.TIMEOUT_DGRAPH_QUERY_SECS),
                 actorSystem.scheduler());
      return stage.thenApply(response -> response);
   }

   static CompletionStage<BackEnd.GetExpandedInteractionsResponse> getExpandedInteractions(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final List<String> uidList) {
      CompletionStage<BackEnd.GetExpandedInteractionsResponse> stage = AskPattern
            .ask(backEnd,
                 replyTo -> new BackEnd.GetExpandedInteractionsRequest(replyTo, uidList),
                 java.time.Duration.ofSeconds(GlobalConstants.TIMEOUT_DGRAPH_QUERY_SECS),
                 actorSystem.scheduler());
      return stage.thenApply(response -> response);
   }

   static CompletionStage<BackEnd.UpdateGoldenRecordResponse> updateGoldenRecord(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final GoldenRecordUpdateRequestPayload payload) {
      CompletionStage<BackEnd.UpdateGoldenRecordResponse> stage = AskPattern
            .ask(backEnd,
                 replyTo -> new BackEnd.UpdateGoldenRecordRequest(replyTo, payload.goldenId(), payload.fields()),
                 java.time.Duration.ofSeconds(GlobalConstants.TIMEOUT_DGRAPH_QUERY_SECS),
                 actorSystem.scheduler());
      return stage.thenApply(response -> response);
   }

   static CompletionStage<BackEnd.PostIidGidLinkResponse> postIidGidLink(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final String currentGoldenId,
         final String newGoldenId,
         final String patientId,
         final Float score) {
      LOGGER.debug("{} {} {} {}", currentGoldenId, newGoldenId, patientId, score);
      final CompletionStage<BackEnd.PostIidGidLinkResponse> stage = AskPattern
            .ask(backEnd,
                 replyTo -> new BackEnd.PostIidGidLinkRequest(replyTo, currentGoldenId, newGoldenId, patientId, score),
                 java.time.Duration.ofSeconds(GlobalConstants.TIMEOUT_DGRAPH_QUERY_SECS),
                 actorSystem.scheduler());
      return stage.thenApply(response -> response);
   }

   static CompletionStage<BackEnd.PostIidNewGidLinkResponse> postIidNewGidLink(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final String currentGoldenId,
         final String patientId) {
      final CompletionStage<BackEnd.PostIidNewGidLinkResponse> stage = AskPattern
            .ask(backEnd,
                 replyTo -> new BackEnd.PostIidNewGidLinkRequest(replyTo, currentGoldenId, patientId, 2.0F),
                 java.time.Duration.ofSeconds(GlobalConstants.TIMEOUT_DGRAPH_QUERY_SECS),
                 actorSystem.scheduler());
      return stage.thenApply(response -> response);
   }

   static CompletionStage<BackEnd.SQLDashboardDataResponse> getSQLDashboardData(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd
                                                                               ) {
      final CompletionStage<BackEnd.SQLDashboardDataResponse> stage = AskPattern
            .ask(backEnd,
                 BackEnd.SQLDashboardDataRequest::new,
                 java.time.Duration.ofSeconds(GlobalConstants.TIMEOUT_GENERAL_SECS),
                 actorSystem.scheduler());
      return stage.thenApply(response -> response);
   }

   static CompletionStage<BackEnd.GetGidsPagedResponse> getGidsPaged(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final long offset,
         final long length) {
      final CompletionStage<BackEnd.GetGidsPagedResponse> stage = AskPattern
            .ask(backEnd,
                 replyTo -> new BackEnd.GetGidsPagedRequest(replyTo, offset, length),
                 java.time.Duration.ofSeconds(GlobalConstants.TIMEOUT_DGRAPH_QUERY_SECS),
                 actorSystem.scheduler());
      return stage.thenApply(response -> response);
   }

   static CompletionStage<BackEnd.GetGoldenRecordAuditTrailResponse> getGoldenRecordAuditTrail(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final String gid) {
      final CompletionStage<BackEnd.GetGoldenRecordAuditTrailResponse> stage = AskPattern
            .ask(backEnd,
                 replyTo -> new BackEnd.GetGoldenRecordAuditTrailRequest(replyTo, gid),
                 java.time.Duration.ofSeconds(GlobalConstants.TIMEOUT_GENERAL_SECS),
                 actorSystem.scheduler());
      return stage.thenApply(response -> response);
   }

   static CompletionStage<BackEnd.GetInteractionAuditTrailResponse> getInteractionAuditTrail(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final String uid) {
      final CompletionStage<BackEnd.GetInteractionAuditTrailResponse> stage = AskPattern
            .ask(backEnd,
                 replyTo -> new BackEnd.GetInteractionAuditTrailRequest(replyTo, uid),
                 java.time.Duration.ofSeconds(GlobalConstants.TIMEOUT_GENERAL_SECS),
                 actorSystem.scheduler());
      return stage.thenApply(response -> response);
   }

   static CompletionStage<ApiModels.ApiPaginatedResultSet> postSimpleSearchGoldenRecords(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final ApiModels.ApiSimpleSearchRequestPayload searchRequestPayload) {
      CompletionStage<BackEnd.PostSearchGoldenRecordsResponse> stage = AskPattern
            .ask(backEnd,
                 replyTo -> new BackEnd.PostSimpleSearchGoldenRecordsRequest(replyTo, searchRequestPayload),
                 java.time.Duration.ofSeconds(GlobalConstants.TIMEOUT_DGRAPH_QUERY_SECS),
                 actorSystem.scheduler());
      return stage.thenApply(response -> ApiModels.ApiExpandedGoldenRecordsPaginatedResultSet.fromLibMPIPaginatedResultSet(
            response.records()));
   }

   static CompletionStage<ApiModels.ApiPaginatedResultSet> postFilterGids(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final FilterGidsRequestPayload filterRequestPayload) {
      CompletionStage<BackEnd.PostFilterGidsResponse> stage = AskPattern
            .ask(backEnd,
                 replyTo -> new BackEnd.PostFilterGidsRequest(replyTo, filterRequestPayload),
                 java.time.Duration.ofSeconds(GlobalConstants.TIMEOUT_DGRAPH_QUERY_SECS),
                 actorSystem.scheduler());
      return stage.thenApply(response -> ApiModels.ApiFilteredGidsPaginatedResultSet.fromLibMPIPaginatedResultSet(response.goldenIds()));
   }

   static CompletionStage<ApiModels.ApiPaginatedResultSet> postFilterGidsWithInteractionCount(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final FilterGidsRequestPayload filterRequestPayload) {
      CompletionStage<BackEnd.PostFilterGidsWithInteractionCountResponse> stage = AskPattern
            .ask(backEnd,
                 replyTo -> new BackEnd.PostFilterGidsWithInteractionCountRequest(replyTo, filterRequestPayload),
                 java.time.Duration.ofSeconds(GlobalConstants.TIMEOUT_DGRAPH_QUERY_SECS),
                 actorSystem.scheduler());
      return stage.thenApply(response -> ApiModels.ApiFilteredGidsWithInteractionCountPaginatedResultSet.fromPaginatedGidsWithInteractionCount(
            response.goldenIds()));
   }

   static CompletionStage<ApiModels.ApiPaginatedResultSet> postSimpleSearchInteractions(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final ApiModels.ApiSimpleSearchRequestPayload simpleSearchRequestPayload) {
      CompletionStage<BackEnd.PostSearchInteractionsResponse> stage = AskPattern
            .ask(backEnd,
                 replyTo -> new BackEnd.PostSimpleSearchInteractionsRequest(replyTo, simpleSearchRequestPayload),
                 java.time.Duration.ofSeconds(GlobalConstants.TIMEOUT_DGRAPH_QUERY_SECS),
                 actorSystem.scheduler());
      return stage.thenApply(response -> ApiModels.ApiInteractionsPaginatedResultSet.fromLibMPIPaginatedResultSet(response.records()));
   }

   static CompletionStage<ApiModels.ApiPaginatedResultSet> postCustomSearchGoldenRecords(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final CustomSearchRequestPayload customSearchRequestPayload) {
      CompletionStage<BackEnd.PostSearchGoldenRecordsResponse> stage = AskPattern
            .ask(backEnd,
                 replyTo -> new BackEnd.PostCustomSearchGoldenRecordsRequest(replyTo, customSearchRequestPayload),
                 java.time.Duration.ofSeconds(GlobalConstants.TIMEOUT_DGRAPH_QUERY_SECS),
                 actorSystem.scheduler());
      return stage.thenApply(response -> ApiModels.ApiExpandedGoldenRecordsPaginatedResultSet.fromLibMPIPaginatedResultSet(
            response.records()));
   }

   static CompletionStage<ApiModels.ApiPaginatedResultSet> postCustomSearchInteractions(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final CustomSearchRequestPayload customSearchRequestPayload) {
      CompletionStage<BackEnd.PostSearchInteractionsResponse> stage = AskPattern
            .ask(backEnd,
                 replyTo -> new BackEnd.PostCustomSearchInteractionsRequest(replyTo, customSearchRequestPayload),
                 java.time.Duration.ofSeconds(GlobalConstants.TIMEOUT_DGRAPH_QUERY_SECS),
                 actorSystem.scheduler());
      return stage.thenApply(response -> ApiModels.ApiInteractionsPaginatedResultSet.fromLibMPIPaginatedResultSet(response.records()));
   }

   static CompletionStage<BackEnd.PostUpdateNotificationResponse> postUpdateNotification(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final NotificationRequest notificationRequest) {
      CompletionStage<BackEnd.PostUpdateNotificationResponse> stage = AskPattern
            .ask(backEnd,
                 replyTo -> new BackEnd.PostUpdateNotificationRequest(replyTo,
                                                                      notificationRequest.notificationId(),
                                                                      notificationRequest.oldGoldenId(),
                                                                      notificationRequest.currentGoldenId()),
                 java.time.Duration.ofSeconds(GlobalConstants.TIMEOUT_DGRAPH_QUERY_SECS),
                 actorSystem.scheduler());
      return stage.thenApply(response -> response);
   }

   static CompletionStage<BackEnd.GetConfigurationResponse> getConfiguration(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      CompletionStage<BackEnd.GetConfigurationResponse> stage = AskPattern
            .ask(backEnd,
                 BackEnd.GetConfigurationRequest::new,
                 java.time.Duration.ofSeconds(GlobalConstants.TIMEOUT_GENERAL_SECS),
                 actorSystem.scheduler());
      return stage.thenApply(response -> response);
   }

   static CompletionStage<BackEnd.GetAgeGroupCountResponse> getAgeGroupCount(
           final ActorSystem<Void> actorSystem,
           final ActorRef<BackEnd.Event> backEnd,
           final ApiModels.SearchAgeCountFields searchAgeCountFields) {
      final CompletionStage<BackEnd.GetAgeGroupCountResponse> stage = AskPattern
              .ask(backEnd,
                      replyTo -> new BackEnd.GetAgeGroupCountRequest(replyTo, searchAgeCountFields),
                      java.time.Duration.ofSeconds(GlobalConstants.TIMEOUT_DGRAPH_QUERY_SECS),
                      actorSystem.scheduler());
      return stage.thenApply(response -> response);
   }

   static CompletionStage<BackEnd.GetFieldCountResponse> getFieldCount(
           final ActorSystem<Void> actorSystem,
           final ActorRef<BackEnd.Event> backEnd,
           final ApiModels.CountFields countFields) {
      final CompletionStage<BackEnd.GetFieldCountResponse> stage = AskPattern
              .ask(backEnd,
                      replyTo -> new BackEnd.GetFieldCountRequest(replyTo, countFields),
                      java.time.Duration.ofSeconds(GlobalConstants.TIMEOUT_DGRAPH_QUERY_SECS),
                      actorSystem.scheduler());
      return stage.thenApply(response -> response);
   }

   static CompletionStage<BackEnd.GetFieldsConfigurationResponse> getFieldsConfiguration(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      CompletionStage<BackEnd.GetFieldsConfigurationResponse> stage = AskPattern
            .ask(backEnd,
                 BackEnd.GetFieldsConfigurationRequest::new,
                 java.time.Duration.ofSeconds(GlobalConstants.TIMEOUT_GENERAL_SECS),
                 actorSystem.scheduler());
      return stage.thenApply(response -> response);
   }

   static CompletionStage<BackEnd.PostConfigurationResponse> postConfiguration(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final Configuration configuration) {
      CompletionStage<BackEnd.PostConfigurationResponse> stage = AskPattern
            .ask(backEnd,
                 replyTo -> new BackEnd.PostConfigurationRequest(replyTo,
                                                                 configuration),
                 java.time.Duration.ofSeconds(GlobalConstants.TIMEOUT_GENERAL_SECS),
                 actorSystem.scheduler());
      return stage.thenApply(response -> response);
   }

   public static CompletionStage<BackEnd.PostUploadCsvFileResponse> postUploadCsvFile(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final FileInfo info,
         final File file,
         final UploadConfig uploadConfig) {
      CompletionStage<BackEnd.PostUploadCsvFileResponse> stage = AskPattern
            .ask(backEnd,
                 replyTo -> new BackEnd.PostUploadCsvFileRequest(replyTo, info, file, uploadConfig),
                 java.time.Duration.ofSeconds(GlobalConstants.TIMEOUT_GENERAL_SECS),
                 actorSystem.scheduler());
      return stage.thenApply(response -> response);
   }

}
