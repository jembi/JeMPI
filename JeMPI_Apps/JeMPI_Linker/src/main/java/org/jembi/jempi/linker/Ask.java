package org.jembi.jempi.linker;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.AskPattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.linker.backend.BackEnd;
import org.jembi.jempi.shared.models.ApiModels;
import org.jembi.jempi.shared.models.InteractionEnvelop;

import java.util.concurrent.CompletionStage;

final class Ask {

   private static final Logger LOGGER = LogManager.getLogger(Ask.class);


   private Ask() {
   }

   static CompletionStage<BackEnd.CrCandidatesResponse> getCrCandidates(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Request> backEnd,
         final ApiModels.ApiCrCandidatesRequest body) {
      CompletionStage<BackEnd.CrCandidatesResponse> stage = AskPattern
            .ask(backEnd,
                 replyTo -> new BackEnd.CrCandidatesRequest(body, replyTo),
                 java.time.Duration.ofSeconds(10),
                 actorSystem.scheduler());
      return stage.thenApply(response -> {
         if (response.goldenRecords().isLeft()) {
            LOGGER.debug("ERROR");
         } else {
            LOGGER.debug("{}", response.goldenRecords().get());
         }
         return response;
      });
   }

   static CompletionStage<BackEnd.CrFindResponse> getCrFind(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Request> backEnd,
         final ApiModels.ApiCrFindRequest body) {
      CompletionStage<BackEnd.CrFindResponse> stage = AskPattern
            .ask(backEnd,
                 replyTo -> new BackEnd.CrFindRequest(body, replyTo),
                 java.time.Duration.ofSeconds(10),
                 actorSystem.scheduler());
      return stage.thenApply(response -> {
         if (response.goldenRecords().isLeft()) {
            LOGGER.debug("ERROR");
         } else {
            LOGGER.debug("{}", response.goldenRecords().get());
         }
         return response;
      });
   }

   static CompletionStage<BackEnd.CrRegisterResponse> postCrRegister(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Request> backEnd,
         final ApiModels.ApiCrRegisterRequest body) {
      final CompletionStage<BackEnd.CrRegisterResponse> stage = AskPattern
            .ask(backEnd,
                 replyTo -> new BackEnd.CrRegisterRequest(body, replyTo),
                 java.time.Duration.ofSeconds(10),
                 actorSystem.scheduler());
      return stage.thenApply(response -> {
         if (response.linkInfo().isLeft()) {
            LOGGER.debug("ERROR");
         } else {
            LOGGER.debug("{}", response.linkInfo().get());
         }
         return response;
      });
   }

   static CompletionStage<BackEnd.CrLinkToGidUpdateResponse> postCrLinkToGidUpdate(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Request> backEnd,
         final ApiModels.ApiCrLinkToGidUpdateRequest body) {
      final CompletionStage<BackEnd.CrLinkToGidUpdateResponse> stage = AskPattern
            .ask(backEnd,
                 replyTo -> new BackEnd.CrLinkToGidUpdateRequest(body, replyTo),
                 java.time.Duration.ofSeconds(10),
                 actorSystem.scheduler());
      return stage.thenApply(response -> {
         if (response.linkInfo().isLeft()) {
            LOGGER.debug("ERROR");
         } else {
            LOGGER.debug("{}", response.linkInfo().get());
         }
         return response;
      });
   }

   static CompletionStage<BackEnd.CrLinkBySourceIdResponse> postCrLinkBySourceId(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Request> backEnd,
         final ApiModels.ApiCrLinkBySourceIdRequest body) {
      final CompletionStage<BackEnd.CrLinkBySourceIdResponse> stage = AskPattern
            .ask(backEnd,
                 replyTo -> new BackEnd.CrLinkBySourceIdRequest(body, replyTo),
                 java.time.Duration.ofSeconds(10),
                 actorSystem.scheduler());
      return stage.thenApply(response -> {
         if (response.linkInfo().isLeft()) {
            LOGGER.debug("ERROR");
         } else {
            LOGGER.debug("{}", response.linkInfo().get());
         }
         return response;
      });
   }

