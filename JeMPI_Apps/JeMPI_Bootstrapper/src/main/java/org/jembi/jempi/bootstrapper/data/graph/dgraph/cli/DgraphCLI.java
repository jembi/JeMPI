package org.jembi.jempi.bootstrapper.data.graph.dgraph.cli;

import picocli.CommandLine.Command;

@Command(name = "dgraph", mixinStandardHelpOptions = true, subcommands = {DgraphResetAllCommand.class,
                                                                          DgraphDeleteAllCommand.class,
                                                                          DgraphCreateAllSchemaDataCommand.class})
public class DgraphCLI {
}
