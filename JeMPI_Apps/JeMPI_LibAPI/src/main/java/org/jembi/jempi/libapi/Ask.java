package org.jembi.jempi.libapi;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.AskPattern;
import akka.http.javadsl.server.directives.FileInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.shared.models.*;

import java.io.File;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletionStage;

public final class Ask {

   private static final Logger LOGGER = LogManager.getLogger(Ask.class);

   private Ask() {
   }

   static CompletionStage<BackEnd.GetGoldenRecordCountResponse> getGoldenRecordCount(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      CompletionStage<BackEnd.GetGoldenRecordCountResponse> stage = AskPattern
            .ask(backEnd,
                 BackEnd.GetGoldenRecordCountRequest::new,
                 java.time.Duration.ofSeconds(10),
                 actorSystem.scheduler());
      return stage.thenApply(response -> response);
   }

   static CompletionStage<BackEnd.GetInteractionCountResponse> getInteractionCount(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      LOGGER.debug("getInteractionCount");
      CompletionStage<BackEnd.GetInteractionCountResponse> stage = AskPattern
            .ask(backEnd,
                 BackEnd.GetInteractionCountRequest::new,
                 java.time.Duration.ofSeconds(10),
                 actorSystem.scheduler());
      return stage.thenApply(response -> response);
   }

   static CompletionStage<BackEnd.GetNumberOfRecordsResponse> getNumberOfRecords(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      LOGGER.debug("getNumberOfRecords");
      CompletionStage<BackEnd.GetNumberOfRecordsResponse> stage = AskPattern
            .ask(backEnd,
                 BackEnd.GetNumberOfRecordsRequest::new,
                 java.time.Duration.ofSeconds(10),
                 actorSystem.scheduler());
      return stage.thenApply(response -> response);
   }

   static CompletionStage<BackEnd.GetGoldenIdsResponse> getGoldenIds(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      LOGGER.debug("getGoldenIds");
      CompletionStage<BackEnd.GetGoldenIdsResponse> stage = AskPattern
            .ask(backEnd,
                 BackEnd.GetGoldenIdsRequest::new,
                 java.time.Duration.ofSeconds(30),
                 actorSystem.scheduler());
      return stage.thenApply(response -> response);
   }

   static CompletionStage<BackEnd.FindMatchesForReviewResponse> findMatchesForReview(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final int limit,
         final int offset,
         final LocalDate date) {
      CompletionStage<BackEnd.FindMatchesForReviewResponse> stage = AskPattern
            .ask(backEnd,
                 replyTo -> new BackEnd.FindMatchesForReviewRequest(replyTo, limit, offset, date),
                 java.time.Duration.ofSeconds(30),
                 actorSystem.scheduler());
      return stage.thenApply(response -> response);
   }

   static CompletionStage<BackEnd.FindExpandedGoldenRecordResponse> findExpandedGoldenRecord(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final String goldenId) {
      LOGGER.debug("findGoldenRecordById");
      final CompletionStage<BackEnd.FindExpandedGoldenRecordResponse> stage = AskPattern
            .ask(backEnd,
                 replyTo -> new BackEnd.FindExpandedGoldenRecordRequest(replyTo, goldenId),
                 java.time.Duration.ofSeconds(5),
                 actorSystem.scheduler());
      return stage.thenApply(response -> response);
   }

   static CompletionStage<BackEnd.FindInteractionResponse> findPatientRecord(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final String patientId) {
      LOGGER.debug("findPatientRecordById : {}", patientId);
      final CompletionStage<BackEnd.FindInteractionResponse> stage = AskPattern
            .ask(backEnd,
                 replyTo -> new BackEnd.FindInteractionRequest(replyTo, patientId),
                 java.time.Duration.ofSeconds(5),
                 actorSystem.scheduler());
      return stage.thenApply(response -> response);
   }

   static CompletionStage<BackEnd.FindCandidatesResponse> findCandidates(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final String patientId,
         final CustomMU mu) {
      LOGGER.debug("getCandidates");
      CompletionStage<BackEnd.FindCandidatesResponse> stage = AskPattern
            .ask(backEnd,
                 replyTo -> new BackEnd.FindCandidatesRequest(replyTo, patientId, mu),
                 java.time.Duration.ofSeconds(5),
                 actorSystem.scheduler());
      return stage.thenApply(response -> response);
   }

