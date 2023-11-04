package org.jembi.jempi.bootstrapper.data.sql.postgres.cli;

import org.jembi.jempi.bootstrapper.data.graph.dgraph.DgraphDataBootstrapper;
import org.jembi.jempi.bootstrapper.data.sql.postgres.PostgresDataBootstrapper;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "createAllSchemaData")
public class PostgresCreateAllSchemasCommand<T> implements Callable {

    @CommandLine.Option(names = "config", scope = CommandLine.ScopeType.INHERIT)
    private String config;

    @Override
    public Object call() throws Exception {
        //TODO: Share
        return new PostgresDataBootstrapper(this.config).createSchema();
    }
}