package org.jembi.jempi.libapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.shared.models.ConfigurationModel.Configuration;
import org.jembi.jempi.shared.models.FieldsConfiguration;
import org.jembi.jempi.shared.utils.AppUtils;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of PostgresClientDao interface for interacting with a PostgreSQL database.
 * This class can be extended to provide custom behavior for database operations.
 */
public final class PostgresClientDaoImpl implements PostgresClientDao {
    private static final Logger LOGGER = LogManager.getLogger(PostgresClientDaoImpl.class);
    private final PsqlClient psqlClient;

    /**
     * Constructs a new PostgresClientDaoImpl with the given database connection parameters.
     *
     * @param pgIP       The IP address of the PostgreSQL server
     * @param pgPort     The port number of the PostgreSQL server
     * @param pgDatabase The name of the database to connect to
     * @param pgUser     The username for database authentication
     * @param pgPassword The password for database authentication
     */
    private PostgresClientDaoImpl(
            final String pgIP,
            final int pgPort,
            final String pgDatabase,
            final String pgUser,
            final String pgPassword) {
        this.psqlClient = new PsqlClient(pgIP, pgPort, pgDatabase, pgUser, pgPassword);
    }

    /**
     * Creates a new instance of PostgresClientDaoImpl with the given database connection parameters.
     *
     * @param ip       The IP address of the PostgreSQL server
     * @param port     The port number of the PostgreSQL server
     * @param db       The name of the database to connect to
     * @param user     The username for database authentication
     * @param password The password for database authentication
     */
    public static PostgresClientDaoImpl create(
            final String ip,
            final int port,
            final String db,
            final String user,
            final String password) {
        return new PostgresClientDaoImpl(
            ip,
            port,
            db,
            user,
            password);
    }

    /**
     * Establishes a connection to the PostgreSQL database.
     * This method can be overridden to provide custom connection logic.
     *
     * @throws SQLException if a database access error occurs
     */
    @Override
    public void connect() {
        LOGGER.info("Connecting to PostgreSQL database");
        psqlClient.connect();
        LOGGER.info("Successfully connected to PostgreSQL database");
    }

    /**
     * Closes the connection to the PostgreSQL database.
     * This method can be overridden to provide custom disconnection logic.
     *
     * @throws SQLException if a database access error occurs
     */
    @Override
    public void disconnect()  {
        LOGGER.info("Disconnecting from PostgreSQL database");
        psqlClient.disconnect();
        LOGGER.info("Successfully disconnected from PostgreSQL database");
    }

    /**
     * Retrieves the current configuration from the database.
     * This method can be overridden to provide custom configuration retrieval logic.
     *
     * @return The Configuration object, or null if no configuration is found
     * @throws SQLException if a database access error occurs or the retrieved JSON is invalid
     */
    @Override
    public Configuration getConfiguration() {
        this.connect();
        LOGGER.info("Retrieving configuration from database");
        String sql = "SELECT json FROM CONFIGURATION WHERE key = 'config' ORDER BY id DESC LIMIT 1";
        try (PreparedStatement preparedStatement = psqlClient.prepareStatement(sql);
             ResultSet rs = preparedStatement.executeQuery()) {

            if (rs.next()) {
                String configFileContent = rs.getString("json");
                ObjectMapper mapper = new ObjectMapper();
                Configuration config = mapper.readValue(configFileContent, Configuration.class);
                LOGGER.info("Successfully retrieved configuration from database");
                return config;
            } else {
                LOGGER.info("No configuration found in the database");
                return null;
            }
        } catch (Exception e) {
            LOGGER.error(e);
         }
         this.disconnect();
         return null;
    }

    /**
     * Retrieves the fields configuration from the database.
     * This method can be overridden to provide custom fields configuration retrieval logic.
     *
     * @return A List of FieldsConfiguration.Field objects, or null if no configuration is found
     * @throws SQLException if a database access error occurs or the retrieved JSON is invalid
     */
    @Override
    public List<FieldsConfiguration.Field> getFieldsConfiguration() {
        this.connect();
        LOGGER.info("Retrieving fields configuration from database");
        String sql = "SELECT json FROM CONFIGURATION WHERE key = 'config-api' ORDER BY id DESC LIMIT 1";
        try (PreparedStatement preparedStatement = psqlClient.prepareStatement(sql);
             ResultSet rs = preparedStatement.executeQuery()) {
            if (rs.next()) {
                String configFileContent = rs.getString("json");
                FieldsConfiguration fieldsConfiguration = AppUtils.OBJECT_MAPPER.readValue(configFileContent, FieldsConfiguration.class);
                ArrayList<FieldsConfiguration.Field> fields = new ArrayList<>();
                if (fieldsConfiguration != null && fieldsConfiguration.systemFields() != null && fieldsConfiguration.fields() != null) {
                    fields.addAll(fieldsConfiguration.systemFields());
                    fields.addAll(fieldsConfiguration.fields());
                }
                LOGGER.info("Successfully retrieved fields configuration from database");
                return fields;
            } else {
                LOGGER.info("No fields configuration found in the database");
                return null;
            }
        } catch (Exception e) {
            LOGGER.error(e);
        }
        this.disconnect();
        return null;
    }

    /**
     * Saves the given configuration to the database.
     * This method can be overridden to provide custom configuration saving logic.
     *
     * @param configuration The Configuration object to be saved
     * @throws SQLException if a database access error occurs or the configuration cannot be converted to JSON
     */
    @Override
    public void saveConfiguration(final Configuration configuration) {
        LOGGER.info("Saving configuration to database");
        this.connect();
        String sql = "INSERT INTO CONFIGURATION (key, json) VALUES (?, ?::json)";
        try (PreparedStatement preparedStatement = psqlClient.prepareStatement(sql)) {
            String jsonConfig = AppUtils.OBJECT_MAPPER.writeValueAsString(configuration);
            preparedStatement.setString(1, "config-api");
            preparedStatement.setString(2, jsonConfig);
            int rowsAffected = preparedStatement.executeUpdate();
            LOGGER.info("Successfully saved configuration to database. Rows affected: {}", rowsAffected);
        } catch (Exception e) {
            LOGGER.error(e);
        }
        this.disconnect();
    }
}
