package org.jembi.jempi.api;

import akka.actor.typed.*;
import akka.actor.typed.javadsl.Behaviors;
import akka.dispatch.MessageDispatcher;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.AppConfig;
import org.jembi.jempi.libapi.BackEnd;
import org.jembi.jempi.libapi.NotificationStreamProcessor;
import org.jembi.jempi.libapi.JsonFieldsConfig;

public final class APIKC {

   private static final Logger LOGGER = LogManager.getLogger(APIKC.class);
   private static final String CONFIG_RESOURCE_FILE_NAME = "/config-api.json";
   private final JsonFieldsConfig jsonFieldsConfig = new JsonFieldsConfig(CONFIG_RESOURCE_FILE_NAME);
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
         final ActorRef<BackEnd.Event> backEnd = context.spawn(BackEnd.create(AppConfig.getDGraphHosts(),
                                                                              AppConfig.getDGraphPorts(),
                                                                              AppConfig.POSTGRESQL_USER,
                                                                              AppConfig.POSTGRESQL_PASSWORD,
                                                                              AppConfig.POSTGRESQL_DATABASE),
                                                               "BackEnd");
         context.watch(backEnd);
         final var notificationsSteam = new NotificationStreamProcessor();
         notificationsSteam.open(AppConfig.POSTGRESQL_DATABASE,
                                 AppConfig.POSTGRESQL_PASSWORD,
                                 AppConfig.KAFKA_APPLICATION_ID,
                                 AppConfig.KAFKA_CLIENT_ID,
                                 AppConfig.KAFKA_BOOTSTRAP_SERVERS);
         final DispatcherSelector selector = DispatcherSelector.fromConfig("akka.actor.default-dispatcher");
         final MessageDispatcher dispatcher = (MessageDispatcher) system.dispatchers().lookup(selector);
         httpServer = new HttpServer(dispatcher);
         httpServer.open("0.0.0.0",
                         AppConfig.HTTP_SERVER_PORT,
                         context.getSystem(),
                         backEnd,
                         jsonFieldsConfig.jsonFields);
         return Behaviors.receive(Void.class).onSignal(Terminated.class, sig -> {
            httpServer.close(context.getSystem());
            return Behaviors.stopped();
         }).build();
      });
   }

   private void run() {
      LOGGER.info("interface:port {}:{}", "0.0.0.0", AppConfig.HTTP_SERVER_PORT);
      try {
         LOGGER.info("Loading fields configuration file ");
         jsonFieldsConfig.load(CONFIG_RESOURCE_FILE_NAME);
         LOGGER.info("Fields configuration file successfully loaded");
         ActorSystem.create(this.create(), "API-App");
      } catch (Exception e) {
         LOGGER.error("Unable to start the API", e);
      }
   }

}
