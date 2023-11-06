package org.jembi.jempi.bootstrapper.data.graph.dgraph.cli;

import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "deleteAll", mixinStandardHelpOptions = true, description = "Delete all the data and schema used by JeMPI Dgraph instance.")
public class DgraphDeleteAllCommand extends BaseDgraphCommand implements Callable<Integer>{
    @Override
    public Integer call() throws Exception {
        this.init();
        return this.execute(() -> this.bootstrapper.deleteData()  ? 0 : 1);
    }
}