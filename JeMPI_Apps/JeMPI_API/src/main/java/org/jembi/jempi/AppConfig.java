package org.jembi.jempi;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

public final class AppConfig {

   private static final Logger LOGGER = LogManager.getLogger(AppConfig.class);
   private static final Config SYSTEM_PROPERTIES = ConfigFactory.systemProperties();
   private static final Config SYSTEM_ENVIRONMENT = ConfigFactory.systemEnvironment();
   public static final Config CONFIG = new Builder()
         .withSystemEnvironment()
         .withSystemProperties()
         .withOptionalRelativeFile("/conf/server.production.conf")
         .withOptionalRelativeFile("/conf/server.staging.conf")
         .withOptionalRelativeFile("/conf/server.test.conf")
         .withResource("application.local.conf")
         .withResource("application.conf")
         .build();

   public static final String KAFKA_BOOTSTRAP_SERVERS = CONFIG.getString("kafka.bootstrap.servers");
   public static final String KAFKA_APPLICATION_ID = CONFIG.getString("kafka.application-id");
   public static final String KAFKA_CLIENT_ID = CONFIG.getString("kafka.client-id");
   public static final String KAFKA_GROUP_ID = CONFIG.getString("kafka.group-id");

   public static final String HTTP_SERVER_HOST = CONFIG.getString("http-server.host");
   public static final Integer HTTP_SERVER_PORT = CONFIG.getInt("http-server.port");
   public static final String DGRAPH_ALPHA1_HOST = CONFIG.getString("dgraph.alpha1.host");
   public static final int DGRAPH_ALPHA1_PORT = CONFIG.getInt("dgraph.alpha1.port");
   public static final String DGRAPH_ALPHA2_HOST = CONFIG.getString("dgraph.alpha2.host");
   public static final int DGRAPH_ALPHA2_PORT = CONFIG.getInt("dgraph.alpha2.port");
   public static final String DGRAPH_ALPHA3_HOST = CONFIG.getString("dgraph.alpha3.host");
   public static final int DGRAPH_ALPHA3_PORT = CONFIG.getInt("dgraph.alpha3.port");

   public static final String POSTGRES_SERVER = CONFIG.getString("postgres.server");

   public static final String SESSION_SECRET = CONFIG.getString("akka.http.session.server-secret");

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
         String empty = resourceConfig.entrySet().isEmpty() ? " contains no values" : "";
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
            LOGGER.debug("Logging properties. Make sure sensitive data such as passwords or secrets are not " +
                         "logged!");
            LOGGER.debug(conf.root().render());
         }
         return conf;
      }

   }

}
