package org.jembi.jempi.em;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.DispatcherSelector;
import akka.actor.typed.javadsl.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.AppConfig;
import org.jembi.jempi.shared.models.BatchInteraction;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public final class BackEnd extends AbstractBehavior<BackEnd.Event> {

   private static final Logger LOGGER = LogManager.getLogger(BackEnd.class);

   private final Executor ec;
   private long receivedCount = 0L;
   private long processedCount = 0L;
   private boolean taskBusy = false;

   private BackEnd(final ActorContext<Event> context) {
      super(context);
      ec = context
            .getSystem()
            .dispatchers()
            .lookup(DispatcherSelector.fromConfig("my-blocking-dispatcher"));
   }

   public static Behavior<BackEnd.Event> create() {
      return Behaviors.setup(BackEnd::new);
   }

   @Override
   public Receive<Event> createReceive() {
      ReceiveBuilder<BackEnd.Event> builder = newReceiveBuilder();
      return builder
            .onMessage(EventPatientReq.class, this::eventPatientReqHandler)
            .onMessage(EventWorkTimeReq.class, this::eventWorkTimeReqHandler)
            .build();
   }

   private void doWork(final boolean newRecord) {
      if (newRecord) {
         receivedCount += 1;
      }
      if (receivedCount - processedCount >= AppConfig.BACKEND_N_NEW_VALUES && !taskBusy) {
         final var startOffset = Math.max(0, processedCount - AppConfig.BACKEND_N_OLD_VALUES);
         final var count = AppConfig.BACKEND_N_NEW_VALUES + (processedCount - startOffset);
         LOGGER.debug("receivedCount({}), startOffset({}), count({})", receivedCount, startOffset, count);

         taskBusy = true;
         var cf = CompletableFuture.supplyAsync(
               () -> {
                  LOGGER.info("START EM");
                  final var emTask = new CustomEMTask();
                  var rc = emTask.doIt(startOffset, count);
                  LOGGER.info("END EM {}", rc);
                  return rc;
               },
               ec);

         cf.whenComplete((event, exception) -> {
            LOGGER.debug("Done: {}", event);
            taskBusy = false;
            processedCount += AppConfig.BACKEND_N_NEW_VALUES;
            if (receivedCount - processedCount >= AppConfig.BACKEND_N_NEW_VALUES) {
               getContext().getSelf().tell(EventWorkTimeReq.INSTANCE);
            }
         });

      }
   }

   private Behavior<Event> eventPatientReqHandler(final EventPatientReq request) {
      doWork(true);
      request.replyTo.tell(new BackEnd.EventPatientRsp(true));
      return Behaviors.same();
   }

   private Behavior<Event> eventWorkTimeReqHandler(final EventWorkTimeReq request) {
      doWork(false);
      return Behaviors.same();
   }


   private enum EventWorkTimeReq implements Event {
      INSTANCE
   }

   interface Event {
   }

   interface EventResponse {
   }

   public record EventPatientReq(
         String key,
         BatchInteraction batchInteraction,
         ActorRef<EventPatientRsp> replyTo) implements Event {
   }

   public record EventPatientRsp(boolean result) implements EventResponse {
   }

}
