package org.jembi.jempi.controller;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.jembi.jempi.AppConfig;
import org.jembi.jempi.shared.models.CustomFieldTallies;
import org.jembi.jempi.shared.models.NotificationResolutionProcessorData;

import java.time.Duration;
import java.util.HashMap;
import java.util.concurrent.CompletionStage;

public final class BackEnd extends AbstractBehavior<BackEnd.Event> {

   private static final Logger LOGGER = LogManager.getLogger(BackEnd.class);
   private static final String SINGLE_TIMER_TIMEOUT_KEY = "SingleTimerTimeOutKey";
//   private final LibMPI libMPI;

   //   private final ProcessorsRegistry interactionProcessorsRegistry;
   private BackEnd(final ActorContext<Event> context) {
      super(context);
      Configurator.setLevel(this.getClass(), AppConfig.GET_LOG_LEVEL);
//      libMPI = getLibMPI(true);
//      interactionProcessorsRegistry = new ProcessorsRegistry();
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
                 java.time.Duration.ofSeconds(6),
                 actorSystem.scheduler());
      return stage.thenApply(response -> response);
   }

   static CompletionStage<BackEnd.DashboardDataResponse> askGetDashboardData(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Event> backEnd) {
      final CompletionStage<BackEnd.DashboardDataResponse> stage = AskPattern
            .ask(backEnd,
                 replyTo -> new DashboardDataRequest(replyTo),
                 java.time.Duration.ofSeconds(6),
                 actorSystem.scheduler());
      return stage.thenApply(response -> response);
   }

/*
   private LibMPI getLibMPI(final boolean useDGraph) {
      LibMPI libMPIIn;
      if (useDGraph) {
         final var host = AppConfig.getDGraphHosts();
         final var port = AppConfig.getDGraphPorts();
         libMPIIn = new LibMPI(AppConfig.GET_LOG_LEVEL,
                               host,
                               port,
                               AppConfig.KAFKA_BOOTSTRAP_SERVERS,
                               "CLIENT_ID_CONTROLLER_INTERACTION_PROCESSOR-" + UUID.randomUUID());
      } else {
         libMPIIn = null;
      }
      libMPIIn.startTransaction();
      return libMPIIn;
   }
*/

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
         timers.startSingleTimer(SINGLE_TIMER_TIMEOUT_KEY, EventWorkTime.INSTANCE, Duration.ofSeconds(5));
         return Behaviors.same();
      });
   }

   private Behavior<Event> onNotificationResolutionHandler(final OnNotificationResolutionRequest request) {

//      for (IOnNotificationResolutionProcessor notificationResolutionProcessor : this.interactionProcessorsRegistry
//      .getOnNotificationResolutionProcessors(GlobalConstants.DEFAULT_LINKER_GLOBAL_STORE_NAME)) {
//         try {
//            notificationResolutionProcessor.processOnNotificationResolution(request.notificationResolutionDetails, libMPI);
//         } catch (Exception e) {
//            LOGGER.error("An error occurred trying OnNotificationResolution", e);
//         }
//
//      }
      request.replyTo.tell(new OnNotificationResolutionResponse(true));
      return Behaviors.same();
   }

   private Behavior<Event> getDashboardDataHandler(final DashboardDataRequest request) {
      final var dashboardData = new HashMap<String, Object>();
      final var linkStatsMeta = LinkStatsMetaCache.get();
      if (linkStatsMeta != null) {
         dashboardData.put("linker_stats", new LinkerStats(123L, 456L));
         dashboardData.put("m_and_u", new DashboardMU(
               DashboardMU.getMU(linkStatsMeta.customFieldTallies().givenName()),
               DashboardMU.getMU(linkStatsMeta.customFieldTallies().familyName()),
               DashboardMU.getMU(linkStatsMeta.customFieldTallies().gender()),
               DashboardMU.getMU(linkStatsMeta.customFieldTallies().dob()),
               DashboardMU.getMU(linkStatsMeta.customFieldTallies().city()),
               DashboardMU.getMU(linkStatsMeta.customFieldTallies().phoneNumber()),
               DashboardMU.getMU(linkStatsMeta.customFieldTallies().nationalId())));
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

   private record DashboardMU(
         MU givenName,
         MU familyName,
         MU gender,
         MU dob,
         MU city,
         MU phoneNumber,
         MU nationalId) {

      static MU getMU(final CustomFieldTallies.FieldTally fieldTally) {
         if (fieldTally.a() + fieldTally.b() == 0 || fieldTally.c() + fieldTally.d() == 0) {
            return new MU(-1.0, -1.0);
         }
         return new MU(fieldTally.a().doubleValue() / (fieldTally.a().doubleValue() + fieldTally.b().doubleValue()),
                       fieldTally.c().doubleValue() / (fieldTally.c().doubleValue() + fieldTally.d().doubleValue()));
      }

      record MU(
            Double m,
            Double u) {
      }
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
