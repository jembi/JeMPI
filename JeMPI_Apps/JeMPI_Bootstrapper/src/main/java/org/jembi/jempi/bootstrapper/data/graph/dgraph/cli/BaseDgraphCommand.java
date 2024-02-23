package org.jembi.jempi.bootstrapper.data.graph.dgraph.cli;

import org.jembi.jempi.bootstrapper.data.BaseDataBootstrapperCommand;
import org.jembi.jempi.bootstrapper.data.graph.dgraph.DgraphDataBootstrapper;

import java.util.concurrent.Callable;

public abstract class BaseDgraphCommand extends BaseDataBootstrapperCommand<DgraphDataBootstrapper> implements Callable<Integer> {
   @Override
   protected DgraphDataBootstrapper getBootstrapper(final String configPath) {
      return new DgraphDataBootstrapper(configPath);
   }
}
