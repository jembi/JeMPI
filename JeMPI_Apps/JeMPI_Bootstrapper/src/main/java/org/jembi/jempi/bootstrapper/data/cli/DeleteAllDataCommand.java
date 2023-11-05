package org.jembi.jempi.bootstrapper.data.cli;

import org.jembi.jempi.bootstrapper.data.BaseDataBootstrapperCommand;
import org.jembi.jempi.bootstrapper.data.graph.dgraph.cli.DgraphDeleteAllDataCommand;
import org.jembi.jempi.bootstrapper.data.sql.postgres.cli.PostgresDeleteAllDataAndSchemaCommand;
import org.jembi.jempi.bootstrapper.data.stream.kafka.cli.KafkaDeleteAllDataCommand;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "deleteAllData")
public class DeleteAllDataCommand extends BaseCLICommand implements Callable<Integer>{
    @Override
    public Integer call() throws Exception {

        return this.Execute(() -> this.CallMultiple(new BaseDataBootstrapperCommand[]
                {
                        new PostgresDeleteAllDataAndSchemaCommand(),
                        new DgraphDeleteAllDataCommand(),
                        new KafkaDeleteAllDataCommand()
                }));
    }
}