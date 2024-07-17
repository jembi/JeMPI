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
      return builder.onMessage(GetGidsAllRequest.class, this::getGidsAllHandler)
                    .onMessage(GetExpandedGoldenRecordRequest.class, this::getExpandedGoldenRecordHandler)
                    .onMessage(GetExpandedGoldenRecordsRequest.class, this::getExpandedGoldenRecordsHandler)
                    .onMessage(PostGoldenRecordRequest.class, this::postGoldenRecordRequestHandler)
                    .build();
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
         request.replyTo.tell(new GetExpandedGoldenRecordsResponse(Either.left(new MpiServiceError.GoldenIdDoesNotExistError(
                 "Golden Records do not exist",
                 Collections.singletonList(request.goldenIds).toString()))));
      } else {
         request.replyTo.tell(new GetExpandedGoldenRecordsResponse(Either.right(goldenRecords)));
      }
      return Behaviors.same();
   }

   private Behavior<Event> postGoldenRecordRequestHandler(final PostGoldenRecordRequest request) {
      String goldenRecords = null;
      try {
         goldenRecords = libMPI.postGoldenRecord(request.goldenRecord);
      } catch (Exception exception) {
         LOGGER.error("libMPI.postGoldenRecord failed for goldenIds: {} with error: {}",
                 request.goldenRecord,
                 exception.getMessage());
      }
      request.replyTo.tell(new PostGoldenRecordResponse(goldenRecords));
      return Behaviors.same();
   }

   public interface Event {
   }

   public interface EventResponse {
   }

   public record GetGidsAllRequest(ActorRef<GetGidsAllResponse> replyTo) implements Event {
   }

   public record GetGidsAllResponse(List<String> records) implements EventResponse {
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
           List<String> goldenIds) implements Event { }

   public record GetExpandedGoldenRecordsResponse(
           Either<MpiGeneralError, List<ExpandedGoldenRecord>> expandedGoldenRecords) implements EventResponse { }

   public record PostGoldenRecordRequest(
           ActorRef<PostGoldenRecordResponse> replyTo,
           RestoreGoldenRecords goldenRecord) implements Event {
   }

   public record PostGoldenRecordResponse(String goldenRecord)
           implements EventResponse {
   }

}
