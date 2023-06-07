package org.jembi.jempi.libapi;


import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;
import akka.http.javadsl.server.directives.FileInfo;
import io.vavr.control.Either;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.libmpi.LibMPI;
import org.jembi.jempi.libmpi.MpiGeneralError;
import org.jembi.jempi.libmpi.MpiServiceError;
import org.jembi.jempi.linker.CustomLinkerProbabilistic;
import org.jembi.jempi.linker.LinkerProbabilistic;
import org.jembi.jempi.shared.models.*;
import org.jembi.jempi.shared.utils.AppUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

public final class BackEnd extends AbstractBehavior<BackEnd.Event> {

   private static final Logger LOGGER = LogManager.getLogger(BackEnd.class);
   private final String pgUser;
   private final String pgPassword;
   private final String pgDatabase;
   private final PsqlNotifications psqlNotifications;
   private LibMPI libMPI = null;
   private String[] dgraphHosts = null;
   private int[] dgraphPorts = null;


   private BackEnd(
         final ActorContext<Event> context,
         final String[] dgraphHosts,
         final int[] dgraphPorts,
         final String sqlUser,
         final String sqlPassword,
         final String sqlDatabase) {
      super(context);
      this.libMPI = null;
      this.dgraphHosts = dgraphHosts;
      this.dgraphPorts = dgraphPorts;
      this.pgUser = sqlUser;
      this.pgPassword = sqlPassword;
      this.pgDatabase = sqlDatabase;
      psqlNotifications = new PsqlNotifications(sqlDatabase);
      openMPI();
   }

   public static Behavior<Event> create(
         final String[] dgraphHosts,
         final int[] dgraphPorts,
         final String sqlUser,
         final String sqlPassword,
         final String sqlDatabase) {
      return Behaviors.setup(context -> new BackEnd(context,
                                                    dgraphHosts,
                                                    dgraphPorts,
                                                    sqlUser,
                                                    sqlPassword,
                                                    sqlDatabase));
   }

   private void openMPI() {
      if (!AppUtils.isNullOrEmpty(Arrays.stream(dgraphHosts).toList())) {
         libMPI = new LibMPI(dgraphHosts, dgraphPorts);
      } else {
         libMPI = new LibMPI(String.format("jdbc:postgresql://postgresql:5432/%s", pgDatabase), pgUser, pgPassword);
      }
   }

   @Override
   public Receive<Event> createReceive() {
      return actor();
   }

   public Receive<Event> actor() {
      ReceiveBuilder<Event> builder = newReceiveBuilder();
      return builder
            .onMessage(GetGoldenRecordCountRequest.class, this::getGoldenRecordCountHandler)
            .onMessage(GetInteractionCountRequest.class, this::getInteractionCountHandler)
            .onMessage(GetNumberOfRecordsRequest.class, this::getNumberOfRecordsHandler)
            .onMessage(GetGoldenIdsRequest.class, this::getGoldenIdsHandler)
            .onMessage(FindExpandedGoldenRecordRequest.class, this::findExpandedGoldenRecordHandler)
            .onMessage(FindExpandedGoldenRecordsRequest.class, this::findExpandedGoldenRecordsHandler)
            .onMessage(FindExpandedPatientRecordsRequest.class, this::findExpandedPatientRecordsHandler)
            .onMessage(FindInteractionRequest.class, this::findInteractionHandler)
            .onMessage(FindCandidatesRequest.class, this::findCandidatesHandler)
            .onMessage(FindMatchesForReviewRequest.class, this::findMatchesForReviewHandler)
            .onMessage(UpdateGoldenRecordFieldsRequest.class, this::updateGoldenRecordFieldsHandler)
            .onMessage(UpdateLinkToExistingGoldenRecordRequest.class, this::updateLinkToExistingGoldenRecordHandler)
            .onMessage(UpdateLinkToNewGoldenRecordRequest.class, this::updateLinkToNewGoldenRecordHandler)
            .onMessage(UpdateNotificationStateRequest.class, this::updateNotificationStateHandler)
            .onMessage(SimpleSearchGoldenRecordsRequest.class, this::simpleSearchGoldenRecordsHandler)
            .onMessage(CustomSearchGoldenRecordsRequest.class, this::customSearchGoldenRecordsHandler)
            .onMessage(SimpleSearchInteractionsRequest.class, this::simpleSearchInteractionsHandler)
            .onMessage(CustomSearchInteractionsRequest.class, this::customSearchInteractionsHandler)
            .onMessage(UploadCsvFileRequest.class, this::uploadCsvFileHandler)
            .build();
   }

