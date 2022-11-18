package org.jembi.jempi.api;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.AskPattern;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.StatusCode;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.AppConfig;
import org.jembi.jempi.libmpi.MpiGeneralError;
import org.jembi.jempi.libmpi.MpiServiceError;
import org.jembi.jempi.shared.models.CustomMU;

import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.stream.Stream;

public class HttpServer extends AllDirectives {

   private static final Logger LOGGER = LogManager.getLogger(HttpServer.class);

   private static final Function<Entry<String, String>, String> paramString = Entry::getValue;
   private CompletionStage<ServerBinding> binding = null;

   void close(ActorSystem<Void> actorSystem) {
      binding.thenCompose(ServerBinding::unbind) // trigger unbinding from the port
             .thenAccept(unbound -> actorSystem.terminate()); // and shutdown when done
   }

   void open(final ActorSystem<Void> actorSystem, final ActorRef<BackEnd.Event> backEnd) {
      final Http http = Http.get(actorSystem);
      binding = http.newServerAt(AppConfig.HTTP_SERVER_HOST, AppConfig.HTTP_SERVER_PORT)
                    .bind(this.createRoute(actorSystem, backEnd));
      LOGGER.info("Server online at http://{}:{}", AppConfig.HTTP_SERVER_HOST, AppConfig.HTTP_SERVER_PORT);
   }

   private CompletionStage<BackEnd.EventGetGoldenRecordCountRsp> getGoldenRecordCount(final ActorSystem<Void> actorSystem,
                                                                                      final ActorRef<BackEnd.Event> backEnd) {
      CompletionStage<BackEnd.EventGetGoldenRecordCountRsp> stage =
            AskPattern.ask(backEnd,
                           BackEnd.EventGetGoldenRecordCountReq::new,
                           java.time.Duration.ofSeconds(10),
                           actorSystem.scheduler());
      return stage.thenApply(response -> response);
   }



   private CompletionStage<BackEnd.EventGetDocumentCountRsp> getDocumentCount(final ActorSystem<Void> actorSystem,
                                                                              final ActorRef<BackEnd.Event> backEnd) {
      LOGGER.debug("getDocumentCount");
      CompletionStage<BackEnd.EventGetDocumentCountRsp> stage =
            AskPattern.ask(backEnd,
                           BackEnd.EventGetDocumentCountReq::new,
                           java.time.Duration.ofSeconds(10),
                           actorSystem.scheduler());
      return stage.thenApply(response -> response);
   }


   private CompletionStage<BackEnd.EventGetNumberOfRecordsRsp> getNumberOfRecords(final ActorSystem<Void> actorSystem,
                                                                                  final ActorRef<BackEnd.Event> backEnd) {
      LOGGER.debug("getNumberOfRecords");
      CompletionStage<BackEnd.EventGetNumberOfRecordsRsp> stage =
            AskPattern.ask(
                  backEnd,
                  BackEnd.EventGetNumberOfRecordsReq::new,
                  java.time.Duration.ofSeconds(10),
                  actorSystem.scheduler());
      return stage.thenApply(response -> response);
   }

   private CompletionStage<BackEnd.EventGetGoldenIdListByPredicateRsp> getGoldenIdListByPredicate(
         final ActorSystem<Void> actorSystem, final ActorRef<BackEnd.Event> backEnd,
         final String predicate,
         final String val) {
      LOGGER.debug("getGoldenIdListByPredicate");
      CompletionStage<BackEnd.EventGetGoldenIdListByPredicateRsp> stage =
            AskPattern.ask(backEnd,
                           replyTo -> new BackEnd.EventGetGoldenIdListByPredicateReq(replyTo, predicate, val),
                           java.time.Duration.ofSeconds(20),
                           actorSystem.scheduler());
      return stage.thenApply(response -> response);
   }

   private CompletionStage<BackEnd.EventGetGoldenIdListRsp> getGoldenIdList(final ActorSystem<Void> actorSystem,
                                                                            final ActorRef<BackEnd.Event> backEnd) {
      LOGGER.debug("getGoldenIdList");
      CompletionStage<BackEnd.EventGetGoldenIdListRsp> stage =
            AskPattern.ask(backEnd,
                           BackEnd.EventGetGoldenIdListReq::new,
                           java.time.Duration.ofSeconds(30),
                           actorSystem.scheduler());
      return stage.thenApply(response -> response);
   }


