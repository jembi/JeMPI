package org.jembi.jempi.bootstrapper.data.graph.dgraph.cli;

import org.jembi.jempi.bootstrapper.data.graph.dgraph.DgraphDataBootstrapper;
import picocli.CommandLine;

import java.util.concurrent.Callable;

//TODO: Add hep menu
@CommandLine.Command(name = "createAllSchemaData")
public class DgraphCreateAllSchemasCommand extends BaseDgraphCommand implements Callable<Integer> {
    @Override
    public Integer call() throws Exception {
        this.init();
        return this.Execute(() -> this.bootstrapper.createSchema()  ? 0 : 1);
    }
}