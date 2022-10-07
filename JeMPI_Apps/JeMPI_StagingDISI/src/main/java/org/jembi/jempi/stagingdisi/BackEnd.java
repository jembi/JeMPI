package org.jembi.jempi.stagingdisi;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;

public class BackEnd extends AbstractBehavior<BackEnd.Event> {

   private static final Logger LOGGER = LogManager.getLogger(BackEnd.class);
   private static final String SINGLE_TIMER_TIMEOUT_KEY = "SingleTimerTimeOutKey";

   private BackEnd(ActorContext<Event> context) {
      super(context);
   }

   public static Behavior<Event> create() {
      return Behaviors.setup(BackEnd::new);
   }

   public Receive<Event> createReceive() {
      return newReceiveBuilder()
            .onMessage(EventTeaTime.class, this::eventTeaTimeHandler)
            .onMessage(EventWorkTime.class, this::eventWorkTimeHandler)
            .build();
   }

   private Behavior<Event> eventWorkTimeHandler(EventWorkTime request) {
      LOGGER.info("WORK TIME");
      return Behaviors.same();
   }

   private Behavior<Event> eventTeaTimeHandler(EventTeaTime request) {
      LOGGER.info("TEA TIME");
      return Behaviors.withTimers(timers -> {
         timers.startSingleTimer(SINGLE_TIMER_TIMEOUT_KEY, EventWorkTime.INSTANCE, Duration.ofSeconds(5));
         return Behaviors.same();
      });
   }

   private enum EventTeaTime implements Event {
      INSTANCE
   }

   private enum EventWorkTime implements Event {
      INSTANCE
   }

   interface Event {}

}
