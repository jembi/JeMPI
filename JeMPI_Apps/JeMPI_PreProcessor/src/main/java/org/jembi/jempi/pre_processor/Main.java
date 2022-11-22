package org.jembi.jempi.pre_processor;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.Terminated;
import akka.actor.typed.javadsl.Behaviors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class Main {
    private static final Logger LOGGER = LogManager.getLogger(Main.class);

    public static void main(final String[] args) {
        new Main().run();
    }

    public Behavior<Void> create() {
        return Behaviors.setup(
                context -> {
//                    ActorRef<BackEnd.Event> backEnd = context.spawn(BackEnd.create(), "BackEnd");
//                    context.watch(backEnd);
//                    final JournalEntryStreamAsync journalEntryStreamAsync = new JournalEntryStreamAsync();
//                    journalEntryStreamAsync.open(context.getSystem(), backEnd);
                    return Behaviors.receive(Void.class)
                            .onSignal(Terminated.class,
                                    sig -> {
//                                        journalEntryStreamAsync.close(context.getSystem());
                                        return Behaviors.stopped();
                                    })
                            .build();
                });
    }

    private void run() {
        LOGGER.info("PreProcessor");
//        LOGGER.info("KAFKA: {} {} {}",
//                AppConfig.KAFKA_BOOTSTRAP_SERVERS,
//                AppConfig.KAFKA_APPLICATION_ID_JOURNAL,
//                AppConfig.KAFKA_CLIENT_ID_JOURNAL);

//        var hello1 = new HelloScala().hello();
//        var hello2 = HelloScala$.MODULE$.hello();
//        var hello3 = HelloScala$.MODULE$.hallo();
//        LOGGER.debug("{} {} {}", hello1, hello2, hello3);

        ActorSystem.create(this.create(), "LinkerApp");
    }
}
