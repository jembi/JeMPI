package org.jembi.jempi.api;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.Terminated;
import akka.actor.typed.javadsl.Behaviors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.AppConfig;
import org.jembi.jempi.libapi.BackEnd;

import java.util.UUID;

public final class API {

   private static final Logger LOGGER = LogManager.getLogger(API.class);
   private HttpServer httpServer;

   private API() {
      LOGGER.info("API started.");
   }

   public static void main(final String[] args) {
      try {
         new API().run();
      } catch (Exception e) {
         LOGGER.error(e.getLocalizedMessage(), e);
      }
   }

   public Behavior<Void> create() {
      return Behaviors.setup(context -> {
         ActorRef<BackEnd.Event> backEnd = context.spawn(BackEnd.create(AppConfig.GET_LOG_LEVEL,
                                                                        AppConfig.getDGraphHosts(),
                                                                        AppConfig.getDGraphPorts(),
                                                                        AppConfig.POSTGRESQL_IP,
                                                                        AppConfig.POSTGRESQL_PORT,
                                                                        AppConfig.POSTGRESQL_USER,
                                                                        AppConfig.POSTGRESQL_PASSWORD,
                                                                        AppConfig.POSTGRESQL_NOTIFICATIONS_DB,
                                                                        AppConfig.POSTGRESQL_AUDIT_DB,
                                                                        AppConfig.KAFKA_BOOTSTRAP_SERVERS,
                                                                        "CLIENT_ID_API-" + UUID.randomUUID(),
                                                                        AppConfig.SYSTEM_CONFIG_DIR,
                                                                        AppConfig.API_CONFIG_REFERENCE_FILENAME,
                                                                        AppConfig.API_CONFIG_MASTER_FILENAME,
                                                                        AppConfig.API_FIELDS_CONFIG_FILENAME), "BackEnd");
         context.watch(backEnd);
         httpServer = HttpServer.create();
         httpServer.open("0.0.0.0", AppConfig.API_HTTP_PORT, context.getSystem(), backEnd);
         return Behaviors.receive(Void.class).onSignal(Terminated.class, sig -> {
            httpServer.close(context.getSystem());
            return Behaviors.stopped();
         }).build();
      });
   }

   private void run() {
      LOGGER.info("interface:port {}:{}", "0.0.0.0", AppConfig.API_HTTP_PORT);
      try {
         ActorSystem.create(this.create(), "API-App");
      } catch (Exception e) {
         LOGGER.error("Unable to start the API", e);
      }
   }

}
