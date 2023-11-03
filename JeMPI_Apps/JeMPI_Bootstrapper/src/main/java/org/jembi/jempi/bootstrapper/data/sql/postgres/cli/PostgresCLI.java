package org.jembi.jempi.bootstrapper.data.sql.postgres.cli;

import picocli.CommandLine.Command;

@Command(
        name = "postgres",
        mixinStandardHelpOptions = true,
        subcommands = {
                PostgresResetAllCommand.class,
                PostgresDeleteAllDataCommand.class,
                PostgresCreateAllSchemasCommand.class
        })

public class PostgresCLI { }
