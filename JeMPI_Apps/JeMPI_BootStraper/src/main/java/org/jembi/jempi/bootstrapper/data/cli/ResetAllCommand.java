package org.jembi.jempi.bootstrapper.data.cli;

import picocli.CommandLine;
import picocli.CommandLine.ScopeType;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "resetAll", description = "Deletes all data from postgres, dgraph and kafka, and then creates schemes, and loads initial data")
public class ResetAllCommand implements Callable {

    @CommandLine.Option(names = "config", scope = ScopeType.INHERIT)
    private String config;

    @Override
    public Object call() throws Exception {
        return null;
    }
}