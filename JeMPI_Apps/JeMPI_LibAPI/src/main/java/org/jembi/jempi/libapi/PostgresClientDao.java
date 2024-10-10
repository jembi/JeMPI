package org.jembi.jempi.libapi;

import org.jembi.jempi.shared.models.ConfigurationModel.Configuration;
import org.jembi.jempi.shared.models.FieldsConfiguration;
import java.util.List;

public interface PostgresClientDao {
    void connect();
    void disconnect();
    Configuration getConfiguration();
    List<FieldsConfiguration.Field> getFieldsConfiguration();
    void saveConfiguration(Configuration configuration);
}
