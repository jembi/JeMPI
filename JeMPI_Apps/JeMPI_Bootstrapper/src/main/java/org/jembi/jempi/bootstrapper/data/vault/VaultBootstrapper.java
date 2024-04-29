package org.jembi.jempi.bootstrapper.data.vault;

import org.apache.logging.log4j.Logger;
import org.jembi.jempi.bootstrapper.Bootstrapper;
import org.jembi.jempi.bootstrapper.utils.BootstrapperLogger;

public abstract class VaultBootstrapper extends Bootstrapper implements IVaultBootstrapper {
    protected static final Logger LOGGER = BootstrapperLogger.getChildLogger(Bootstrapper.LOGGER, "Vault");

    public VaultBootstrapper(final String configFilePath) {
        super(configFilePath);
    }
}