   //Mahao
   private CompletionStage<BackEnd.EventGetMatchesForReviewListRsp> getMatchesForReviewList(final ActorSystem<Void> actorSystem,
                                                                                    final ActorRef<BackEnd.Event> backEnd) {
      CompletionStage<BackEnd.EventGetMatchesForReviewListRsp> stage =
              AskPattern.ask(backEnd,
                      BackEnd.EventGetMatchesForReviewReq::new,
                      java.time.Duration.ofSeconds(30),
                      actorSystem.scheduler());
      return stage.thenApply(response -> response);
   }
   private CompletionStage<BackEnd.EventGetGoldenRecordRsp> getGoldenRecord(final ActorSystem<Void> actorSystem,
                                                                            final ActorRef<BackEnd.Event> backEnd,
                                                                            final String uid) {
      LOGGER.debug("getGoldenRecord");
      final CompletionStage<BackEnd.EventGetGoldenRecordRsp> stage =
            AskPattern.ask(backEnd,
                           replyTo -> new BackEnd.EventGetGoldenRecordReq(replyTo, uid),
                           java.time.Duration.ofSeconds(5),
                           actorSystem.scheduler());
      return stage.thenApply(response -> response);
   }

   private CompletionStage<BackEnd.EventGetCandidatesRsp> getCandidates(final ActorSystem<Void> actorSystem,
                                                                        final ActorRef<BackEnd.Event> backEnd,
                                                                        final String uid, final CustomMU mu) {
      LOGGER.debug("getCandidates");
      CompletionStage<BackEnd.EventGetCandidatesRsp> stage =
            AskPattern.ask(backEnd,
                           replyTo -> new BackEnd.EventGetCandidatesReq(replyTo, uid, mu),
                           java.time.Duration.ofSeconds(5),
                           actorSystem.scheduler());
      return stage.thenApply(response -> response);
   }

   private CompletionStage<BackEnd.EventGetGoldenRecordDocumentsRsp> getGoldenRecordDocuments(final ActorSystem<Void> actorSystem,
                                                                                              final ActorRef<BackEnd.Event> backEnd,
                                                                                              final List<String> uid) {
      LOGGER.debug("getGoldenRecordDocuments");
      CompletionStage<BackEnd.EventGetGoldenRecordDocumentsRsp> stage =
            AskPattern.ask(backEnd,
                           replyTo -> new BackEnd.EventGetGoldenRecordDocumentsReq(replyTo, uid),
                           java.time.Duration.ofSeconds(6),
                           actorSystem.scheduler());
      return stage.thenApply(response -> response);
   }

   private CompletionStage<BackEnd.EventPatchGoldenRecordPredicateRsp> patchGoldenRecordPredicate(
         final ActorSystem<Void> actorSystem, final ActorRef<BackEnd.Event> backEnd,
         final String uid,
         final String predicate,
         final String value) {
      LOGGER.debug("patchGoldenRecordPredicate");
      CompletionStage<BackEnd.EventPatchGoldenRecordPredicateRsp> stage =
            AskPattern.ask(backEnd,
                           replyTo -> new BackEnd.EventPatchGoldenRecordPredicateReq(replyTo, uid, predicate, value),
                           java.time.Duration.ofSeconds(6),
                           actorSystem.scheduler());
      return stage.thenApply(response -> response);
   }

   private CompletionStage<BackEnd.EventPatchLinkRsp> patchLink(final ActorSystem<Void> actorSystem,
                                                                final ActorRef<BackEnd.Event> backEnd,
                                                                final String goldenID,
                                                                final String newGoldenID,
                                                                final String docID,
                                                                final Float score) {
      LOGGER.debug("patchLink");
      final CompletionStage<BackEnd.EventPatchLinkRsp> stage =
            AskPattern.ask(backEnd,
                           replyTo -> new BackEnd.EventPatchLinkReq(replyTo, goldenID, newGoldenID, docID, score),
                           java.time.Duration.ofSeconds(6),
                           actorSystem.scheduler());
      return stage.thenApply(response -> response);
   }

   private CompletionStage<BackEnd.EventPatchUnLinkRsp> patchUnLink(final ActorSystem<Void> actorSystem,
                                                                    final ActorRef<BackEnd.Event> backEnd,
                                                                    final String goldenID, final String docID) {
      LOGGER.debug("patchUnLink");
      final CompletionStage<BackEnd.EventPatchUnLinkRsp> stage =
            AskPattern.ask(backEnd,
                           replyTo -> new BackEnd.EventPatchUnLinkReq(replyTo, goldenID, docID, 2.0F),
                           java.time.Duration.ofSeconds(6),
                           actorSystem.scheduler());
      return stage.thenApply(response -> response);
   }

