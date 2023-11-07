package org.jembi.jempi.bootstrapper.data;

import java.util.concurrent.ExecutionException;

public interface IDataBootstrapper {
    Boolean createSchema() throws Exception;
    Boolean deleteData() throws Exception;
    Boolean resetAll() throws Exception;
}
