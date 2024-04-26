package org.jembi.jempi.bootstrapper.data.vault.cli;

import picocli.CommandLine;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "resetVault", mixinStandardHelpOptions = true, description = "Deletes vault secrets and sets new ones.")
public class VaultResetAllCommand extends BaseVaultCommand implements Callable<Integer> {
    @Override
    public Integer call() throws Exception {
        this.init();
        return this.execute(() -> this.bootstrapper.resetVault()
                ? 0
                : 1);
    }
}
