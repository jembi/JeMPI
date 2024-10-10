package org.jembi.jempi.libapi;

import org.jembi.jempi.shared.models.ConfigurationModel.Configuration;
import org.jembi.jempi.shared.models.FieldsConfiguration;
import java.util.List;

public interface PostgresClientDao {
    void connect();
    void disconnect();
    Configuration getConfiguration(String configKey);
    List<FieldsConfiguration.Field> getFieldsConfiguration(String configKey);
    void saveConfiguration(Configuration configuration, String configKey);
}
