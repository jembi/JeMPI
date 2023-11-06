package org.jembi.jempi.bootstrapper.data.cli;

import org.jembi.jempi.bootstrapper.data.BaseDataBootstrapperCommand;
import org.jembi.jempi.bootstrapper.data.graph.dgraph.cli.DgraphDeleteAllCommand;
import org.jembi.jempi.bootstrapper.data.sql.postgres.cli.PostgresDeleteAllCommand;
import org.jembi.jempi.bootstrapper.data.stream.kafka.cli.KafkaDeleteAllCommand;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "deleteAllSchemaData", mixinStandardHelpOptions = true, description = "Delete all the data and schema used by JeMPI.")
public class DeleteAllSchemaDataCommand extends BaseCLICommand implements Callable<Integer> {
    @Override
    public Integer call() throws Exception {

        return this.execute(() -> this.callMultiple(new BaseDataBootstrapperCommand[]
                {
                        new PostgresDeleteAllCommand(),
                        new DgraphDeleteAllCommand(),
                        new KafkaDeleteAllCommand()
                }));
    }
}
