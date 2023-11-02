package org.jembi.jempi.bootstrapper.data;

import org.jembi.jempi.bootstrapper.Bootstrapper;

public abstract class DataBootstrapper extends Bootstrapper implements IDataBootstrapper {
    public DataBootstrapper(String configFilePath) {
        super(configFilePath);
    }
}
