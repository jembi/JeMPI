package org.jembi.jempi.bootstrapper;

import org.jembi.jempi.bootstrapper.data.cli.CLI;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(mixinStandardHelpOptions = true, subcommands = {CLI.class})

public class BootstrapperCLI implements Runnable {

   @CommandLine.Option(names = {"-c", "--config"}, description = "Config file")
   private String config;

   public static void main(final String... args) {
      int exitCode = new CommandLine(new BootstrapperCLI()).execute(args);
      System.exit(exitCode);
   }

   @Override
   public void run() {
   }
}