   static CompletionStage<BackEnd.FindExpandedGoldenRecordsResponse> findExpandedGoldenRecords(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final List<String> goldenIds) {
      LOGGER.debug("getExpandedGoldenRecords");
      CompletionStage<BackEnd.FindExpandedGoldenRecordsResponse> stage = AskPattern
            .ask(backEnd,
                 replyTo -> new BackEnd.FindExpandedGoldenRecordsRequest(replyTo, goldenIds),
                 java.time.Duration.ofSeconds(6),
                 actorSystem.scheduler());
      return stage.thenApply(response -> response);
   }

   static CompletionStage<BackEnd.FindExpandedPatientRecordsResponse> findExpandedPatientRecords(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final List<String> uidList) {
      LOGGER.debug("getExpandedPatients");
      CompletionStage<BackEnd.FindExpandedPatientRecordsResponse> stage = AskPattern
            .ask(backEnd,
                 replyTo -> new BackEnd.FindExpandedPatientRecordsRequest(replyTo, uidList),
                 java.time.Duration.ofSeconds(6),
                 actorSystem.scheduler());
      return stage.thenApply(response -> response);
   }

   static CompletionStage<BackEnd.UpdateGoldenRecordFieldsResponse> updateGoldenRecordFields(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final String goldenId,
         final GoldenRecordUpdateRequestPayload payload) {
      LOGGER.debug("updateGoldenRecord");
      CompletionStage<BackEnd.UpdateGoldenRecordFieldsResponse> stage = AskPattern
            .ask(backEnd,
                 replyTo -> new BackEnd.UpdateGoldenRecordFieldsRequest(replyTo, goldenId, payload.fields()),
                 java.time.Duration.ofSeconds(6),
                 actorSystem.scheduler());
      return stage.thenApply(response -> response);
   }

   static CompletionStage<BackEnd.UpdateLinkToExistingGoldenRecordResponse> updateLinkToExistingGoldenRecord(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final String currentGoldenId,
         final String newGoldenId,
         final String patientId,
         final Float score) {
      LOGGER.debug("patchLink");
      final CompletionStage<BackEnd.UpdateLinkToExistingGoldenRecordResponse> stage = AskPattern
            .ask(backEnd,
                 replyTo -> new BackEnd.UpdateLinkToExistingGoldenRecordRequest(replyTo,
                                                                                currentGoldenId,
                                                                                newGoldenId,
                                                                                patientId,
                                                                                score),
                 java.time.Duration.ofSeconds(6),
                 actorSystem.scheduler());
      return stage.thenApply(response -> response);
   }

   static CompletionStage<BackEnd.UpdateLinkToNewGoldenRecordResponse> updateLinkToNewGoldenRecord(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final String currentGoldenId,
         final String patientId) {
      LOGGER.debug("patchUnLink");
      final CompletionStage<BackEnd.UpdateLinkToNewGoldenRecordResponse> stage = AskPattern
            .ask(backEnd,
                 replyTo -> new BackEnd.UpdateLinkToNewGoldenRecordRequest(replyTo, currentGoldenId, patientId, 2.0F),
                 java.time.Duration.ofSeconds(6),
                 actorSystem.scheduler());
      return stage.thenApply(response -> response);
   }

   static CompletionStage<BackEnd.GoldenRecordAuditTrailResponse> goldenRecordAuditTrail(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final String uid) {
      LOGGER.debug("{}", uid);
      final CompletionStage<BackEnd.GoldenRecordAuditTrailResponse> stage = AskPattern
            .ask(backEnd,
                 replyTo -> new BackEnd.GoldenRecordAuditTrailRequest(replyTo, uid),
                 java.time.Duration.ofSeconds(6),
                 actorSystem.scheduler());
      return stage.thenApply(response -> response);
   }

