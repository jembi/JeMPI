package org.jembi.jempi.bootstrapper.data.cli;

import org.jembi.jempi.bootstrapper.data.vault.BaseVaultBootstrapperCommand;
import org.jembi.jempi.bootstrapper.data.vault.cli.BaseVaultCLICommand;
import org.jembi.jempi.bootstrapper.data.vault.cli.VaultResetAllCommand;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "resetVault", mixinStandardHelpOptions = true, description = "Deletes all secrets used by JeMPI and"
                                                                                         + " sets new ones")
public class ResetVaultCommand extends BaseVaultCLICommand implements Callable<Integer> {
   @Override
   public Integer call() throws Exception {

      return this.execute(() -> this.callMultiple(
            new BaseVaultBootstrapperCommand[]{new VaultResetAllCommand()}));
   }
}
