package org.jembi.jempi.linker;

import akka.actor.typed.*;
import akka.actor.typed.javadsl.Behaviors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.AppConfig;
import org.jembi.jempi.linker.backend.BackEnd;
import org.jembi.jempi.shared.models.GlobalConstants;

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
         final ActorRef<BackEnd.Request> backEnd = context.spawn(
               Behaviors.supervise(BackEnd.create())
                        .onFailure(SupervisorStrategy.resume()
                                                     .withLoggingEnabled(true)),
               "BackEnd"
                                                                );
         context.watch(backEnd);
         final SPInteractions spInteractions = SPInteractions.create(GlobalConstants.TOPIC_INTERACTION_LINKER);
         spInteractions.open(system, backEnd);
         final SPMU spMU = new SPMU();
         spMU.open(system, backEnd);
         LOGGER.info("SPMU opened");
         httpServer = HttpServer.create();
         httpServer.open(system, backEnd);

         return Behaviors.supervise(
               Behaviors.receive(Void.class)
                        .onSignal(Terminated.class, sig -> {
                           LOGGER.info("Terminating due to: {}", sig.getRef());
                           httpServer.close(system);
                           return Behaviors.stopped();
                        })
                        .onMessage(Void.class, msg -> {
                           LOGGER.info("*** Actor restarted ***");
                           return Behaviors.same();
                        })
                        .build()
                                   ).onFailure(SupervisorStrategy.resume()
                                                                 .withLoggingEnabled(true)
                                               // Enable logging for the supervisor strategy
                                              );
      });
   }

   private void innerRun() {
      LOGGER.info("KAFKA: {}", AppConfig.KAFKA_BOOTSTRAP_SERVERS);
//      try {
//         final var cfg1 = OBJECT_MAPPER.writeValueAsString(INPUT_INTERFACE_CONFIG);
//         final var cfg2 = OBJECT_MAPPER.writeValueAsString(API_CONFIG);
//         final var cfg3 = OBJECT_MAPPER.writeValueAsString(LINKER_CONFIG);
//         LOGGER.info("Input Interface Config: {}", cfg1);
//         LOGGER.info("Api Config: {}", cfg2);
//         LOGGER.info("Linker Config: {}", cfg3);
//      } catch (JsonProcessingException e) {
//         LOGGER.error(e.getLocalizedMessage(), e);
//      }
      ActorSystem.create(this.create(), "LinkerApp");
   }

   private void run() {
      try {
         innerRun();
      } catch (final Exception e) {
         LOGGER.error(e.getLocalizedMessage(), e);
      }
   }

}
