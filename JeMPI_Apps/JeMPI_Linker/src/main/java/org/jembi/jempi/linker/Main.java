package org.jembi.jempi.linker;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.Terminated;
import akka.actor.typed.javadsl.Behaviors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.AppConfig;
import org.jembi.jempi.linker.backend.BackEnd;

public final class Main {

   private static final Logger LOGGER = LogManager.getLogger(Main.class);
   private HttpServer httpServer;

   private Main() {
   }

   public static void main(final String[] args) {
      new Main().run();
   }

   public Behavior<Void> create() {
      return Behaviors.setup(context -> {
         final var system = context.getSystem();
         final ActorRef<BackEnd.Request> backEnd = context.spawn(BackEnd.create(), "BackEnd");
         context.watch(backEnd);
         final SPInteractions spInteractions = SPInteractions.create();
         spInteractions.open(system, backEnd);
         final SPMU spMU = new SPMU();
         spMU.open(system, backEnd);
         httpServer = HttpServer.create();
         httpServer.open(system, backEnd);
         return Behaviors.receive(Void.class).onSignal(Terminated.class, sig -> {
            httpServer.close(system);
            return Behaviors.stopped();
         }).build();
      });
   }

   private void run() {
      LOGGER.info("KAFKA: {}", AppConfig.KAFKA_BOOTSTRAP_SERVERS);
      ActorSystem.create(this.create(), "LinkerApp");
   }

}
