package org.jembi.jempi.async_receiver;

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
                    return Behaviors.receive(Void.class)
                            .onSignal(Terminated.class,
                                    sig -> {
                                        return Behaviors.stopped();
                                    }).build();
                });
    }

    private void run() {
        LOGGER.info("AsyncReceiver");
        ActorSystem.create(this.create(), "AsyncReceiver");
    }
}
