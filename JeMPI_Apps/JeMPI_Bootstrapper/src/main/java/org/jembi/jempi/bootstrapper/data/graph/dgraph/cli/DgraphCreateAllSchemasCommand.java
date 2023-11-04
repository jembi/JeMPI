package org.jembi.jempi.bootstrapper.data.graph.dgraph.cli;

import org.jembi.jempi.bootstrapper.data.graph.dgraph.DgraphDataBootstrapper;
import picocli.CommandLine;

import java.util.concurrent.Callable;

//TODO: Add hep menu
@CommandLine.Command(name = "createAllSchemaData")
public class DgraphCreateAllSchemasCommand<T> implements Callable {
    @CommandLine.Option(names = "config", scope = CommandLine.ScopeType.INHERIT)
    private String config;

    @Override
    public Object call() throws Exception {
        //TODO: Share
        return new DgraphDataBootstrapper(this.config).createSchema();
    }
}