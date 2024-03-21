package org.jembi.jempi.linker;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.Terminated;
import akka.actor.typed.javadsl.Behaviors;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.AppConfig;
import org.jembi.jempi.linker.backend.BackEnd;
import org.jembi.jempi.shared.config.Config;
import org.jembi.jempi.shared.models.CustomMU;
import org.jembi.jempi.shared.models.GlobalConstants;

import static org.jembi.jempi.shared.utils.AppUtils.OBJECT_MAPPER;

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
         if (!CustomMU.SEND_INTERACTIONS_TO_EM) {
            final SPInteractions spInteractions = SPInteractions.create(GlobalConstants.TOPIC_INTERACTION_LINKER);
            spInteractions.open(system, backEnd);
         }
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
      try {
         final var json = OBJECT_MAPPER.writeValueAsString(Config.INPUT_INTERFACE);
         LOGGER.info("Input Interface Config: {}", json);
      } catch (JsonProcessingException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
      }
      ActorSystem.create(this.create(), "LinkerApp");
   }

}
