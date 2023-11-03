package org.jembi.jempi.bootstrapper.data.graph.dgraph;

import org.jembi.jempi.bootstrapper.data.DataBootstrapper;

public class DgraphDataBootstrapper extends DataBootstrapper {
    public DgraphDataBootstrapper(String configFilePath) {
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