   private Behavior<Event> simpleSearchGoldenRecordsHandler(final SimpleSearchGoldenRecordsRequest request) {
      SimpleSearchRequestPayload payload = request.searchRequestPayload();
      List<SimpleSearchRequestPayload.SearchParameter> parameters = payload.parameters();
      Integer offset = payload.offset();
      Integer limit = payload.limit();
      String sortBy = payload.sortBy();
      Boolean sortAsc = payload.sortAsc();
      libMPI.startTransaction();
      var recs = libMPI.simpleSearchGoldenRecords(parameters, offset, limit, sortBy, sortAsc);
      libMPI.closeTransaction();
      request.replyTo.tell(new SearchGoldenRecordsResponse(recs));
      return Behaviors.same();
   }

   private Behavior<Event> customSearchGoldenRecordsHandler(final CustomSearchGoldenRecordsRequest request) {
      CustomSearchRequestPayload payload = request.customSearchRequestPayload();
      List<SimpleSearchRequestPayload> parameters = payload.$or();
      Integer offset = payload.offset();
      Integer limit = payload.limit();
      String sortBy = payload.sortBy();
      Boolean sortAsc = payload.sortAsc();
      libMPI.startTransaction();
      var recs = libMPI.customSearchGoldenRecords(parameters, offset, limit, sortBy, sortAsc);
      libMPI.closeTransaction();
      request.replyTo.tell(new SearchGoldenRecordsResponse(recs));
      return Behaviors.same();
   }

   private Behavior<Event> simpleSearchInteractionsHandler(final SimpleSearchInteractionsRequest request) {
      SimpleSearchRequestPayload payload = request.searchRequestPayload();
      List<SimpleSearchRequestPayload.SearchParameter> parameters = payload.parameters();
      Integer offset = payload.offset();
      Integer limit = payload.limit();
      String sortBy = payload.sortBy();
      Boolean sortAsc = payload.sortAsc();
      libMPI.startTransaction();
      var recs = libMPI.simpleSearchInteractions(parameters, offset, limit, sortBy, sortAsc);
      libMPI.closeTransaction();
      request.replyTo.tell(new SearchInteractionsResponse(recs));
      return Behaviors.same();
   }

   private Behavior<Event> customSearchInteractionsHandler(final CustomSearchInteractionsRequest request) {
      CustomSearchRequestPayload payload = request.customSearchRequestPayload();
      List<SimpleSearchRequestPayload> parameters = payload.$or();
      Integer offset = payload.offset();
      Integer limit = payload.limit();
      String sortBy = payload.sortBy();
      Boolean sortAsc = payload.sortAsc();
      libMPI.startTransaction();
      var recs = libMPI.customSearchInteractions(parameters, offset, limit, sortBy, sortAsc);
      libMPI.closeTransaction();
      request.replyTo.tell(new SearchInteractionsResponse(recs));
      return Behaviors.same();
   }

   private Behavior<Event> findMatchesForReviewHandler(final FindMatchesForReviewRequest request) {
      LOGGER.debug("findMatchesForReviewHandler");
      var recs = psqlNotifications.getMatchesForReview(pgPassword, request.limit(), request.offset(), request.date());
      request.replyTo.tell(new FindMatchesForReviewResponse(recs));
      return Behaviors.same();
   }

