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
import org.apache.logging.log4j.core.config.Configurator;
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
      Configurator.setLevel(this.getClass(), AppConfig.GET_LOG_LEVEL);
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
      binding = http.newServerAt(httpServerHost, httpPort).bind(this.createCorsRoutes(actorSystem, backEnd, jsonFields));
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
      return concat(post(() -> concat(path(GlobalConstants.SEGMENT_POST_UPDATE_NOTIFICATION,
                                           () -> Routes.postUpdateNotification(actorSystem, backEnd)),
                                      path(segment(GlobalConstants.SEGMENT_POST_SIMPLE_SEARCH).slash(segment(Pattern.compile(
                                                 "^(golden|patient)$"))),
                                           type -> Routes.postSimpleSearch(actorSystem,
                                                                           backEnd,
                                                                           type.equals("golden")
                                                                                 ? RecordType.GoldenRecord
                                                                                 : RecordType.Interaction)),
                                      path(segment(GlobalConstants.SEGMENT_POST_CUSTOM_SEARCH).slash(segment(Pattern.compile(
                                                 "^(golden|patient)$"))),
                                           type -> Routes.postCustomSearch(actorSystem,
                                                                           backEnd,
                                                                           type.equals("golden")
                                                                                 ? RecordType.GoldenRecord
                                                                                 : RecordType.Interaction)),
                                      path(GlobalConstants.SEGMENT_POST_UPLOAD_CSV_FILE,
                                           () -> Routes.postUploadCsvFile(actorSystem, backEnd)),
                                      path(GlobalConstants.SEGMENT_PROXY_POST_CALCULATE_SCORES,
                                           () -> Routes.proxyPostCalculateScores(AppConfig.LINKER_IP,
                                                                                 AppConfig.LINKER_HTTP_PORT,
                                                                                 http)),
                                      path(GlobalConstants.SEGMENT_POST_FILTER_GIDS,
                                           () -> Routes.postFilterGids(actorSystem, backEnd)),
                                      path(GlobalConstants.SEGMENT_PROXY_CR_REGISTER,
                                           () -> Routes.postCrRegister(AppConfig.LINKER_IP, AppConfig.LINKER_HTTP_PORT, http)),
                                      path(GlobalConstants.SEGMENT_PROXY_CR_FIND,
                                           () -> Routes.postCrFind(AppConfig.LINKER_IP, AppConfig.LINKER_HTTP_PORT, http)),
                                      path(GlobalConstants.SEGMENT_PROXY_CR_CANDIDATES,
                                           () -> Routes.postCrCandidates(AppConfig.LINKER_IP, AppConfig.LINKER_HTTP_PORT, http)),
                                      path(GlobalConstants.SEGMENT_POST_FILTER_GIDS_WITH_INTERACTION_COUNT,
                                           () -> Routes.postFilterGidsWithInteractionCount(actorSystem, backEnd)))),
                    patch(() -> concat(path(segment(GlobalConstants.SEGMENT_PATCH_GOLDEN_RECORD).slash(segment(Pattern.compile(
                                             "^[A-z0-9]+$"))), gid -> Routes.patchGoldenRecord(actorSystem, backEnd, gid)),
                                       path(GlobalConstants.SEGMENT_PATCH_IID_NEW_GID_LINK,
                                            () -> Routes.patchIidNewGidLink(actorSystem, backEnd)),
                                       path(GlobalConstants.SEGMENT_PATCH_IID_GID_LINK,
                                            () -> Routes.patchIidGidLink(actorSystem, backEnd)),
                                       path(GlobalConstants.SEGMENT_PROXY_CR_UPDATE_FIELDS,
                                            () -> Routes.patchCrUpdateFields(AppConfig.LINKER_IP, AppConfig.LINKER_HTTP_PORT, http)))),
                    get(() -> concat(path(GlobalConstants.SEGMENT_COUNT_GOLDEN_RECORDS,
                                          () -> Routes.countGoldenRecords(actorSystem, backEnd)),
                                     path(GlobalConstants.SEGMENT_HEALTH,
                                          () -> complete(StatusCodes.OK)),
                                     path(GlobalConstants.SEGMENT_COUNT_INTERACTIONS,
                                          () -> Routes.countInteractions(actorSystem, backEnd)),
                                     path(GlobalConstants.SEGMENT_COUNT_RECORDS,
                                          () -> Routes.countRecords(actorSystem, backEnd)),
                                     path(GlobalConstants.SEGMENT_GET_GIDS_ALL,
                                          () -> Routes.getGidsAll(actorSystem, backEnd)),
                                     path(GlobalConstants.SEGMENT_GET_GIDS_PAGED,
                                          () -> Routes.getGidsPaged(actorSystem, backEnd)),
                                     path(segment(GlobalConstants.SEGMENT_GET_INTERACTION).slash(segment(Pattern.compile(
                                           "^[A-z0-9]+$"))), iid -> Routes.getInteraction(actorSystem, backEnd, iid)),
                                     path(segment(GlobalConstants.SEGMENT_GET_EXPANDED_GOLDEN_RECORD).slash(segment(Pattern.compile(
                                           "^[A-z0-9]+$"))), gid -> Routes.getExpandedGoldenRecord(actorSystem, backEnd, gid)),
                                     path(GlobalConstants.SEGMENT_GET_EXPANDED_GOLDEN_RECORDS_USING_PARAMETER_LIST,
                                          () -> Routes.getExpandedGoldenRecordsUsingParameterList(actorSystem, backEnd)),
                                     path(GlobalConstants.SEGMENT_GET_EXPANDED_GOLDEN_RECORDS_USING_CSV,
                                          () -> Routes.getExpandedGoldenRecordsFromUsingCSV(actorSystem, backEnd)),
                                     path(GlobalConstants.SEGMENT_GET_EXPANDED_INTERACTIONS_USING_CSV,
                                          () -> Routes.getExpandedInteractionsUsingCSV(actorSystem, backEnd)),
                                     path(GlobalConstants.SEGMENT_GET_GOLDEN_RECORD_AUDIT_TRAIL,
                                          () -> Routes.getGoldenRecordAuditTrail(actorSystem, backEnd)),
                                     path(GlobalConstants.SEGMENT_GET_INTERACTION_AUDIT_TRAIL,
                                          () -> Routes.getInteractionAuditTrail(actorSystem, backEnd)),
                                     path(GlobalConstants.SEGMENT_GET_NOTIFICATIONS,
                                          () -> Routes.getNotifications(actorSystem, backEnd)),
                                     path(segment(GlobalConstants.SEGMENT_GET_INTERACTION).slash(segment(Pattern.compile(
                                           "^[A-z0-9]+$"))), iid -> Routes.getInteraction(actorSystem, backEnd, iid)),
                                     path(segment(GlobalConstants.SEGMENT_GET_EXPANDED_GOLDEN_RECORD).slash(segment(Pattern.compile(
                                           "^[A-z0-9]+$"))), gid -> Routes.getExpandedGoldenRecord(actorSystem, backEnd, gid)),
                                     path(GlobalConstants.SEGMENT_GET_FIELDS_CONFIG, () -> complete(StatusCodes.OK, jsonFields)),
                                     path(GlobalConstants.SEGMENT_PROXY_GET_CANDIDATES_WITH_SCORES,
                                          () -> Routes.proxyGetCandidatesWithScore(AppConfig.LINKER_IP,
                                                                                   AppConfig.LINKER_HTTP_PORT,
                                                                                   http)))));
   }

   Route createCorsRoutes(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final String jsonFields) {
      final var settings = CorsSettings.create(AppConfig.CONFIG);
      return cors(settings, () -> pathPrefix("JeMPI", () -> createJeMPIRoutes(actorSystem, backEnd, jsonFields)));
   }

}
