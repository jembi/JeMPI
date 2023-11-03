package org.jembi.jempi.bootstrapper.data.sql.postgres.cli;

import picocli.CommandLine;
import picocli.CommandLine.ScopeType;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "resetAll")
public class PostgresResetAllCommand implements Callable {

    @CommandLine.Option(names = "config", scope = ScopeType.INHERIT)
    private String config;

    @Override
    public Object call() throws Exception {
        return null;
    }
}