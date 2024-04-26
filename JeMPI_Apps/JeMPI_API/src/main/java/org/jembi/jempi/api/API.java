package org.jembi.jempi.api;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.Terminated;
import akka.actor.typed.javadsl.Behaviors;
import io.github.jopenlibs.vault.Vault;
import io.github.jopenlibs.vault.VaultConfig;
import io.github.jopenlibs.vault.VaultException;
import io.github.jopenlibs.vault.response.LogicalResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.AppConfig;
import org.jembi.jempi.libapi.BackEnd;
import org.jembi.jempi.libapi.JsonFieldsConfig;

import java.util.UUID;

public final class API {

   private static final Logger LOGGER = LogManager.getLogger(API.class);
   private static final String CONFIG_RESOURCE_FILE_NAME = "config-api.json";
   private final JsonFieldsConfig jsonFieldsConfig = new JsonFieldsConfig(CONFIG_RESOURCE_FILE_NAME);
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

   public Behavior<Void> create(final String encryptionKey) {
      LOGGER.info("encryptionKey ->" + encryptionKey);
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
               "CLIENT_ID_API-" + UUID.randomUUID()), "BackEnd");
         context.watch(backEnd);
         httpServer = HttpServer.create();
         httpServer.open("0.0.0.0", AppConfig.API_HTTP_PORT, context.getSystem(), backEnd, jsonFieldsConfig.jsonFields);
         return Behaviors.receive(Void.class).onSignal(Terminated.class, sig -> {
            httpServer.close(context.getSystem());
            return Behaviors.stopped();
         }).build();
      });
   }

   private void run() {
      LOGGER.info("interface:port {}:{}", "0.0.0.0", AppConfig.API_HTTP_PORT);
      try {
         LOGGER.info("Loading fields configuration file ");
         jsonFieldsConfig.load(CONFIG_RESOURCE_FILE_NAME);
         LOGGER.info("Fields configuration file successfully loaded");

         String encryptionKey = fetchKeyFromVault();
         ActorSystem.create(this.create(encryptionKey), "API-App");
      } catch (Exception e) {
         LOGGER.error("Unable to start the API", e);
      }
   }

   private String fetchKeyFromVault() {
      String key = null;
      try {
         final VaultConfig config = new VaultConfig()
               .address("http://172.20.10.3:8200")
               .token("root")
               .engineVersion(2)
               .build();
         Vault vault = Vault.create(config);
         LogicalResponse response = vault.logical().read("secret/jeMPI");
         key = response.getData().get("jempi_encryption_token");
      } catch (VaultException e) {
         LOGGER.error("Error fetching key from Vault", e);
      }
      return key;
   }

}
