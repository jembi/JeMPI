package org.jembi.jempi.notifications;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.Terminated;
import akka.actor.typed.javadsl.Behaviors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.AppConfig;

public final class Main {

   private static final Logger LOGGER = LogManager.getLogger(Main.class);

   public static void main(final String[] args) {
      new Main().run();
   }

   public Behavior<Void> create() {
      return Behaviors.setup(context -> {
         ActorRef<BackEnd.Event> backEnd = context.spawn(BackEnd.create(), "BackEnd");
         context.watch(backEnd);
         final NotificationStreamAsync notificationStreamAsync = new NotificationStreamAsync();
         notificationStreamAsync.open(context.getSystem(), backEnd);
         return Behaviors.receive(Void.class).onSignal(Terminated.class, sig -> {
            notificationStreamAsync.close(context.getSystem());
            return Behaviors.stopped();
         }).build();
      });
   }

   private void run() {
      LOGGER.info("KAFKA: {} {} {}", AppConfig.KAFKA_BOOTSTRAP_SERVERS, AppConfig.KAFKA_APPLICATION_ID_NOTIFICATIONS,
                  AppConfig.KAFKA_CLIENT_ID_NOTIFICATIONS);

      var hello1 = new HelloScala().hello();
      var hello2 = HelloScala$.MODULE$.hello();
      var hello3 = HelloScala$.MODULE$.hallo();
      LOGGER.debug("{} {} {}", hello1, hello2, hello3);

      ActorSystem.create(this.create(), "LinkerApp");
   }
}
