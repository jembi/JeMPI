package org.jembi.jempi.pre_processor;

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
      return Behaviors.setup(
            context -> {
               final var customSourceRecordStream = new CustomSourceRecordStream();
               customSourceRecordStream.open();
               final var customFHIRsyncReceiver = new CustomFHIRsyncReceiver();
               customFHIRsyncReceiver.open(context.getSystem());
               return Behaviors.receive(Void.class)
                               .onSignal(Terminated.class,
                                         sig -> Behaviors.stopped())
                               .build();
            });
   }

   private void run() {
      LOGGER.info("PreProcessor");
      LOGGER.info("KAFKA: {} {} {}",
                  AppConfig.KAFKA_BOOTSTRAP_SERVERS,
                  AppConfig.KAFKA_APPLICATION_ID,
                  AppConfig.KAFKA_CLIENT_ID);
      ActorSystem.create(this.create(), "PreProcessor");
   }
}