   private Route mapError(final MpiGeneralError obj) {
      LOGGER.debug("{}", obj);
      return switch (obj) {
         case MpiServiceError.EntityIDDoesNotExistError e -> complete(StatusCodes.BAD_REQUEST, e, Jackson.marshaller());
         case MpiServiceError.GoldenIDDoesNotExistError e -> complete(StatusCodes.BAD_REQUEST, e, Jackson.marshaller());
         case MpiServiceError.GoldenIDEntityConflictError e -> complete(StatusCodes.BAD_REQUEST, e, Jackson.marshaller());
         case MpiServiceError.DeletePredicateError e -> complete(StatusCodes.BAD_REQUEST, e, Jackson.marshaller());
         default -> complete(StatusCodes.INTERNAL_SERVER_ERROR);
      };
   }

   private StatusCode mapPatchGoldenRecordPredicateResult(int result) {
      return result == 0 ? StatusCodes.OK : StatusCodes.CONFLICT;
   }

   private Route routePatchGoldenRecordPredicate(final ActorSystem<Void> actorSystem, final ActorRef<BackEnd.Event> backEnd) {
      return parameter(
            "uid",
            uid -> parameter(
                  "predicate",
                  predicate -> parameter(
                        "value",
                        value -> onComplete(
                              patchGoldenRecordPredicate(actorSystem, backEnd, uid, predicate, value),
                              result -> complete(result.isSuccess()
                                                 ? mapPatchGoldenRecordPredicateResult(result.get().result())
                                                 : StatusCodes.IM_A_TEAPOT)))));
   }

   private Route routeUnlink(final ActorSystem<Void> actorSystem, final ActorRef<BackEnd.Event> backEnd) {
      return parameter(
            "goldenID",
            goldenID -> parameter(
                  "docID",
                  docID -> onComplete(
                        patchUnLink(actorSystem, backEnd, goldenID, docID),
                        result -> result.isSuccess()
                                  ? result.get()
                                          .linkInfo()
                                          .mapLeft(this::mapError)
                                          .fold(error -> error,
                                                linkInfo -> complete(StatusCodes.OK, linkInfo, Jackson.marshaller()))
                                  : complete(StatusCodes.IM_A_TEAPOT))));
   }

   private Route routeLink(final ActorSystem<Void> actorSystem, final ActorRef<BackEnd.Event> backEnd) {
      return parameter(
            "goldenID",
            goldenID -> parameter(
                  "newGoldenID",
                  newGoldenID -> parameter(
                        "docID",
                        docID -> parameter(
                              "score",
                              score -> onComplete(
                                    patchLink(actorSystem, backEnd, goldenID, newGoldenID, docID, Float.parseFloat(score)),
                                    result -> result.isSuccess()
                                              ? result.get()
                                                      .linkInfo()
                                                      .mapLeft(this::mapError)
                                                      .fold(error -> error,
                                                            linkInfo -> complete(StatusCodes.OK, linkInfo, Jackson.marshaller()))
                                              : complete(StatusCodes.IM_A_TEAPOT))))));
   }

   private Route routeGoldenRecordCount(final ActorSystem<Void> actorSystem, final ActorRef<BackEnd.Event> backEnd) {
      return onComplete(
            getGoldenRecordCount(actorSystem, backEnd),
            result -> result.isSuccess()
                      ? complete(StatusCodes.OK, new GoldenRecordCount(result.get().count()), Jackson.marshaller())
                      : complete(StatusCodes.IM_A_TEAPOT));
   }


   private Route routeDocumentCount(final ActorSystem<Void> actorSystem, final ActorRef<BackEnd.Event> backEnd) {
      return onComplete(
            getDocumentCount(actorSystem, backEnd),
            result -> result.isSuccess()
                      ? complete(StatusCodes.OK, new DocumentCount(result.get().count()), Jackson.marshaller())
                      : complete(StatusCodes.IM_A_TEAPOT));
   }

   private Route routeNumberOfRecords(final ActorSystem<Void> actorSystem, final ActorRef<BackEnd.Event> backEnd) {
      return onComplete(
            getNumberOfRecords(actorSystem, backEnd),
            result -> result.isSuccess()
                      ? complete(StatusCodes.OK,
                                 new NumberOfRecords(result.get().goldenRecords(), result.get().documents()),
                                 Jackson.marshaller())
                      : complete(StatusCodes.IM_A_TEAPOT));
   }

   private Route routeGoldenIdList(final ActorSystem<Void> actorSystem, final ActorRef<BackEnd.Event> backEnd) {
      return onComplete(
            getGoldenIdList(actorSystem, backEnd),
            result -> result.isSuccess()
                      ? complete(StatusCodes.OK, result.get(), Jackson.marshaller())
                      : complete(StatusCodes.IM_A_TEAPOT));
   }

   //MahaoMockup
   private Route routeMatchesForReviewList(final ActorSystem<Void> actorSystem, final ActorRef<BackEnd.Event> backEnd) {
      return onComplete(
              getMatchesForReviewList(actorSystem, backEnd),
              result -> result.isSuccess()
                      ? complete(StatusCodes.OK, result.get(), Jackson.marshaller())
                      : complete(StatusCodes.IM_A_TEAPOT));
   }


