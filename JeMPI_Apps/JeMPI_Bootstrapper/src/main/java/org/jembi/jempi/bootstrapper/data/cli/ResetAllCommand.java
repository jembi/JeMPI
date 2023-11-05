package org.jembi.jempi.bootstrapper.data.cli;

import org.jembi.jempi.bootstrapper.data.BaseDataBootstrapperCommand;
import org.jembi.jempi.bootstrapper.data.graph.dgraph.cli.DgraphDeleteAllDataCommand;
import org.jembi.jempi.bootstrapper.data.graph.dgraph.cli.DgraphResetAllCommand;
import org.jembi.jempi.bootstrapper.data.sql.postgres.cli.PostgresDeleteAllDataAndSchemaCommand;
import org.jembi.jempi.bootstrapper.data.sql.postgres.cli.PostgresResetAllCommand;
import org.jembi.jempi.bootstrapper.data.stream.kafka.cli.KafkaDeleteAllDataCommand;
import org.jembi.jempi.bootstrapper.data.stream.kafka.cli.KafkaResetAllCommand;
import picocli.CommandLine;
import picocli.CommandLine.ScopeType;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "resetAll", description = "Deletes all data from postgres, dgraph and kafka, and then creates schemes, and loads initial data")
public class ResetAllCommand extends BaseCLICommand implements Callable<Integer>{
    @Override
    public Integer call() throws Exception {

        return this.Execute(() -> this.CallMultiple(new BaseDataBootstrapperCommand[]
                {
                        new PostgresResetAllCommand(),
                        new DgraphResetAllCommand(),
                        new KafkaResetAllCommand()
                }));
    }
}