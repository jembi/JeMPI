package org.jembi.jempi.controller;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.jembi.jempi.AppConfig;
import org.jembi.jempi.libmpi.LibMPI;
import org.jembi.jempi.shared.models.GlobalConstants;
import org.jembi.jempi.shared.models.NotificationResolutionProcessorData;

import java.time.Duration;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

public final class BackEnd extends AbstractBehavior<BackEnd.Event> {

   private static final Logger LOGGER = LogManager.getLogger(BackEnd.class);
   private static final String SINGLE_TIMER_TIMEOUT_KEY = "SingleTimerTimeOutKey";

   private LibMPI libMPI = null;

   private BackEnd(final ActorContext<Event> context) {
      super(context);
      Configurator.setLevel(this.getClass(), AppConfig.GET_LOG_LEVEL);
      if (libMPI == null) {
         openMPI();
      }
   }

   public static Behavior<Event> create() {
      return Behaviors.setup(BackEnd::new);
   }

   static CompletionStage<BackEnd.OnNotificationResolutionResponse> askOnNotificationResolution(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd,
         final NotificationResolutionProcessorData notificationResolutionDetails) {
      final CompletionStage<BackEnd.OnNotificationResolutionResponse> stage = AskPattern
            .ask(backEnd,
                 replyTo -> new OnNotificationResolutionRequest(replyTo, notificationResolutionDetails),
                 java.time.Duration.ofSeconds(GlobalConstants.TIMEOUT_DGRAPH_QUERY_SECS),
                 actorSystem.scheduler());
      return stage.thenApply(response -> response);
   }

   static CompletionStage<BackEnd.DashboardDataResponse> askGetDashboardData(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      final CompletionStage<BackEnd.DashboardDataResponse> stage = AskPattern
            .ask(backEnd,
                 replyTo -> new DashboardDataRequest(replyTo),
                 java.time.Duration.ofSeconds(GlobalConstants.TIMEOUT_GENERAL_SECS),
                 actorSystem.scheduler());
      return stage.thenApply(response -> response);
   }

   private void openMPI() {
      final var host = AppConfig.getDGraphHosts();
      final var port = AppConfig.getDGraphPorts();
      libMPI = new LibMPI(AppConfig.GET_LOG_LEVEL,
                          host,
                          port,
                          AppConfig.KAFKA_BOOTSTRAP_SERVERS,
                          "CLIENT_ID_CONTROLLER-" + UUID.randomUUID());
   }

   public void close() {
   }

   public Receive<Event> createReceive() {
      return newReceiveBuilder().onMessage(EventTeaTime.class, this::eventTeaTimeHandler)
                                .onMessage(EventWorkTime.class, this::eventWorkTimeHandler)
                                .onMessage(OnNotificationResolutionRequest.class, this::onNotificationResolutionHandler)
                                .onMessage(DashboardDataRequest.class, this::getDashboardDataHandler)
                                .build();
   }

   private Behavior<Event> eventWorkTimeHandler(final EventWorkTime request) {
      LOGGER.info("WORK TIME");
      return Behaviors.same();
   }

   private Behavior<Event> eventTeaTimeHandler(final EventTeaTime request) {
      LOGGER.info("TEA TIME");
      return Behaviors.withTimers(timers -> {
         timers.startSingleTimer(SINGLE_TIMER_TIMEOUT_KEY,
                                 EventWorkTime.INSTANCE,
                                 Duration.ofSeconds(GlobalConstants.TIMEOUT_TEA_TIME_SECS));
         return Behaviors.same();
      });
   }

   private Behavior<Event> onNotificationResolutionHandler(final OnNotificationResolutionRequest request) {
      request.replyTo.tell(new OnNotificationResolutionResponse(true));
      return Behaviors.same();
   }

   private Behavior<Event> getDashboardDataHandler(final DashboardDataRequest request) {
      final var dashboardData = new HashMap<String, Object>();
      final var linkStatsMeta = LinkStatsMetaCache.get();
      if (linkStatsMeta != null) {
         dashboardData.put("linker_stats", new LinkerStats(libMPI.countGoldenRecords(), libMPI.countInteractions()));
         dashboardData.put("m_and_u", CustomControllerDashboardMU.fromCustomFieldTallies(linkStatsMeta.customFieldTallies()));
         final var tp = linkStatsMeta.confusionMatrix().TP();
         final var fp = linkStatsMeta.confusionMatrix().FP();
         final var tn = linkStatsMeta.confusionMatrix().TN();
         final var fn = linkStatsMeta.confusionMatrix().FN();
         final var b1 = 0.25;  // beta = 0.5
         final var b2 = 1.0;   // beta = 1.0
         final var b3 = 4.0;   // beta = 2.0;
         final var f1 = ((1.0 + b1) * tp) / ((1 + b1) * tp + b1 * fn + fp);
         final var f2 = ((1.0 + b2) * tp) / ((1 + b2) * tp + b2 * fn + fp);
         final var f3 = ((1.0 + b3) * tp) / ((1 + b3) * tp + b3 * fn + fp);
         dashboardData.put("tptn",
                           new TPTN(new TPTN.TPTNMatrix(tp.longValue(),
                                                        fp.longValue(),
                                                        tn.longValue(),
                                                        fn.longValue()),
                                    new TPTN.TPTNfScore(f1, f2, f3)));
      }
      request.replyTo.tell(new DashboardDataResponse(dashboardData));
      return Behaviors.same();
   }

   private enum EventTeaTime implements Event {
      INSTANCE
   }

   private enum EventWorkTime implements Event {
      INSTANCE
   }

   interface Event {
   }

   private record LinkerStats(
         Long goldenRecordCount,
         Long interactionsCount) {
   }

   private record TPTN(
         TPTNMatrix tptnMatrix,
         TPTNfScore tptnfScore) {
      record TPTNMatrix(
            Long truePositive,
            Long falsePositive,
            Long trueNegative,
            Long falseNegative) {
      }

      record TPTNfScore(
            Double precision,
            Double recall_precision,
            Double recall
      ) {
      }

   }

   public record OnNotificationResolutionRequest(
         ActorRef<OnNotificationResolutionResponse> replyTo,
         NotificationResolutionProcessorData notificationResolutionDetails
   ) implements Event {
   }

   public record OnNotificationResolutionResponse(Boolean updated)
         implements Event {
   }

   public record DashboardDataRequest(
         ActorRef<DashboardDataResponse> replyTo
   ) implements Event {
   }

   public record DashboardDataResponse(HashMap<String, Object> dashboardData)
         implements Event {
   }

}
