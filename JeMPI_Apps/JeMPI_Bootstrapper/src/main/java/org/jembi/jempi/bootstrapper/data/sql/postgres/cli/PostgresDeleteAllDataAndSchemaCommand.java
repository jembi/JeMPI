package org.jembi.jempi.bootstrapper.data.sql.postgres.cli;

import org.jembi.jempi.bootstrapper.data.sql.postgres.PostgresDataBootstrapper;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "deleteAllDataAndSchemas")
public class PostgresDeleteAllDataAndSchemaCommand implements Callable {
    @CommandLine.Option(names = "config", scope = CommandLine.ScopeType.INHERIT)
    private String config;

    @Override
    public Object call() throws Exception {
        //TODO: Share
        PostgresDataBootstrapper pgDataBootstrapper = new PostgresDataBootstrapper(this.config);
        return pgDataBootstrapper.deleteData() && pgDataBootstrapper.deleteTables();
    }
}
