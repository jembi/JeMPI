package org.jembi.jempi.linker;

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
   private EntityStreamSync entityStreamSync;

   private Main() {
   }

   public static void main(final String[] args) {
      new Main().run();
   }

   public Behavior<Void> create() {
      return Behaviors.setup(
            context -> {
               final var system = context.getSystem();
               final ActorRef<BackEnd.Event> backEnd = context.spawn(BackEnd.create(), "BackEnd");
               context.watch(backEnd);
               final EntityStreamAsync entityStreamAsync = EntityStreamAsync.create();
               entityStreamAsync.open(system, backEnd);
               final FrontEndMUStream frontEndMUStream = new FrontEndMUStream();
               frontEndMUStream.open(system, backEnd);
               entityStreamSync = EntityStreamSync.create();
               entityStreamSync.open(system, backEnd);
               return Behaviors.receive(Void.class)
                               .onSignal(Terminated.class,
                                         sig -> {
                                            entityStreamSync.close(system);
                                            return Behaviors.stopped();
                                         })
                               .build();
            });
   }

   private void run() {
      LOGGER.info("KAFKA: {} {} {} {} {}",
                  AppConfig.KAFKA_BOOTSTRAP_SERVERS,
                  AppConfig.KAFKA_APPLICATION_ID_ENTITIES,
                  AppConfig.KAFKA_CLIENT_ID_ENTITIES,
                  AppConfig.KAFKA_APPLICATION_ID_MU,
                  AppConfig.KAFKA_CLIENT_ID_MU);

      var hello1 = new HelloScala().hello();
      var hello2 = HelloScala$.MODULE$.hello();
      var hello3 = HelloScala$.MODULE$.hallo();
      LOGGER.debug("{} {} {}", hello1, hello2, hello3);

      ActorSystem.create(this.create(), "LinkerApp");
   }

}
