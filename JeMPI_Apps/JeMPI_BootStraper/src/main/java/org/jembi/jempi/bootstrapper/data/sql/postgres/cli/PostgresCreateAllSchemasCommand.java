package org.jembi.jempi.bootstrapper.data.sql.postgres.cli;

import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "createAllSchemaData")
public class PostgresCreateAllSchemasCommand<T> implements Callable {

    @CommandLine.Option(names = "config", scope = CommandLine.ScopeType.INHERIT)
    private String config;

    @Override
    public Object call() throws Exception {
        return null;
    }
}