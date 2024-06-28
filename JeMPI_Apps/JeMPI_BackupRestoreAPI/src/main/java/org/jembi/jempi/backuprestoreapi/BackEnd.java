package org.jembi.jempi.backuprestoreapi;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;
import io.vavr.control.Either;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.libmpi.LibMPI;
import org.jembi.jempi.libmpi.MpiGeneralError;
import org.jembi.jempi.libmpi.MpiServiceError;
import org.jembi.jempi.shared.models.*;

import java.util.Collections;
import java.util.List;

public final class BackEnd extends AbstractBehavior<BackEnd.Event> {

   private static final Logger LOGGER = LogManager.getLogger(BackEnd.class);
   private final String pgIP;
   private final Integer pgPort;
   private final String pgUser;
   private final String pgPassword;
   private final String pgNotificationsDb;
   private final String pgAuditDb;
   private final PsqlNotifications psqlNotifications;
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
                    .onMessage(GetInteractionRequest.class, this::getInteractionHandler)
                    .onMessage(GetExpandedInteractionsRequest.class, this::getExpandedInteractionsHandler)
                    .onMessage(GetExpandedGoldenRecordRequest.class, this::getExpandedGoldenRecordHandler)
                    .onMessage(GetExpandedGoldenRecordsRequest.class, this::getExpandedGoldenRecordsHandler)
                    .build();
   }
   private Behavior<Event> countGoldenRecordsHandler(final CountGoldenRecordsRequest request) {
      try {
         final long count = libMPI.countGoldenRecords();
         request.replyTo.tell(new CountGoldenRecordsResponse(Either.right(count)));
      } catch (Exception exception) {
         LOGGER.error("libMPI.countGoldenRecords failed with error message: {}", exception.getMessage());
         request.replyTo.tell(
               new CountGoldenRecordsResponse(Either.left(new MpiServiceError.GeneralError(exception.getMessage()))));
      }
      return Behaviors.same();
   }

   private Behavior<Event> countInteractionsHandler(final CountInteractionsRequest request) {
      try {
         final long count = libMPI.countInteractions();
         request.replyTo.tell(new CountInteractionsResponse(Either.right(count)));
      } catch (Exception exception) {
         LOGGER.error("libMPI.countPatientRecords failed with error message: {}", exception.getMessage());
         request.replyTo.tell(
               new CountInteractionsResponse(Either.left(new MpiServiceError.GeneralError(exception.getMessage()))));
      }
      return Behaviors.same();
   }

   private Behavior<Event> countRecordsHandler(final CountRecordsRequest request) {
      final var recs = libMPI.countGoldenRecords();
      final var docs = libMPI.countInteractions();
      request.replyTo.tell(new CountRecordsResponse(recs, docs));
      return Behaviors.same();
   }

   private Behavior<Event> findExpandedSourceIdHandler(final FindExpandedSourceIdRequest request) {
      final var sourceIdList = libMPI.findExpandedSourceIdList(request.facility, request.client);
      request.replyTo.tell(new FindExpandedSourceIdResponse(sourceIdList));

      return Behaviors.same();
   }

   private Behavior<Event> getGidsAllHandler(final GetGidsAllRequest request) {
      var recs = libMPI.findGoldenIds();
      request.replyTo.tell(new GetGidsAllResponse(recs));
      return Behaviors.same();
   }

   private Behavior<Event> getExpandedGoldenRecordHandler(final GetExpandedGoldenRecordRequest request) {
      ExpandedGoldenRecord expandedGoldenRecord = null;
      try {
         expandedGoldenRecord = libMPI.findExpandedGoldenRecord(request.goldenId);
      } catch (Exception e) {
         LOGGER.error(e.getLocalizedMessage(), e);
         LOGGER.error("libMPI.findExpandedGoldenRecord failed for goldenId: {} with error: {}",
                      request.goldenId,
                      e.getMessage());
      }

      if (expandedGoldenRecord == null) {
         request.replyTo
               .tell(new GetExpandedGoldenRecordResponse(Either.left(new MpiServiceError.GoldenIdDoesNotExistError(
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
         goldenRecords = libMPI.findExpandedGoldenRecords(request.goldenIds);
      } catch (Exception exception) {
         LOGGER.error("libMPI.findExpandedGoldenRecords failed for goldenIds: {} with error: {}",
                      request.goldenIds,
                      exception.getMessage());
      }

      if (goldenRecords == null) {
         request.replyTo
               .tell(new GetExpandedGoldenRecordsResponse(Either.left(new MpiServiceError.GoldenIdDoesNotExistError(
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
         expandedInteractions = libMPI.findExpandedInteractions(request.patientIds);
      } catch (Exception exception) {
         LOGGER.error("libMPI.findExpandedPatientRecords failed for patientIds: {} with error: {}",
                      request.patientIds,
                      exception.getMessage());
      }

      if (expandedInteractions == null) {
         request.replyTo
               .tell(new GetExpandedInteractionsResponse(Either.left(new MpiServiceError.InteractionIdDoesNotExistError(
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
         interaction = libMPI.findInteraction(request.iid);
      } catch (Exception exception) {
         LOGGER.error("libMPI.findPatientRecord failed for patientId: {} with error: {}", request.iid,
                      exception.getMessage());
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

   public record GetExpandedGoldenRecordResponse(Either<MpiGeneralError, ExpandedGoldenRecord> goldenRecord)
         implements EventResponse {
   }

   public record GetExpandedGoldenRecordsRequest(
         ActorRef<GetExpandedGoldenRecordsResponse> replyTo,
         List<String> goldenIds) implements Event {
   }

   public record GetExpandedGoldenRecordsResponse(
         Either<MpiGeneralError, List<ExpandedGoldenRecord>> expandedGoldenRecords) implements EventResponse {
   }

   public record GetExpandedInteractionsRequest(
         ActorRef<GetExpandedInteractionsResponse> replyTo,
         List<String> patientIds) implements Event {
   }

   public record GetExpandedInteractionsResponse(
         Either<MpiGeneralError, List<ExpandedInteraction>> expandedPatientRecords) implements EventResponse {
   }

   public record GetInteractionRequest(
         ActorRef<GetInteractionResponse> replyTo,
         String iid) implements Event {
   }

   public record GetInteractionResponse(Either<MpiGeneralError, Interaction> patient) implements EventResponse {
   }


}
