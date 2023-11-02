package org.jembi.jempi.bootstrapper.data;

public interface IDataBootstrapper {
    Boolean createSchema();
    Boolean deleteData();
    Boolean resetAll();
}
