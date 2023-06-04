package org.jembi.jempi.api;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.Route;
import ch.megard.akka.http.cors.javadsl.settings.CorsSettings;
import org.jembi.jempi.AppConfig;
import org.jembi.jempi.libapi.ApiBase;
import org.jembi.jempi.libapi.BackEnd;
import org.jembi.jempi.shared.models.GlobalConstants;
import org.jembi.jempi.shared.models.RecordType;

import java.util.regex.Pattern;

import static akka.http.javadsl.server.PathMatchers.segment;
import static ch.megard.akka.http.cors.javadsl.CorsDirectives.cors;

public final class HttpServer extends ApiBase {

   private HttpServer() {
   }

   static HttpServer create() {
      return new HttpServer();
   }

   private Route createJeMPIRoutes(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final String jsonFields) {
      return concat(
            post(() -> concat(path(GlobalConstants.SEGMENT_UPDATE_NOTIFICATION,
                                   () -> routeUpdateNotificationState(actorSystem, backEnd)),
                              path(segment(GlobalConstants.SEGMENT_POST_SIMPLE_SEARCH)
                                         .slash(segment(Pattern.compile("^(golden|patient)$"))),
                                   type -> {
                                      final var t = type.equals("golden")
                                            ? RecordType.GoldenRecord
                                            : RecordType.Interaction;
                                      return routeSimpleSearch(actorSystem, backEnd, t);
                                   }),
                              path(segment(GlobalConstants.SEGMENT_POST_CUSTOM_SEARCH)
                                         .slash(segment(Pattern.compile("^(golden|patient)$"))),
                                   type -> {
                                      final var t = type.equals("golden")
                                            ? RecordType.GoldenRecord
                                            : RecordType.Interaction;
                                      return routeCustomSearch(actorSystem, backEnd, t);
                                   }),
                              path(GlobalConstants.SEGMENT_CALCULATE_SCORES, this::routeCalculateScores),
                              path(GlobalConstants.SEGMENT_UPLOAD, () -> routeUploadCsvFile(actorSystem, backEnd)))),
            patch(() -> concat(
                  path(segment(GlobalConstants.SEGMENT_UPDATE_GOLDEN_RECORD)
                             .slash(segment(Pattern.compile("^[A-z0-9]+$"))),
                       goldenId -> routeUpdateGoldenRecordFields(actorSystem, backEnd, goldenId)),
                  path(GlobalConstants.SEGMENT_CREATE_GOLDEN_RECORD,
                       () -> routeUpdateLinkToNewGoldenRecord(actorSystem, backEnd)),
                  path(GlobalConstants.SEGMENT_LINK_RECORD, () -> routeUpdateLinkToExistingGoldenRecord(actorSystem, backEnd)))),
            get(() -> concat(
                  path(GlobalConstants.SEGMENT_COUNT_GOLDEN_RECORDS, () -> routeGoldenRecordCount(actorSystem, backEnd)),
                  path(GlobalConstants.SEGMENT_COUNT_PATIENT_RECORDS, () -> routeInteractionCount(actorSystem, backEnd)),
                  path(GlobalConstants.SEGMENT_COUNT_RECORDS, () -> routeNumberOfRecords(actorSystem, backEnd)),
                  path(GlobalConstants.SEGMENT_GOLDEN_IDS, () -> routeGoldenIds(actorSystem, backEnd)),
                  path(GlobalConstants.SEGMENT_GET_GOLDEN_ID_DOCUMENTS, () -> routeGoldenRecord(actorSystem, backEnd)),
                  path(GlobalConstants.SEGMENT_EXPANDED_GOLDEN_RECORDS, () -> routeExpandedGoldenRecords(actorSystem, backEnd)),
                  path(GlobalConstants.SEGMENT_EXPANDED_PATIENT_RECORDS, () -> routeExpandedPatientRecords(actorSystem, backEnd)),
                  path(GlobalConstants.SEGMENT_GET_NOTIFICATIONS, () -> routeFindMatchesForReview(actorSystem, backEnd)),
                  path(GlobalConstants.SEGMENT_CANDIDATE_GOLDEN_RECORDS, () -> routeFindCandidates(actorSystem, backEnd)),
                  path(segment(GlobalConstants.SEGMENT_PATIENT_RECORD_ROUTE)
                             .slash(segment(Pattern.compile("^[A-z0-9]+$"))),
                       patientId -> routeFindPatientRecord(actorSystem, backEnd, patientId)),
                  path(segment(GlobalConstants.SEGMENT_GOLDEN_RECORD_ROUTE)
                             .slash(segment(Pattern.compile("^[A-z0-9]+$"))),
                       goldenId -> routeFindExpandedGoldenRecord(actorSystem, backEnd, goldenId)),
                  path(GlobalConstants.SEGMENT_GET_FIELDS_CONFIG, () -> complete(StatusCodes.OK, jsonFields)))
               ));
   }

   @Override
   protected Route createCorsRoutes(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final String jsonFields) {
      final var settings = CorsSettings.create(AppConfig.CONFIG);
      return cors(
            settings,
            () -> pathPrefix("JeMPI",
                             () -> createJeMPIRoutes(actorSystem, backEnd, jsonFields)));
   }

}
