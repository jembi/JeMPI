package org.jembi.jempi;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;

public final class AppConfig {

    private static final Logger LOGGER = LogManager.getLogger(AppConfig.class);
    private static final Config SYSTEM_PROPERTIES = ConfigFactory.systemProperties();
    private static final Config SYSTEM_ENVIRONMENT = ConfigFactory.systemEnvironment();
    public static final Config CONFIG = new Builder()
            .withSystemEnvironment()
            .withSystemProperties()
            .withResource("application.conf")
            .build();

    public static final Integer MONITOR_HTTP_PORT = CONFIG.getInt("MONITOR_HTTP_PORT");
    public static final String POSTGRESQL_IP = CONFIG.getString("POSTGRESQL_IP");
    public static final Integer POSTGRESQL_PORT = CONFIG.getInt("POSTGRESQL_PORT");

    public static final String POSTGRESQL_USER = CONFIG.getString("POSTGRESQL_USER");
    public static final String POSTGRESQL_PASSWORD = CONFIG.getString("POSTGRESQL_PASSWORD");
    public static final String POSTGRESQL_DATABASE = CONFIG.getString("POSTGRESQL_DATABASE");
    private static final String[] DGRAPH_ALPHA_HOSTS = CONFIG.getString("DGRAPH_HOSTS").split(",");
    private static final int[] DGRAPH_ALPHA_HTTP_PORTS = Arrays.stream(CONFIG.getString("DGRAPH_HTTP_PORTS").split(",")).mapToInt(s -> {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException ex) {
            return Integer.MIN_VALUE;
        }
    }).toArray();
    private static final int[] DGRAPH_ALPHA_PORTS = Arrays.stream(CONFIG.getString("DGRAPH_PORTS").split(",")).mapToInt(s -> {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException ex) {
            return Integer.MIN_VALUE;
        }
    }).toArray();
    public static final Integer API_HTTP_PORT = CONFIG.getInt("API_HTTP_PORT");
    public static final String API_IP = CONFIG.getString("API_IP");
    public static final Level GET_LOG_LEVEL = Level.toLevel(CONFIG.getString("LOG4J2_LEVEL"));
    public static String[] getDGraphHosts() {
        return DGRAPH_ALPHA_HOSTS;
    }
    public static int[] getDGraphPorts() {
        return DGRAPH_ALPHA_PORTS;
    }
    public static int[] getDGraphHttpPorts() {
        return DGRAPH_ALPHA_HTTP_PORTS;
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
            String empty = resourceConfig.entrySet().isEmpty() ? " contains no values" : "";
            conf = conf.withFallback(resourceConfig);
            LOGGER.info("Loaded config file from resource ({}){}", resource, empty);
            return this;
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
