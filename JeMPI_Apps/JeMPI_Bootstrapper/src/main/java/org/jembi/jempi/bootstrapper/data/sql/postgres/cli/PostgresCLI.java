package org.jembi.jempi.bootstrapper.data.sql.postgres.cli;

import picocli.CommandLine.Command;

@Command(name = "postgres", mixinStandardHelpOptions = true, subcommands = {PostgresResetAllCommand.class,
                                                                            PostgresDeleteDataOnlyCommand.class,
                                                                            PostgresDeleteAllCommand.class,
                                                                            PostgresCreateAllSchemaDataCommand.class})

public class PostgresCLI {
}
