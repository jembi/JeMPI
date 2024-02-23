package org.jembi.jempi.bootstrapper.data;

public interface IDataBootstrapper {
   Boolean createSchema() throws Exception;

   Boolean deleteData() throws Exception;

   Boolean resetAll() throws Exception;
}
