package org.jembi.jempi.shared.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.shared.config.input.JsonConfig;
import org.jembi.jempi.shared.utils.AppUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import com.fasterxml.jackson.core.JsonProcessingException;

public final class Config {

   private static final Logger LOGGER = LogManager.getLogger(Config.class);
   public static final JsonConfig JSON_CONFIG;
   public static final FieldsConfig FIELDS_CONFIG;
   public static final InputInterfaceConfig INPUT_INTERFACE_CONFIG;
   public static final ApiConfig API_CONFIG;
   public static final LinkerConfig LINKER_CONFIG;
   public static final DGraphConfig DGRAPH_CONFIG;
   private static String pgIp;
   private static int pgPort; 
   private static String pgDb;
   private static String pgUser;
   private static String pgPassword;
   static {
      JSON_CONFIG = retrieveConfigFromDatabase();
      FIELDS_CONFIG = new FieldsConfig(JSON_CONFIG);
      INPUT_INTERFACE_CONFIG = new InputInterfaceConfig(JSON_CONFIG);
      API_CONFIG = new ApiConfig(JSON_CONFIG);
      LINKER_CONFIG = new LinkerConfig(JSON_CONFIG);
      DGRAPH_CONFIG = new DGraphConfig(JSON_CONFIG);
   }

   private Config(
    final String ip,
    final int port,
    final String db,
    final String user,
    final String password) {
    pgIp = ip;
    pgPort = port;
    pgDb = db;
    pgUser = user;
    pgPassword = password;
   }

   public static Config create(
    final String ip,
    final int port,
    final String db,
    final String user,
    final String password) {
    return new Config(ip, port, db, user, password);
   }

   private static JsonConfig retrieveConfigFromDatabase() {
      String port = String.valueOf(pgPort);
      LOGGER.info(".............................................................");
              LOGGER.info("Retrieved configuration from database: {} {} {} {} {}", port, pgIp, pgDb, pgUser, pgPassword);
              LOGGER.info(".............................................................");
      // Database connection details (should be externalized in production)
      String url = "jdbc:postgresql://" + pgIp + ":" + port + "/" + pgDb;
      String jsonConfig = null;
      JsonConfig jsonConfigs = null;
      try (Connection conn = DriverManager.getConnection(url, pgUser, pgPassword);
           Statement stmt = conn.createStatement();
           ResultSet rs = stmt.executeQuery("SELECT json FROM configuration WHERE key = 'config'")) {
          
          if (rs.next()) {
              jsonConfig = rs.getString("json");
              jsonConfigs = AppUtils.OBJECT_MAPPER.readValue(jsonConfig, JsonConfig.class);
              LOGGER.info(".............................................................");
              LOGGER.info("Retrieved configuration from database: {}", jsonConfigs);
              LOGGER.info(".............................................................");
          } else {
              throw new RuntimeException("Configuration not found in the database");
          }
      } catch (SQLException | JsonProcessingException e) {
          throw new RuntimeException("Error retrieving configuration from database", e);
      }
      return jsonConfigs;
   }
}
