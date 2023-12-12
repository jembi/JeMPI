package org.jembi.jempi.bootstrapper.data.sql.postgres.cli;

import org.jembi.jempi.bootstrapper.data.BaseDataBootstrapperCommand;
import org.jembi.jempi.bootstrapper.data.sql.postgres.PostgresDataBootstrapper;

import java.util.concurrent.Callable;

public abstract class BasePostgresCommand extends BaseDataBootstrapperCommand<PostgresDataBootstrapper> implements Callable<Integer> {
   @Override
   protected PostgresDataBootstrapper getBootstrapper(String configPath) {
      return new PostgresDataBootstrapper(configPath);
   }
}

