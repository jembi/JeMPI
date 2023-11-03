package org.jembi.jempi.bootstrapper;

import org.apache.logging.log4j.Logger;
import org.jembi.jempi.bootstrapper.utils.BootstrapperLogger;

public class Bootstrapper {
    protected static final Logger LOGGER = BootstrapperLogger.getLogger("Jempi Bootstrapper");
    protected BootstrapperConfig loadedConfig;
    public Bootstrapper(String configFilePath){
        this.loadedConfig = BootstrapperConfig.create(configFilePath, LOGGER);
    }
}

