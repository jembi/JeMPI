package org.jembi.jempi.em;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.AppConfig;
import org.jembi.jempi.em.util.KafkaTopicManager;

public final class EM {

   private static final Logger LOGGER = LogManager.getLogger(EM.class);

   private EM() {
   }

   public static void main(final String[] args) {
       KafkaTopicManager topicManager = KafkaTopicManager.getInstance();
       try {
           topicManager.createTopic("Mahao_Topic", 3, (short) 1);
       } catch (Exception e) {
           LOGGER.debug(e.getMessage());
       }
      new EM().run();

   }

   public static Behavior<Void> create() {
      return Behaviors.setup(
            context -> {
               ActorRef<BackEnd.Event> backEnd = context.spawn(BackEnd.create(), "BackEnd");
               context.watch(backEnd);
               final FrontEndStream frontEndStream = new FrontEndStream();
               frontEndStream.open(context.getSystem(), backEnd);
               return Behaviors.receive(Void.class)
                               .onSignal(akka.actor.typed.Terminated.class, sig -> Behaviors.stopped())
                               .build();
            });
   }

   private void run() {
      LOGGER.info("KAFKA: {} {} {}",
                  AppConfig.KAFKA_BOOTSTRAP_SERVERS,
                  AppConfig.KAFKA_APPLICATION_ID,
                  AppConfig.KAFKA_CLIENT_ID);
      ActorSystem.create(EM.create(), "EMApp");
   }

}
