package org.jembi.jempi.controller;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.Terminated;
import akka.actor.typed.javadsl.Behaviors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.AppConfig;

public final class Main {

   private static final Logger LOGGER = LogManager.getLogger(Main.class);

   private Main() {
   }

   public static void main(final String[] args) {
      new Main().run();
   }

   public Behavior<Void> create() {
      return Behaviors.setup(
            context -> {
               final var backEndActor = context.spawn(BackEnd.create(), "BackEnd");
               context.watch(backEndActor);
               final var auditTrailStreamProcessor = new AuditTrailStreamProcessor();
               auditTrailStreamProcessor.open();
               final var notificationStreamProcessor = new NotificationStreamProcessor();
               notificationStreamProcessor.open();
               final var interactionsStreamProcessor = new InteractionsStreamProcessor();
               interactionsStreamProcessor.open(context.getSystem(), backEndActor);
               final var interactionsHTTP = new InteractionsHTTP();
               interactionsHTTP.open(context.getSystem(), backEndActor);
               return Behaviors.receive(Void.class)
                               .onSignal(Terminated.class,
                                         sig -> {
                                            interactionsHTTP.close(context.getSystem());
                                            return Behaviors.stopped();
                                         })
                               .build();
            });
   }

   private void run() {
      LOGGER.info("KAFKA: {} {} {}",
                  AppConfig.KAFKA_BOOTSTRAP_SERVERS,
                  AppConfig.KAFKA_APPLICATION_ID,
                  AppConfig.KAFKA_CLIENT_ID);

      ActorSystem.create(this.create(), "ControllerApp");

   }
}
