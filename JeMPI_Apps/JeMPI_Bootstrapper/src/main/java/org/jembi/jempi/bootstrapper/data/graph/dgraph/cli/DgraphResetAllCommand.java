package org.jembi.jempi.bootstrapper.data.graph.dgraph.cli;

import org.jembi.jempi.bootstrapper.data.graph.dgraph.DgraphDataBootstrapper;
import picocli.CommandLine;
import picocli.CommandLine.ScopeType;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "resetAll")
public class DgraphResetAllCommand extends BaseDgraphCommand implements Callable<Integer> {
    @Override
    public Integer call() throws Exception {
        this.init();
        return this.Execute(() -> this.bootstrapper.resetAll()  ? 0 : 1 );
    }
}