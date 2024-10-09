package org.jembi.jempi;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.Arrays;

public final class AppConfig {

   private static final Logger LOGGER = LogManager.getLogger(org.jembi.jempi.AppConfig.class);
   private static final Config SYSTEM_PROPERTIES = ConfigFactory.systemProperties();
   private static final Config SYSTEM_ENVIRONMENT = ConfigFactory.systemEnvironment();
   private static final Config CONFIG = new org.jembi.jempi.AppConfig.Builder().withSystemEnvironment()
                                                                               .withSystemProperties()
                                                                               .withOptionalRelativeFile(
                                                                                     "/conf/server.production.conf")
                                                                               .withOptionalRelativeFile(
                                                                                     "/conf/server.staging.conf")
                                                                               .withOptionalRelativeFile("/conf/server.test.conf")
                                                                               .withResource("application.local.conf")
                                                                               .withResource("application.conf")
                                                                               .build();
   public static final String KAFKA_BOOTSTRAP_SERVERS = CONFIG.getString("KAFKA_BOOTSTRAP_SERVERS");
   public static final String KAFKA_APPLICATION_ID = CONFIG.getString("KAFKA_APPLICATION_ID");
   public static final String KAFKA_CLIENT_ID = CONFIG.getString("KAFKA_CLIENT_ID");
   public static final String POSTGRESQL_IP = CONFIG.getString("POSTGRESQL_IP");
   public static final Integer POSTGRESQL_PORT = CONFIG.getInt("POSTGRESQL_PORT");
   public static final String POSTGRESQL_USER = CONFIG.getString("POSTGRESQL_USER");
   public static final String POSTGRESQL_PASSWORD = CONFIG.getString("POSTGRESQL_PASSWORD");
   public static final String POSTGRESQL_NOTIFICATIONS_DB = CONFIG.getString("POSTGRESQL_NOTIFICATIONS_DB");
   public static final String POSTGRESQL_AUDIT_DB = CONFIG.getString("POSTGRESQL_AUDIT_DB");
   public static final String POSTGRESQL_CONFIGURATION_DB = CONFIG.getString("POSTGRESQL_CONFIGURATION_DB");
   public static final Integer CONTROLLER_HTTP_PORT = CONFIG.getInt("CONTROLLER_HTTP_PORT");
   public static final String LINKER_IP = CONFIG.getString("LINKER_IP");
   public static final Integer LINKER_HTTP_PORT = CONFIG.getInt("LINKER_HTTP_PORT");
   public static final Level GET_LOG_LEVEL = Level.toLevel(CONFIG.getString("LOG4J2_LEVEL"));

   private static final String[] DGRAPH_ALPHA_HOSTS = CONFIG.getString("DGRAPH_HOSTS").split(",");
   private static final int[] DGRAPH_ALPHA_PORTS = Arrays.stream(CONFIG.getString("DGRAPH_PORTS").split(",")).mapToInt(s -> {
      try {
         return Integer.parseInt(s);
      } catch (NumberFormatException ex) {
         return Integer.MIN_VALUE;
      }
   }).toArray();

   public static String[] getDGraphHosts() {
      return DGRAPH_ALPHA_HOSTS;
   }
   public static int[] getDGraphPorts() {
      return DGRAPH_ALPHA_PORTS;
   }

   private AppConfig() {
   }

   private static class Builder {
      private Config conf = ConfigFactory.empty();

      Builder() {
         LOGGER.info("Loading configs first row is highest priority, second row is fallback and so on");
      }

      // This should return the current executing user path
      private static String getExecutionDirectory() {
         return SYSTEM_PROPERTIES.getString("user.dir");
      }

      Builder withSystemProperties() {
         conf = conf.withFallback(SYSTEM_PROPERTIES);
         LOGGER.info("Loaded system properties into config");
         return this;
      }

      Builder withSystemEnvironment() {
         conf = conf.withFallback(SYSTEM_ENVIRONMENT);
         LOGGER.info("Loaded system environment into config");
         return this;
      }

      Builder withResource(final String resource) {
         Config resourceConfig = ConfigFactory.parseResources(resource);
         String empty = resourceConfig.entrySet().isEmpty()
               ? " contains no values"
               : "";
         conf = conf.withFallback(resourceConfig);
         LOGGER.info("Loaded config file from resource ({}){}", resource, empty);
         return this;
      }

      Builder withOptionalFile(final String path) {
         File secureConfFile = new File(path);
         if (secureConfFile.exists()) {
            LOGGER.info("Loaded config file from path ({})", path);
            conf = conf.withFallback(ConfigFactory.parseFile(secureConfFile));
         } else {
            LOGGER.info("Attempted to load file from path ({}) but it was not found", path);
         }
         return this;
      }

      Builder withOptionalRelativeFile(final String path) {
         return withOptionalFile(getExecutionDirectory() + path);
      }

      Config build() {
         // Resolve substitutions.
         conf = conf.resolve();
         if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Logging properties. Make sure sensitive data such as passwords or secrets are not logged!");
            LOGGER.debug(conf.root().render());
         }
         return conf;
      }

   }

}