   static CompletionStage<BackEnd.InteractionAuditTrailResponse> interactionAuditTrail(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final String uid) {
      LOGGER.debug("{}", uid);
      final CompletionStage<BackEnd.InteractionAuditTrailResponse> stage = AskPattern
            .ask(backEnd,
                 replyTo -> new BackEnd.InteractionAuditTrailRequest(replyTo, uid),
                 java.time.Duration.ofSeconds(6),
                 actorSystem.scheduler());
      return stage.thenApply(response -> response);
   }

   static CompletionStage<ApiModels.ApiPaginatedResultSet> simpleSearchGoldenRecords(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final SimpleSearchRequestPayload searchRequestPayload) {
      CompletionStage<BackEnd.SearchGoldenRecordsResponse> stage = AskPattern
            .ask(backEnd,
                 replyTo -> new BackEnd.SimpleSearchGoldenRecordsRequest(replyTo, searchRequestPayload),
                 java.time.Duration.ofSeconds(11),
                 actorSystem.scheduler());
      return stage.thenApply(response -> ApiModels.ApiExpandedGoldenRecordsPaginatedResultSet.fromLibMPIPaginatedResultSet(
            response.records()));
   }

   static CompletionStage<ApiModels.ApiPaginatedResultSet> simpleSearchInteractions(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final SimpleSearchRequestPayload simpleSearchRequestPayload) {
      CompletionStage<BackEnd.SearchInteractionsResponse> stage = AskPattern
            .ask(backEnd,
                 replyTo -> new BackEnd.SimpleSearchInteractionsRequest(replyTo, simpleSearchRequestPayload),
                 java.time.Duration.ofSeconds(11),
                 actorSystem.scheduler());
      return stage.thenApply(response -> ApiModels.ApiPatientRecordsPaginatedResultSet.fromLibMPIPaginatedResultSet(response.records()));
   }

   static CompletionStage<ApiModels.ApiPaginatedResultSet> customSearchGoldenRecords(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final CustomSearchRequestPayload customSearchRequestPayload) {
      CompletionStage<BackEnd.SearchGoldenRecordsResponse> stage = AskPattern
            .ask(backEnd,
                 replyTo -> new BackEnd.CustomSearchGoldenRecordsRequest(replyTo, customSearchRequestPayload),
                 java.time.Duration.ofSeconds(11),
                 actorSystem.scheduler());
      return stage.thenApply(response -> ApiModels.ApiExpandedGoldenRecordsPaginatedResultSet.fromLibMPIPaginatedResultSet(
            response.records()));
   }

   static CompletionStage<ApiModels.ApiPaginatedResultSet> customSearchInteractions(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final CustomSearchRequestPayload customSearchRequestPayload) {
      CompletionStage<BackEnd.SearchInteractionsResponse> stage = AskPattern
            .ask(backEnd,
                 replyTo -> new BackEnd.CustomSearchInteractionsRequest(replyTo, customSearchRequestPayload),
                 java.time.Duration.ofSeconds(11),
                 actorSystem.scheduler());
      return stage.thenApply(response -> ApiModels.ApiPatientRecordsPaginatedResultSet.fromLibMPIPaginatedResultSet(response.records()));
   }

   static CompletionStage<BackEnd.UpdateNotificationStateRespnse> updateNotificationState(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final NotificationRequest notificationRequest) {
      CompletionStage<BackEnd.UpdateNotificationStateRespnse> stage = AskPattern
            .ask(backEnd,
                 replyTo -> new BackEnd.UpdateNotificationStateRequest(replyTo,
                                                                       notificationRequest.notificationId(),
                                                                       notificationRequest.state()),
                 java.time.Duration.ofSeconds(11),
                 actorSystem.scheduler());
      return stage.thenApply(response -> response);
   }

   public static CompletionStage<BackEnd.UploadCsvFileResponse> uploadCsvFile(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final FileInfo info,
         final File file) {
      CompletionStage<BackEnd.UploadCsvFileResponse> stage = AskPattern
            .ask(backEnd,
                 replyTo -> new BackEnd.UploadCsvFileRequest(replyTo, info, file),
                 java.time.Duration.ofSeconds(11),
                 actorSystem.scheduler());
      return stage.thenApply(response -> response);
   }

}
