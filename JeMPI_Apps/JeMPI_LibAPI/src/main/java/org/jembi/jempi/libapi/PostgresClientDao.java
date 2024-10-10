package org.jembi.jempi.libapi;

import org.jembi.jempi.shared.models.ConfigurationModel.Configuration;
import org.jembi.jempi.shared.models.FieldsConfiguration;

import java.sql.SQLException;
import java.util.List;

public interface PostgresClientDao {
    void connect() throws SQLException;
    void disconnect() throws SQLException;
    Configuration getConfiguration() throws SQLException;
    List<FieldsConfiguration.Field> getFieldsConfiguration() throws SQLException;
    void saveConfiguration(Configuration configuration) throws SQLException;
}
