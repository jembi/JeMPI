package org.jembi.jempi.bootstrapper.data.vault.cli;

import org.jembi.jempi.bootstrapper.data.vault.BaseVaultBootstrapperCommand;
import org.jembi.jempi.bootstrapper.data.vault.VaultBootstrapper;

import java.util.concurrent.Callable;

public abstract class BaseVaultCLICommand extends BaseVaultBootstrapperCommand<VaultBootstrapper>
      implements Callable<Integer> {
   @Override
   public BaseVaultCLICommand init() throws Exception {
      super.init();
      return this;
   }

   @Override
   protected VaultBootstrapper getBootstrapper(final String configPath) {
      return null;
   }

   protected Integer callMultiple(final BaseVaultBootstrapperCommand<VaultBootstrapper>[] bootstrapperCommands)
         throws Exception {
      Integer execResult = 0;
      for (BaseVaultBootstrapperCommand<VaultBootstrapper> b : bootstrapperCommands) {
         execResult += b.setConfigPath(this.config).init().call();
      }
      return execResult;
   }
}
