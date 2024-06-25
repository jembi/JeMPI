package org.jembi.jempi.backuprestoreapi;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.AskPattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.shared.models.GlobalConstants;
import org.jembi.jempi.shared.models.ApiModels;

import java.util.concurrent.CompletionStage;

public final class Ask {

   private static final Logger LOGGER = LogManager.getLogger(Ask.class);

   private Ask() {
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

   static CompletionStage<BackEnd.GetExpandedGoldenRecordResponse> getExpandedGoldenRecord(
           final ActorSystem<Void> actorSystem,
           final ActorRef<BackEnd.Event> backEnd,
           final ApiModels.ApiGoldenRecords payload) {
      final CompletionStage<BackEnd.GetExpandedGoldenRecordResponse> stage = AskPattern
              .ask(backEnd,
                      replyTo -> new BackEnd.GetExpandedGoldenRecordRequest(replyTo, payload.gid()),
                      java.time.Duration.ofSeconds(GlobalConstants.TIMEOUT_DGRAPH_QUERY_SECS),
                      actorSystem.scheduler());
      return stage.thenApply(response -> response);
   }

   static CompletionStage<BackEnd.PostGoldenRecordResponse> postGoldenRecord(
           final ActorSystem<Void> actorSystem,
           final ActorRef<BackEnd.Event> backEnd,
           final ApiModels.RestoreGoldenRecord payload) {
      LOGGER.info(".......................ASK......................");
      LOGGER.error(payload);
      CompletionStage<BackEnd.PostGoldenRecordResponse> stage = AskPattern
              .ask(backEnd,
                      replyTo -> new BackEnd.PostGoldenRecordRequest(replyTo, payload),
                      java.time.Duration.ofSeconds(GlobalConstants.TIMEOUT_DGRAPH_QUERY_SECS),
                      actorSystem.scheduler());
      return stage.thenApply(response -> response);
   }
}
