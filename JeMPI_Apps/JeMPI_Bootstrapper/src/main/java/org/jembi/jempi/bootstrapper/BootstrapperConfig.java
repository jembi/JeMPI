package org.jembi.jempi.bootstrapper;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.Arrays;

public class BootstrapperConfig {

   public final  String POSTGRESQL_IP;
   public final Integer POSTGRESQL_PORT;
   public final String POSTGRESQL_USER;
   public final String POSTGRESQL_PASSWORD;
   public final String POSTGRESQL_DATABASE;
   public final String POSTGRESQL_USERS_DB;
   public final String POSTGRESQL_NOTIFICATIONS_DB;
   public final String POSTGRESQL_CONFIGURATION_DB;
   public final String POSTGRESQL_AUDIT_DB;
   public final String POSTGRESQL_KC_TEST_DB;
   public final String KAFKA_BOOTSTRAP_SERVERS;
   public final String KAFKA_APPLICATION_ID;
   public final String[] DGRAPH_ALPHA_HOSTS;
   public final int[] DGRAPH_ALPHA_PORTS;

   public BootstrapperConfig(final Config parsedConfig) {
      POSTGRESQL_IP = parsedConfig.getString("POSTGRESQL_IP");
      POSTGRESQL_PORT = parsedConfig.getInt("POSTGRESQL_PORT");
      POSTGRESQL_USER = parsedConfig.getString("POSTGRESQL_USER");
      POSTGRESQL_PASSWORD = parsedConfig.getString("POSTGRESQL_PASSWORD");

      POSTGRESQL_DATABASE = parsedConfig.getString("POSTGRESQL_DATABASE");
      POSTGRESQL_USERS_DB = parsedConfig.getString("POSTGRESQL_USERS_DB");
      POSTGRESQL_NOTIFICATIONS_DB = parsedConfig.getString("POSTGRESQL_NOTIFICATIONS_DB");
      POSTGRESQL_AUDIT_DB = parsedConfig.getString("POSTGRESQL_AUDIT_DB");
      POSTGRESQL_CONFIGURATION_DB = parsedConfig.getString("POSTGRESQL_CONFIGURATION_DB");
      POSTGRESQL_KC_TEST_DB = parsedConfig.getString("POSTGRESQL_KC_TEST_DB");

      KAFKA_BOOTSTRAP_SERVERS = parsedConfig.getString("KAFKA_BOOTSTRAP_SERVERS");
      KAFKA_APPLICATION_ID = parsedConfig.getString("KAFKA_APPLICATION_ID");
      DGRAPH_ALPHA_HOSTS = parsedConfig.getString("DGRAPH_HOSTS").split(",");
      DGRAPH_ALPHA_PORTS = Arrays.stream(parsedConfig.getString("DGRAPH_PORTS").split(",")).mapToInt(s -> {
         try {
            return Integer.parseInt(s);
         } catch (NumberFormatException ex) {
            return Integer.MIN_VALUE;
         }
      }).toArray();
   }

   public static BootstrapperConfig create(
         final String filepath,
         final Logger logger) {
      return new BootstrapperConfig(new Builder(logger).withOptionalFile(filepath)
                                                       .withSystemEnvironment()
                                                       .withSystemProperties()
                                                       .build());
   }

   private static class Builder {

      private static final Config SYSTEM_PROPERTIES = ConfigFactory.systemProperties();
      private static final Config SYSTEM_ENVIRONMENT = ConfigFactory.systemEnvironment();
      private final Logger logger;
      private Config conf = ConfigFactory.empty();

      Builder(final Logger logger) {
         this.logger = logger;
      }

      // This should return the current executing user path
      private static String getExecutionDirectory() {
         return SYSTEM_PROPERTIES.getString("user.dir");
      }

      Builder withSystemProperties() {
         conf = conf.withFallback(SYSTEM_PROPERTIES);
         return this;
      }

      Builder withSystemEnvironment() {
         conf = conf.withFallback(SYSTEM_ENVIRONMENT);
         return this;
      }

      Builder withOptionalFile(final String path) {
         if (path == null) {
            return this;
         }
         File secureConfFile = new File(path);

         if (!secureConfFile.isAbsolute()) {
            secureConfFile = new File(getExecutionDirectory() + path);
         }
         if (secureConfFile.exists()) {
            this.logger.info("Loaded config file from path ({})", path);
            conf = conf.withFallback(ConfigFactory.parseFile(secureConfFile));
         } else {
            this.logger.info("Attempted to load file from path ({}) but it was not found", path);
         }
         return this;
      }

      Config build() {
         conf = conf.resolve();
         return conf;
      }

   }

}