   private Behavior<Event> getGoldenRecordCountHandler(final GetGoldenRecordCountRequest request) {
      LOGGER.debug("getGoldenRecordCountHandler");
      try {
         libMPI.startTransaction();
         final long count = libMPI.countGoldenRecords();
         libMPI.closeTransaction();

         request.replyTo.tell(new GetGoldenRecordCountResponse(Either.right(count)));
      } catch (Exception exception) {
         LOGGER.error("libMPI.countGoldenRecords failed with error message: {}", exception.getMessage());
         request.replyTo.tell(new GetGoldenRecordCountResponse(Either.left(new MpiServiceError.GeneralError(exception.getMessage()))));
      }
      return Behaviors.same();
   }

   private Behavior<Event> getInteractionCountHandler(final GetInteractionCountRequest request) {
      LOGGER.debug("etInteractionCountHandler");

      try {
         libMPI.startTransaction();
         final long count = libMPI.countInteractions();
         libMPI.closeTransaction();

         request.replyTo.tell(new GetInteractionCountResponse(Either.right(count)));
      } catch (Exception exception) {
         LOGGER.error("libMPI.countPatientRecords failed with error message: {}", exception.getMessage());
         request.replyTo.tell(new GetInteractionCountResponse(Either.left(new MpiServiceError.GeneralError(
               exception.getMessage()))));
      }
      return Behaviors.same();
   }

   private Behavior<Event> getNumberOfRecordsHandler(final GetNumberOfRecordsRequest request) {
      LOGGER.debug("getNumberOfRecordsHandler");
      libMPI.startTransaction();
      var recs = libMPI.countGoldenRecords();
      var docs = libMPI.countInteractions();
      libMPI.closeTransaction();
      request.replyTo.tell(new GetNumberOfRecordsResponse(recs, docs));
      return Behaviors.same();
   }

   private Behavior<Event> getGoldenIdsHandler(final GetGoldenIdsRequest request) {
      LOGGER.debug("getGoldenIds");
      libMPI.startTransaction();
      var recs = libMPI.findGoldenIds();
      request.replyTo.tell(new GetGoldenIdsResponse(recs));
      libMPI.closeTransaction();
      return Behaviors.same();
   }

   private Behavior<Event> findExpandedGoldenRecordHandler(final FindExpandedGoldenRecordRequest request) {
      ExpandedGoldenRecord expandedGoldenRecord = null;
      LOGGER.debug("findExpandedGoldenRecordHandler");

      try {
         libMPI.startTransaction();
         expandedGoldenRecord = libMPI.findExpandedGoldenRecord(request.goldenId);
         libMPI.closeTransaction();
      } catch (Exception exception) {
         LOGGER.error("libMPI.findExpandedGoldenRecord failed for goldenId: {} with error: {}",
                      request.goldenId,
                      exception.getMessage());
      }

      if (expandedGoldenRecord == null) {
         request.replyTo.tell(new FindExpandedGoldenRecordResponse(Either.left(new MpiServiceError.GoldenIdDoesNotExistError(
               "Golden Record does not exist",
               request.goldenId))));
      } else {
         request.replyTo.tell(new FindExpandedGoldenRecordResponse(Either.right(expandedGoldenRecord)));
      }

      return Behaviors.same();
   }

   private Behavior<Event> findExpandedGoldenRecordsHandler(final FindExpandedGoldenRecordsRequest request) {
      List<ExpandedGoldenRecord> goldenRecords = null;
      LOGGER.debug("getExpandedGoldenRecords");

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
         request.replyTo.tell(new FindExpandedGoldenRecordsResponse(Either.left(new MpiServiceError.GoldenIdDoesNotExistError(
               "Golden Records do not exist",
               Collections.singletonList(request.goldenIds).toString()))));
      } else {
         request.replyTo.tell(new FindExpandedGoldenRecordsResponse(Either.right(goldenRecords)));
      }

