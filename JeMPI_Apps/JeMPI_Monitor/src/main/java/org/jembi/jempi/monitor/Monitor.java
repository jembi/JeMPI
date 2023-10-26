package org.jembi.jempi.monitor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.Terminated;
import akka.actor.typed.javadsl.Behaviors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.libapi.BackEnd;
import org.jembi.jempi.AppConfig;
import org.jembi.jempi.monitor.lib.LibRegistry;

import java.util.UUID;
public class Monitor {

    private static final Logger LOGGER = LogManager.getLogger(Monitor.class);
    private RestHttpServer restServer;

    private Monitor() {
        LOGGER.info("JeMPI Monitor service started.");
    }

    public static void main(final String[] args) {
        try {
            new Monitor().run();
        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage(), e);
        }
    }

    public Behavior<Void> create() {
        return Behaviors.setup(context -> {
            ActorRef<Void> starter = context.spawn(Behaviors.setup(starterContext -> {
                try{
                    LibRegistry libRegistry = new LibRegistry(AppConfig.GET_LOG_LEVEL,
                            AppConfig.getDGraphHosts(),
                            AppConfig.getDGraphPorts(),
                            AppConfig.POSTGRESQL_IP,
                            AppConfig.POSTGRESQL_PORT,
                            AppConfig.POSTGRESQL_USER,
                            AppConfig.POSTGRESQL_PASSWORD,
                            AppConfig.POSTGRESQL_DATABASE);

                    restServer = RestHttpServer.create();
                    restServer.open("0.0.0.0",
                            AppConfig.MONITOR_HTTP_PORT,
                            context,
                            libRegistry);

                }
                catch (Exception e){
                    LOGGER.error("Monitoring Server encountered an error", e);
                    throw e;
                }

                return Behaviors.empty();
            }), "start");

            context.watch(starter);

            return Behaviors.receive(Void.class).onSignal(Terminated.class, sig -> {
                LOGGER.info("Monitoring Server Terminated. Reason {}", sig);
                restServer.close(context.getSystem());
                return Behaviors.stopped();
            }).build();
        });
    }

    private void run() {
        LOGGER.info("interface:port {}:{}", "0.0.0.0", AppConfig.MONITOR_HTTP_PORT);
        try {
            ActorSystem.create(this.create(), "Monitor-App");
        } catch (Exception e) {
            LOGGER.error("Unable to start JeMPI Monitoring service", e);
        }
    }
}
