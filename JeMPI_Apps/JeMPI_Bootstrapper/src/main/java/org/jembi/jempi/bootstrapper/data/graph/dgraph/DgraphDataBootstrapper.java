package org.jembi.jempi.bootstrapper.data.graph.dgraph;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.bootstrapper.data.DataBootstrapper;
import org.jembi.jempi.bootstrapper.utils.BootstrapperLogger;
import org.jembi.jempi.libmpi.dgraph.LibDgraph;

import java.util.concurrent.Callable;


public class DgraphDataBootstrapper extends DataBootstrapper {
    protected static final Logger LOGGER = BootstrapperLogger.getChildLogger(DataBootstrapper.LOGGER, "DGraph");
    protected LibDgraph libDgraph;
    public DgraphDataBootstrapper(String configFilePath) {
        super(configFilePath);
        this.LoadDgraphLib();
    }

    public void LoadDgraphLib(){
        libDgraph = new LibDgraph(Level.INFO,
                                    this.loadedConfig.DGRAPH_ALPHA_HOSTS,
                                    this.loadedConfig.DGRAPH_ALPHA_PORTS);
    }
    @Override
    public Boolean createSchema() {
        LOGGER.info("Loading DGraph schema data.");
        libDgraph.startTransaction();
        libDgraph.createSchema();
        return true;
    }

    @Override
    public Boolean deleteData() {
        LOGGER.info("Deleting DGraph data and schemas.");
        libDgraph.startTransaction();
        libDgraph.dropAll();
        return true;
    }

    @Override
    public Boolean resetAll() {
        LOGGER.info("Resetting DGraph data and schemas.");
        return this.deleteData() && this.createSchema();
    }
}