      return Behaviors.same();
   }

   private Behavior<Event> findExpandedPatientRecordsHandler(final FindExpandedPatientRecordsRequest request) {
      List<ExpandedInteraction> expandedInteractions = null;
      LOGGER.debug("getExpandedPatients");

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
         request.replyTo.tell(new FindExpandedPatientRecordsResponse(Either.left(new MpiServiceError.InteractionIdDoesNotExistError(
               "Patient Records do not exist",
               Collections.singletonList(request.patientIds).toString()))));
      } else {
         request.replyTo.tell(new FindExpandedPatientRecordsResponse(Either.right(expandedInteractions)));
      }
      return Behaviors.same();
   }

   private Behavior<Event> findInteractionHandler(final FindInteractionRequest request) {
      Interaction interaction = null;
      LOGGER.debug("findPatientRecordHandler");

      try {
         libMPI.startTransaction();
         interaction = libMPI.findInteraction(request.patientId);
         libMPI.closeTransaction();
      } catch (Exception exception) {
         LOGGER.error("libMPI.findPatientRecord failed for patientId: {} with error: {}",
                      request.patientId,
                      exception.getMessage());
      }

      if (interaction == null) {
         request.replyTo.tell(new FindInteractionResponse(Either.left(new MpiServiceError.InteractionIdDoesNotExistError(
               "Patient not found",
               request.patientId))));
      } else {
         request.replyTo.tell(new FindInteractionResponse(Either.right(interaction)));
      }

      return Behaviors.same();
   }

   private Behavior<Event> findCandidatesHandler(final FindCandidatesRequest request) {
      LOGGER.debug("getCandidates");
      LOGGER.debug("{} {}", request.patientId, request.mu);
      Interaction interaction = null;
      List<GoldenRecord> goldenRecords = null;
      List<FindCandidatesResponse.Candidate> candidates = null;

      try {
         libMPI.startTransaction();
         interaction = libMPI.findInteraction(request.patientId);

         try {
            goldenRecords = libMPI.findCandidates(interaction.demographicData());
         } catch (Exception exception) {
            LOGGER.error("libMPI.getCandidates failed to find patientRecord.demographicData: {}",
                         interaction.demographicData());
            request.replyTo.tell(new FindCandidatesResponse(Either.left(new MpiServiceError.CandidatesNotFoundError(
                  "Candidates(golden records) not found with demographic data for patientId",
                  request.patientId))));
            return Behaviors.same();
         }

         libMPI.closeTransaction();

         final var patientDemographic = interaction.demographicData();
         CustomLinkerProbabilistic.updateMU(request.mu);
         LinkerProbabilistic.checkUpdatedMU();
         candidates = goldenRecords
               .stream()
               .map(candidate -> new FindCandidatesResponse.Candidate(candidate,
                                                                      CustomLinkerProbabilistic.probabilisticScore(candidate.demographicData(),
                                                                                                                   patientDemographic)))
               .toList();
         request.replyTo.tell(new FindCandidatesResponse(Either.right(candidates)));
      } catch (Exception exception) {
         LOGGER.error("findCandidatesHandler failed to find patientId: {}", request.patientId);
         request.replyTo.tell(new FindCandidatesResponse(Either.left(new MpiServiceError.InteractionIdDoesNotExistError(
               "Patient not found",
               request.patientId))));
      }

      return Behaviors.same();
   }

   private Behavior<Event> updateGoldenRecordFieldsHandler(final UpdateGoldenRecordFieldsRequest request) {
      final var fields = request.fields();
      final var goldenId = request.goldenId;
      libMPI.startTransaction();
      final var updatedFields = new ArrayList<GoldenRecordUpdateRequestPayload.Field>();
      LOGGER.debug("Golden record {} update.", goldenId);
      for (final GoldenRecordUpdateRequestPayload.Field field : fields) {
         final var result = libMPI.updateGoldenRecordField(goldenId, field.name(), field.value());
         if (result) {
            LOGGER.debug("Golden record field update {} has been successfully updated.", field);
            updatedFields.add(field);
         } else {
            LOGGER.debug("Golden record field update {} update has failed.", field);
         }
      }
      request.replyTo.tell(new UpdateGoldenRecordFieldsResponse(updatedFields));
      libMPI.closeTransaction();
      return Behaviors.same();
   }

   private Behavior<Event> updateLinkToExistingGoldenRecordHandler(final UpdateLinkToExistingGoldenRecordRequest request) {
      var result = libMPI.updateLink(request.currentGoldenId, request.newGoldenId, request.patientId, request.score);
      request.replyTo.tell(new UpdateLinkToExistingGoldenRecordResponse(result));
      return Behaviors.same();
   }

   private Behavior<Event> updateLinkToNewGoldenRecordHandler(final UpdateLinkToNewGoldenRecordRequest request) {
      var linkInfo = libMPI.linkToNewGoldenRecord(request.currentGoldenId, request.patientId, request.score);
      request.replyTo.tell(new UpdateLinkToNewGoldenRecordResponse(linkInfo));
      return Behaviors.same();
   }

   private Behavior<Event> updateNotificationStateHandler(final UpdateNotificationStateRequest request) {
      try {
         psqlNotifications.updateNotificationState(pgPassword, request.notificationId, request.state);
      } catch (SQLException exception) {
         LOGGER.error(exception.getMessage());
      }
      request.replyTo.tell(new UpdateNotificationStateRespnse());
      return Behaviors.same();
   }

   private Behavior<Event> uploadCsvFileHandler(final UploadCsvFileRequest request) throws IOException {
      File file = request.file();
      try {
         Files.copy(file.toPath(), Paths.get("/app/csv/" + file.getName()));
//       final var result = file.delete()
         Files.delete(file.toPath());
         LOGGER.debug("File moved successfully");
      } catch (NoSuchFileException e) {
         LOGGER.debug("No such file");
      } catch (SecurityException | IOException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
      }
      request.replyTo.tell(new UploadCsvFileResponse());
      return Behaviors.same();
   }

   public interface Event {
   }

   public interface EventResponse {
   }

   public record GetGoldenRecordCountRequest(ActorRef<GetGoldenRecordCountResponse> replyTo) implements Event {
   }

   public record GetGoldenRecordCountResponse(Either<MpiGeneralError, Long> count) implements EventResponse {
   }

   public record GetInteractionCountRequest(ActorRef<GetInteractionCountResponse> replyTo) implements Event {
   }

   public record GetInteractionCountResponse(Either<MpiGeneralError, Long> count) implements EventResponse {
   }

   public record GetNumberOfRecordsRequest(ActorRef<GetNumberOfRecordsResponse> replyTo) implements Event {
   }

   public record GetNumberOfRecordsResponse(
         long goldenRecords,
         long patientRecords) implements EventResponse {
   }

   public record GetGoldenIdsRequest(ActorRef<GetGoldenIdsResponse> replyTo) implements Event {
   }

   public record GetGoldenIdsResponse(List<String> records) implements EventResponse {
   }

   public record FindExpandedGoldenRecordRequest(
         ActorRef<FindExpandedGoldenRecordResponse> replyTo,
         String goldenId)
         implements Event {
   }

   public record FindExpandedGoldenRecordResponse(Either<MpiGeneralError, ExpandedGoldenRecord> goldenRecord) implements EventResponse {
   }

   public record FindExpandedGoldenRecordsRequest(
         ActorRef<FindExpandedGoldenRecordsResponse> replyTo,
         List<String> goldenIds) implements Event {
   }

   public record FindExpandedGoldenRecordsResponse(Either<MpiGeneralError, List<ExpandedGoldenRecord>> expandedGoldenRecords)
         implements EventResponse {
   }

   public record FindExpandedPatientRecordsRequest(
         ActorRef<FindExpandedPatientRecordsResponse> replyTo,
         List<String> patientIds) implements Event {
   }

   public record FindExpandedPatientRecordsResponse(Either<MpiGeneralError, List<ExpandedInteraction>> expandedPatientRecords)
         implements EventResponse {
   }

   public record FindInteractionRequest(
         ActorRef<FindInteractionResponse> replyTo,
         String patientId) implements Event {
   }

   public record FindInteractionResponse(Either<MpiGeneralError, Interaction> patient)
         implements EventResponse {
   }

   public record FindMatchesForReviewRequest(
         ActorRef<FindMatchesForReviewResponse> replyTo,
         int limit,
         int offset,
         LocalDate date) implements Event {
   }

   public record FindMatchesForReviewResponse(List<HashMap<String, Object>> records) implements EventResponse {
   }

   public record UpdateGoldenRecordFieldsRequest(
         ActorRef<UpdateGoldenRecordFieldsResponse> replyTo,
         String goldenId,
         List<GoldenRecordUpdateRequestPayload.Field> fields) implements Event {
   }

   public record UpdateGoldenRecordFieldsResponse(List<GoldenRecordUpdateRequestPayload.Field> fields) implements EventResponse {
   }

   public record UpdateLinkToExistingGoldenRecordRequest(
         ActorRef<UpdateLinkToExistingGoldenRecordResponse> replyTo,
         String currentGoldenId,
         String newGoldenId,
         String patientId,
         Float score) implements Event {
   }

   public record UpdateLinkToExistingGoldenRecordResponse(Either<MpiGeneralError, LinkInfo> linkInfo)
         implements EventResponse {
   }

   public record UpdateLinkToNewGoldenRecordRequest(
         ActorRef<UpdateLinkToNewGoldenRecordResponse> replyTo,
         String currentGoldenId,
         String patientId,
         float score) implements Event {
   }

   public record UpdateLinkToNewGoldenRecordResponse(Either<MpiGeneralError, LinkInfo> linkInfo)
         implements EventResponse {
   }

   public record FindCandidatesRequest(
         ActorRef<FindCandidatesResponse> replyTo,
         String patientId,
         CustomMU mu) implements Event {
   }

   public record FindCandidatesResponse(Either<MpiGeneralError, List<Candidate>> candidates) implements EventResponse {
      public record Candidate(
            GoldenRecord goldenRecord,
            float score) {
      }
   }

   public record UpdateNotificationStateRequest(
         ActorRef<UpdateNotificationStateRespnse> replyTo,
         String notificationId,
         String state) implements Event {
   }

   public record UpdateNotificationStateRespnse() implements EventResponse {
   }

   /**
    * Search events
    */
   public record SimpleSearchGoldenRecordsRequest(
         ActorRef<SearchGoldenRecordsResponse> replyTo,
         SimpleSearchRequestPayload searchRequestPayload) implements Event {
   }

   public record CustomSearchGoldenRecordsRequest(
         ActorRef<SearchGoldenRecordsResponse> replyTo,
         CustomSearchRequestPayload customSearchRequestPayload) implements Event {
   }

   public record SearchGoldenRecordsResponse(
         LibMPIPaginatedResultSet<ExpandedGoldenRecord> records) implements EventResponse {
   }

   public record SimpleSearchInteractionsRequest(
         ActorRef<SearchInteractionsResponse> replyTo,
         SimpleSearchRequestPayload searchRequestPayload) implements Event {
   }

   public record CustomSearchInteractionsRequest(
         ActorRef<SearchInteractionsResponse> replyTo,
         CustomSearchRequestPayload customSearchRequestPayload) implements Event {
   }

   public record SearchInteractionsResponse(
         LibMPIPaginatedResultSet<Interaction> records) implements EventResponse {
   }

   public record UploadCsvFileRequest(
         ActorRef<UploadCsvFileResponse> replyTo,
         FileInfo info,
         File file)
         implements Event {
   }

   public record UploadCsvFileResponse() implements EventResponse {
   }

}
