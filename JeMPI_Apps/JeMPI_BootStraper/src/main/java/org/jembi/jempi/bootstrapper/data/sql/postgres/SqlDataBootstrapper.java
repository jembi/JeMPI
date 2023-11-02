package org.jembi.jempi.bootstrapper.data.sql.postgres;

import org.jembi.jempi.bootstrapper.data.DataBootstrapper;

public class SqlDataBootstrapper extends DataBootstrapper {
    public SqlDataBootstrapper(String configFilePath) {
        super(configFilePath);
    }

    @Override
    public Boolean createSchema() {
        return null;
    }

    @Override
    public Boolean deleteData() {
        return null;
    }

    @Override
    public Boolean resetAll() {
        return null;
    }
}