   static CompletionStage<BackEnd.CrLinkBySourceIdUpdateResponse> postCrLinkBySourceIdUpdate(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Request> backEnd,
         final ApiModels.ApiCrLinkBySourceIdUpdateRequest body) {
      final CompletionStage<BackEnd.CrLinkBySourceIdUpdateResponse> stage = AskPattern
            .ask(backEnd,
                 replyTo -> new BackEnd.CrLinkBySourceIdUpdateRequest(body, replyTo),
                 java.time.Duration.ofSeconds(10),
                 actorSystem.scheduler());
      return stage.thenApply(response -> {
         if (response.linkInfo().isLeft()) {
            LOGGER.debug("ERROR");
         } else {
            LOGGER.debug("{}", response.linkInfo().get());
         }
         return response;
      });
   }

   static CompletionStage<BackEnd.CrUpdateFieldResponse> patchCrUpdateField(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Request> backEnd,
         final ApiModels.ApiCrUpdateFieldsRequest body) {
      CompletionStage<BackEnd.CrUpdateFieldResponse> stage = AskPattern
            .ask(backEnd,
                 replyTo -> new BackEnd.CrUpdateFieldRequest(body, replyTo),
                 java.time.Duration.ofSeconds(10),
                 actorSystem.scheduler());
      return stage.thenApply(response -> response);
   }


   static CompletionStage<BackEnd.SyncLinkInteractionResponse> postLinkInteraction(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Request> backEnd,
         final ApiModels.LinkInteractionSyncBody body) {
      CompletionStage<BackEnd.SyncLinkInteractionResponse> stage = AskPattern
            .ask(backEnd,
                 replyTo -> new BackEnd.SyncLinkInteractionRequest(body, replyTo),
                 java.time.Duration.ofSeconds(11),
                 actorSystem.scheduler());
      return stage.thenApply(response -> response);
   }

   static CompletionStage<BackEnd.AsyncLinkInteractionResponse> linkInteraction(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Request> backEnd,
         final String key,
         final InteractionEnvelop batchInteraction) {
      return AskPattern.ask(backEnd,
                            replyTo -> new BackEnd.AsyncLinkInteractionRequest(replyTo, key, batchInteraction),
                            java.time.Duration.ofSeconds(60),
                            actorSystem.scheduler());
   }

   static CompletionStage<BackEnd.FindCandidatesWithScoreResponse> findCandidates(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Request> backEnd,
         final String iid) {
      CompletionStage<BackEnd.FindCandidatesWithScoreResponse> stage = AskPattern
            .ask(backEnd,
                 replyTo -> new BackEnd.FindCandidatesWithScoreRequest(replyTo, iid),
                 java.time.Duration.ofSeconds(5),
                 actorSystem.scheduler());
      return stage.thenApply(response -> response);
   }

/*
   static CompletionStage<BackEnd.SyncLinkInteractionToGidResponse> postLinkPatientToGid(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Request> backEnd,
         final ApiModels.LinkInteractionToGidSyncBody body) {
      CompletionStage<BackEnd.SyncLinkInteractionToGidResponse> stage = AskPattern
            .ask(backEnd,
                 replyTo -> new BackEnd.SyncLinkInteractionToGidRequest(body, replyTo),
                 java.time.Duration.ofSeconds(11),
                 actorSystem.scheduler());
      return stage.thenApply(response -> response);
   }
*/

   static CompletionStage<BackEnd.CalculateScoresResponse> postCalculateScores(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Request> backEnd,
         final ApiModels.ApiCalculateScoresRequest body) {
      CompletionStage<BackEnd.CalculateScoresResponse> stage = AskPattern
            .ask(backEnd,
                 replyTo -> new BackEnd.CalculateScoresRequest(body, replyTo),
                 java.time.Duration.ofSeconds(11),
                 actorSystem.scheduler());
      return stage.thenApply(response -> response);
   }


//   static CompletionStage<BackEnd.EventGetMURsp> getMU(
//         final ActorSystem<Void> actorSystem,
//         final ActorRef<BackEnd.Request> backEnd) {
//      CompletionStage<BackEnd.EventGetMURsp> stage =
//            AskPattern.ask(backEnd, BackEnd.EventGetMUReq::new, java.time.Duration.ofSeconds(11), actorSystem.scheduler());
//      return stage.thenApply(response -> response);
//   }

}