   private Route routeGoldenIdListByPredicate(final ActorSystem<Void> actorSystem, final ActorRef<BackEnd.Event> backEnd) {
      return parameter(
            "predicate",
            predicate -> parameter(
                  "value",
                  value -> onComplete(getGoldenIdListByPredicate(actorSystem, backEnd, predicate, value),
                                      result -> result.isSuccess()
                                                ? complete(StatusCodes.OK, result.get(), Jackson.marshaller())
                                                : complete(StatusCodes.IM_A_TEAPOT))));
   }

   private Route routeGoldenRecordDocuments(final ActorSystem<Void> actorSystem, final ActorRef<BackEnd.Event> backEnd) {
      return parameterList(params -> {
         final var uidList = params.stream().map(paramString).toList();
         return onComplete(getGoldenRecordDocuments(actorSystem, backEnd, uidList),
                           result -> result.isSuccess()
                                     ? complete(StatusCodes.OK, result.get(), Jackson.marshaller())
                                     : complete(StatusCodes.IM_A_TEAPOT));
      });
   }

   private Route routeGoldenRecordDocumentList(final ActorSystem<Void> actorSystem, final ActorRef<BackEnd.Event> backEnd) {
      return parameter("uidList",
                       items -> {
                          final var uidList = Stream.of(items.split(",")).map(String::trim).toList();
                          return onComplete(getGoldenRecordDocuments(actorSystem, backEnd, uidList),
                                            result -> result.isSuccess()
                                                      ? complete(StatusCodes.OK, result.get(), Jackson.marshaller())
                                                      : complete(StatusCodes.IM_A_TEAPOT));
                       });
   }

   private Route routeGoldenRecord(final ActorSystem<Void> actorSystem, final ActorRef<BackEnd.Event> backEnd) {
      return parameter("uid",
                       uid -> onComplete(getGoldenRecord(actorSystem, backEnd, uid),
                                         result -> result.isSuccess()
                                                   ? complete(StatusCodes.OK, result.get(), Jackson.marshaller())
                                                   : complete(StatusCodes.IM_A_TEAPOT)));
   }

   private Route routeCandidates(final ActorSystem<Void> actorSystem, final ActorRef<BackEnd.Event> backEnd) {
      return parameter(
            "uid",
            uid -> entity(
                  Jackson.unmarshaller(CustomMU.class),
                  mu -> onComplete(
                        getCandidates(actorSystem, backEnd, uid, mu),
                        result -> result.isSuccess()
                                  ? result.get()
                                          .candidates()
                                          .mapLeft(this::mapError)
                                          .fold(error -> error,
                                                candidateList -> complete(StatusCodes.OK, candidateList, Jackson.marshaller()))
                                  : complete(StatusCodes.IM_A_TEAPOT))));
   }

   private Route createRoute(final ActorSystem<Void> actorSystem, final ActorRef<BackEnd.Event> backEnd) {
      return pathPrefix(
            "JeMPI",
            () -> concat(
                  patch(() -> concat(
                        path("PatchGoldenRecordPredicate", () -> routePatchGoldenRecordPredicate(actorSystem, backEnd)),
                        path("Unlink", () -> routeUnlink(actorSystem, backEnd)),
                        path("Link", () -> routeLink(actorSystem, backEnd)))),
                  get(() -> concat(
                        path("GoldenRecordCount", () -> routeGoldenRecordCount(actorSystem, backEnd)),
                        path("MatchesForReview", () -> routeMatchesForReviewList(actorSystem, backEnd)),
                        path("DocumentCount", () -> routeDocumentCount(actorSystem, backEnd)),
                        path("NumberOfRecords", () -> routeNumberOfRecords(actorSystem, backEnd)),
                        path("GoldenIdList", () -> routeGoldenIdList(actorSystem, backEnd)),
                        path("GoldenIdListByPredicate", () -> routeGoldenIdListByPredicate(actorSystem, backEnd)),
                        path("GoldenRecordDocuments", () -> routeGoldenRecordDocuments(actorSystem, backEnd)),
                        path("GoldenRecordDocumentList", () -> routeGoldenRecordDocumentList(actorSystem, backEnd)),
                        path("GoldenRecord", () -> routeGoldenRecord(actorSystem, backEnd)),
                        path("Candidates", () -> routeCandidates(actorSystem, backEnd))))));
   }

   private record GoldenRecordCount(Long count) {
   }

   private record DocumentCount(Long count) {
   }

   private record NumberOfRecords(Long goldenRecords,
                                  Long documents) {
   }

}
