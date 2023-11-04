package org.jembi.jempi.bootstrapper.data.graph.dgraph.cli;

import org.jembi.jempi.bootstrapper.data.graph.dgraph.DgraphDataBootstrapper;
import picocli.CommandLine;
import picocli.CommandLine.ScopeType;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "resetAll")
public class DgraphResetAllCommand implements Callable {

    @CommandLine.Option(names = "config", scope = ScopeType.INHERIT)
    private String config;

    @Override
    public Object call() throws Exception {
        // TODO: Share
        return new DgraphDataBootstrapper(this.config).resetAll() ? 0 : 1;
    }
}