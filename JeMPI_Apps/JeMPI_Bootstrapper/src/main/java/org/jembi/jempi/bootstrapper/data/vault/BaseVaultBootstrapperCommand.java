package org.jembi.jempi.bootstrapper.data.vault;

import org.apache.logging.log4j.Logger;
import org.jembi.jempi.bootstrapper.utils.BootstrapperLogger;
import picocli.CommandLine;

import java.util.concurrent.Callable;

public abstract class BaseVaultBootstrapperCommand<T extends VaultBootstrapper> implements Callable<Integer> {
   protected static final Logger LOGGER = BootstrapperLogger.getLogger("VaultBootstrapperCLI");

   @CommandLine.Option(names = "config", scope = CommandLine.ScopeType.INHERIT)
   protected String config;

   protected T bootstrapper;

   public BaseVaultBootstrapperCommand<T> init() throws Exception {
      bootstrapper = getBootstrapper(config);
      return this;
   }

   protected Integer execute(final Callable<Integer> bootstrapperFunc) {
      try {
         Integer bootstrapperResult = bootstrapperFunc.call();
         if (bootstrapperResult != 0) {
            LOGGER.warn("Command completed successfully with some errors");
            return CommandLine.ExitCode.SOFTWARE;
         }
         return CommandLine.ExitCode.OK;
      } catch (Exception e) {
         LOGGER.error("An error occurred whilst executing the command.", e);
         return CommandLine.ExitCode.SOFTWARE;
      }
   }

   protected abstract T getBootstrapper(String configPath) throws Exception;

   public BaseVaultBootstrapperCommand<T> setConfigPath(final String config) {
      this.config = config;
      return this;
   }
}
