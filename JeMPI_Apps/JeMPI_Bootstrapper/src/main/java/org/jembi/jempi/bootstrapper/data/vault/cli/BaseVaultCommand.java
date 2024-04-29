package org.jembi.jempi.bootstrapper.data.vault.cli;

import org.jembi.jempi.bootstrapper.data.vault.BaseVaultBootstrapperCommand;
import org.jembi.jempi.bootstrapper.data.vault.VaultDataBootstrapper;

import java.util.concurrent.Callable;

public abstract class BaseVaultCommand extends BaseVaultBootstrapperCommand<VaultDataBootstrapper>
      implements Callable<Integer> {
   @Override
   protected VaultDataBootstrapper getBootstrapper(final String configPath) {
      return new VaultDataBootstrapper(configPath);
   }
}
