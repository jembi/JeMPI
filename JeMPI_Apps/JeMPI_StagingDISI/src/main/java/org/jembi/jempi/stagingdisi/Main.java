package org.jembi.jempi.stagingdisi;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.Terminated;
import akka.actor.typed.javadsl.Behaviors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {

   private static final Logger LOGGER = LogManager.getLogger(Main.class);

   private FrontEndRest frontEndRest;
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
               frontEndRest = new FrontEndRest();
               frontEndRest.open(context.getSystem(), backEndActor);
               return Behaviors.receive(Void.class)
                               .onSignal(Terminated.class,
                                         sig -> {
                                            frontEndRest.close(context.getSystem());
                                            return Behaviors.stopped();
                                         })
                               .build();
            });
   }

   private void run() {
      LOGGER.info("StagingDISI");
      ActorSystem.create(this.create(), "StagingDISI");
   }
}

