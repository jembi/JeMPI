package org.jembi.jempi.controller;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.jembi.jempi.AppConfig;
import org.jembi.jempi.controller.interactions_processor.processors.IDashboardDataProducer;
import org.jembi.jempi.controller.interactions_processor.processors.IOnNotificationResolutionProcessor;
import org.jembi.jempi.controller.interactions_processor.processors.ProcessorsRegistry;
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
   private final LibMPI libMPI;
   private final ProcessorsRegistry interactionProcessorsRegistry;
   private BackEnd(final ActorContext<Event> context) {
      super(context);
      Configurator.setLevel(this.getClass(), AppConfig.GET_LOG_LEVEL);
      libMPI = getLibMPI(true);
      interactionProcessorsRegistry = new ProcessorsRegistry();
   }

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

   public static Behavior<Event> create() {
      return Behaviors.setup(BackEnd::new);
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
         timers.startSingleTimer(SINGLE_TIMER_TIMEOUT_KEY, EventWorkTime.INSTANCE, Duration.ofSeconds(5));
         return Behaviors.same();
      });
   }

   private Behavior<Event> onNotificationResolutionHandler(final OnNotificationResolutionRequest request) {

      for (IOnNotificationResolutionProcessor notificationResolutionProcessor : this.interactionProcessorsRegistry.getOnNotificationResolutionProcessors(GlobalConstants.DEFAULT_LINKER_GLOBAL_STORE_NAME)) {
         try {
            notificationResolutionProcessor.processOnNotificationResolution(request.notificationResolutionDetails, libMPI);
         } catch (Exception e) {
            LOGGER.error("An error occurred trying OnNotificationResolution", e);
         }

      }
      request.replyTo.tell(new OnNotificationResolutionResponse(true));
      return Behaviors.same();
   }


   private Behavior<Event> getDashboardDataHandler(final DashboardDataRequest request) {

      HashMap<String, Object> dashboardData = new HashMap<>();

      for (IDashboardDataProducer<?> dashboardDataProducer : this.interactionProcessorsRegistry.getDashboardDataProducerProcessors(GlobalConstants.DEFAULT_LINKER_GLOBAL_STORE_NAME)) {
         try {
            dashboardData.put(dashboardDataProducer.getDashboardDataName(), dashboardDataProducer.getDashboardData(libMPI));
         } catch (Exception e) {
            LOGGER.error(String.format("An error occurred trying to the dashboard data for %s. Will not be included", dashboardDataProducer.getDashboardDataName()), e);
         }

      }
      request.replyTo.tell(new DashboardDataResponse(dashboardData));
      return Behaviors.same();
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

   private enum EventTeaTime implements Event {
      INSTANCE
   }

   private enum EventWorkTime implements Event {
      INSTANCE
   }

   interface Event {
   }

   public record OnNotificationResolutionRequest(
           ActorRef<OnNotificationResolutionResponse> replyTo,
           NotificationResolutionProcessorData notificationResolutionDetails
   ) implements Event  {
   }

   public record OnNotificationResolutionResponse(Boolean updated)
           implements Event {
   }

   public record DashboardDataRequest(
           ActorRef<DashboardDataResponse> replyTo
   ) implements Event  {
   }

   public record DashboardDataResponse(HashMap<String, Object> dashboardData)
           implements Event {
   }

}
