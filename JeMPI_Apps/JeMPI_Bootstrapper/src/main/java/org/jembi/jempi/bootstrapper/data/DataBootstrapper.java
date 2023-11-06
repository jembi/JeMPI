package org.jembi.jempi.bootstrapper.data;

import org.apache.logging.log4j.Logger;
import org.jembi.jempi.bootstrapper.Bootstrapper;
import org.jembi.jempi.bootstrapper.utils.BootstrapperLogger;

public abstract class DataBootstrapper extends Bootstrapper implements IDataBootstrapper {
    protected static final Logger LOGGER = BootstrapperLogger.getChildLogger(Bootstrapper.LOGGER, "Data");
    public DataBootstrapper(final String configFilePath) {
        super(configFilePath);
    }
}
