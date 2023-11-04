package org.jembi.jempi.bootstrapper.data.graph.dgraph.cli;

import org.jembi.jempi.bootstrapper.data.cli.DeleteAllDataCommand;
import org.jembi.jempi.bootstrapper.data.graph.dgraph.DgraphDataBootstrapper;
import org.jembi.jempi.bootstrapper.data.stream.kafka.KafkaDataBootstrapper;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "deleteAllData")
public class DgraphDeleteAllDataCommand extends DeleteAllDataCommand implements Callable<Integer>{
    @Override
    public Integer call() throws Exception {
        // TODO: Share
        return new DgraphDataBootstrapper(this.config).deleteData() ? 0 : 1;
    }
}