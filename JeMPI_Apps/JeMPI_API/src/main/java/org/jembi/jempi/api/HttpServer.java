package org.jembi.jempi.api;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import ch.megard.akka.http.cors.javadsl.settings.CorsSettings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.AppConfig;
import org.jembi.jempi.libapi.BackEnd;
import org.jembi.jempi.libapi.Routes;
import org.jembi.jempi.shared.models.GlobalConstants;
import org.jembi.jempi.shared.models.RecordType;

import java.util.concurrent.CompletionStage;
import java.util.regex.Pattern;

import static akka.http.javadsl.server.PathMatchers.segment;
import static ch.megard.akka.http.cors.javadsl.CorsDirectives.cors;

public final class HttpServer extends AllDirectives {

   private static final Logger LOGGER = LogManager.getLogger(HttpServer.class);

   private CompletionStage<ServerBinding> binding = null;
   private Http http = null;


   private HttpServer() {
   }

   static HttpServer create() {
      return new HttpServer();
   }

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

   public void close(final ActorSystem<Void> actorSystem) {
      binding.thenCompose(ServerBinding::unbind) // trigger unbinding from the port
             .thenAccept(unbound -> actorSystem.terminate()); // and shutdown when done
   }


   private Route createJeMPIRoutes(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final String jsonFields) {
      return concat(post(() -> concat(path(GlobalConstants.SEGMENT_UPDATE_NOTIFICATION,
                                           () -> Routes.routeUpdateNotificationState(actorSystem, backEnd)),
                                      path(segment(GlobalConstants.SEGMENT_POST_SIMPLE_SEARCH).slash(segment(Pattern.compile(
                                            "^(golden|patient)$"))), type -> {
                                         final var t = type.equals("golden")
                                               ? RecordType.GoldenRecord
                                               : RecordType.Interaction;
                                         return Routes.routeSimpleSearch(actorSystem, backEnd, t);
                                      }),
                                      path(segment(GlobalConstants.SEGMENT_POST_CUSTOM_SEARCH).slash(segment(Pattern.compile(
                                            "^(golden|patient)$"))), type -> {
                                         final var t = type.equals("golden")
                                               ? RecordType.GoldenRecord
                                               : RecordType.Interaction;
                                         return Routes.routeCustomSearch(actorSystem, backEnd, t);
                                      }),
                                      path(GlobalConstants.SEGMENT_CALCULATE_SCORES, () -> Routes.routeCalculateScores(http)),
                                      path(GlobalConstants.SEGMENT_UPLOAD,
                                           () -> Routes.routeUploadCsvFile(actorSystem, backEnd)))),
                    patch(() -> concat(path(segment(GlobalConstants.SEGMENT_UPDATE_GOLDEN_RECORD).slash(segment(Pattern.compile(
                                             "^[A-z0-9]+$"))), goldenId -> Routes.routeUpdateGoldenRecordFields(actorSystem,
                                                                                                                backEnd,
                                                                                                                goldenId)),
                                       path(GlobalConstants.SEGMENT_CREATE_GOLDEN_RECORD,
                                            () -> Routes.routeUpdateLinkToNewGoldenRecord(actorSystem, backEnd)),
                                       path(GlobalConstants.SEGMENT_LINK_RECORD,
                                            () -> Routes.routeUpdateLinkToExistingGoldenRecord(actorSystem, backEnd)))),
                    get(() -> concat(path(GlobalConstants.SEGMENT_COUNT_GOLDEN_RECORDS,
                                          () -> Routes.routeGoldenRecordCount(actorSystem, backEnd)),
                                     path(GlobalConstants.SEGMENT_COUNT_PATIENT_RECORDS,
                                          () -> Routes.routeInteractionCount(actorSystem, backEnd)),
                                     path(GlobalConstants.SEGMENT_COUNT_RECORDS,
                                          () -> Routes.routeNumberOfRecords(actorSystem, backEnd)),
                                     path(GlobalConstants.SEGMENT_GOLDEN_IDS, () -> Routes.routeGoldenIds(actorSystem, backEnd)),
                                     path(GlobalConstants.SEGMENT_GET_GOLDEN_ID_DOCUMENTS,
                                          () -> Routes.routeGoldenRecord(actorSystem, backEnd)),
                                     path(GlobalConstants.SEGMENT_EXPANDED_GOLDEN_RECORDS,
                                          () -> Routes.routeExpandedGoldenRecords(actorSystem, backEnd)),
                                     path(GlobalConstants.SEGMENT_EXPANDED_PATIENT_RECORDS,
                                          () -> Routes.routeExpandedPatientRecords(actorSystem, backEnd)),
                                     path(GlobalConstants.SEGMENT_GET_NOTIFICATIONS,
                                          () -> Routes.routeFindMatchesForReview(actorSystem, backEnd)),
                                     path(GlobalConstants.SEGMENT_CANDIDATE_GOLDEN_RECORDS,
                                          () -> Routes.routeFindCandidates(actorSystem, backEnd)),
                                     path(segment(GlobalConstants.SEGMENT_PATIENT_RECORD_ROUTE).slash(segment(Pattern.compile(
                                                "^[A-z0-9]+$"))),
                                          patientId -> Routes.routeFindPatientRecord(actorSystem, backEnd, patientId)),
                                     path(segment(GlobalConstants.SEGMENT_GOLDEN_RECORD_ROUTE).slash(segment(Pattern.compile(
                                                "^[A-z0-9]+$"))),
                                          goldenId -> Routes.routeFindExpandedGoldenRecord(actorSystem, backEnd, goldenId)),
                                     path(GlobalConstants.SEGMENT_GET_FIELDS_CONFIG,
                                          () -> complete(StatusCodes.OK, jsonFields)))));
   }

   Route createCorsRoutes(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final String jsonFields) {
      final var settings = CorsSettings.create(AppConfig.CONFIG);
      return cors(settings, () -> pathPrefix("JeMPI", () -> createJeMPIRoutes(actorSystem, backEnd, jsonFields)));
   }

}
