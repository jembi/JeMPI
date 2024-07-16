package org.jembi.jempi.api;

import akka.actor.typed.*;
import akka.actor.typed.javadsl.Behaviors;
import akka.dispatch.MessageDispatcher;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.AppConfig;
import org.jembi.jempi.api.httpServer.HttpServer;
import org.jembi.jempi.libapi.BackEnd;

import java.util.UUID;

public final class APIKC {

   private static final Logger LOGGER = LogManager.getLogger(APIKC.class);
   private HttpServer httpServer;

   private APIKC() {
      LOGGER.info("API started.");
   }

   public static void main(final String[] args) {
      try {
         new APIKC().run();
      } catch (Exception e) {
         LOGGER.error(e.getLocalizedMessage(), e);
      }
   }

   public Behavior<Void> create() {
      return Behaviors.setup(context -> {
         final ActorSystem<Void> system = context.getSystem();
         final ActorRef<BackEnd.Event> backEnd = context.spawn(BackEnd.create(AppConfig.GET_LOG_LEVEL,
                                                                              AppConfig.getDGraphHosts(),
                                                                              AppConfig.getDGraphPorts(),
                                                                              AppConfig.POSTGRESQL_IP,
                                                                              AppConfig.POSTGRESQL_PORT,
                                                                              AppConfig.POSTGRESQL_USER,
                                                                              AppConfig.POSTGRESQL_PASSWORD,
                                                                              AppConfig.POSTGRESQL_NOTIFICATIONS_DB,
                                                                              AppConfig.POSTGRESQL_AUDIT_DB,
                                                                              AppConfig.KAFKA_BOOTSTRAP_SERVERS,
                                                                              "CLIENT_ID_API_KC-" + UUID.randomUUID(),
                                                                              AppConfig.SYSTEM_CONFIG_DIR,
                                                                              AppConfig.API_CONFIG_REFERENCE_FILENAME,
                                                                              AppConfig.API_CONFIG_MASTER_FILENAME,
                                                                              AppConfig.API_FIELDS_CONFIG_FILENAME),
                                                               "BackEnd");
         context.watch(backEnd);
         final DispatcherSelector selector = DispatcherSelector.fromConfig("akka.actor.default-dispatcher");
         final MessageDispatcher dispatcher = (MessageDispatcher) system.dispatchers().lookup(selector);
         httpServer = new HttpServer(dispatcher);
         httpServer.open("0.0.0.0", AppConfig.API_KC_HTTP_PORT, context.getSystem(), backEnd);
         return Behaviors.receive(Void.class).onSignal(Terminated.class, sig -> {
            LOGGER.info("API Server Terminated. Reason {}", sig);
            httpServer.close(context.getSystem());
            return Behaviors.stopped();
         }).build();
      });
   }

   private void run() {
      LOGGER.info("interface:port {}:{}", "0.0.0.0", AppConfig.API_KC_HTTP_PORT);
      try {
         ActorSystem.create(this.create(), "API-App");
      } catch (Exception e) {
         LOGGER.error("Unable to start the API", e);
      }
   }

}
