package org.jembi.jempi.bootstrapper.data.cli;

import org.jembi.jempi.bootstrapper.data.graph.dgraph.cli.DgraphCLI;
import org.jembi.jempi.bootstrapper.data.sql.postgres.cli.PostgresCLI;
import org.jembi.jempi.bootstrapper.data.stream.kafka.cli.KafkaCLI;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(
        name = "data",
        mixinStandardHelpOptions = true,
        subcommands = {
        KafkaCLI.class,
        DgraphCLI.class,
        PostgresCLI.class,
        ResetAllCommand.class,
        DeleteAllSchemaDataCommand.class,
        CreateAllSchemaDataCommand.class
})
public class CLI {
    @CommandLine.Option(names = {"-c", "--config"}, description = "Config file")
    private String config;
}
