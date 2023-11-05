package org.jembi.jempi.bootstrapper.data.cli;

import org.jembi.jempi.bootstrapper.data.BaseDataBootstrapperCommand;
import org.jembi.jempi.bootstrapper.data.graph.dgraph.cli.DgraphCreateAllSchemasCommand;
import org.jembi.jempi.bootstrapper.data.graph.dgraph.cli.DgraphDeleteAllDataCommand;
import org.jembi.jempi.bootstrapper.data.sql.postgres.cli.PostgresCreateAllSchemasCommand;
import org.jembi.jempi.bootstrapper.data.sql.postgres.cli.PostgresDeleteAllDataAndSchemaCommand;
import org.jembi.jempi.bootstrapper.data.stream.kafka.cli.KafkaCreateAllSchemasCommand;
import org.jembi.jempi.bootstrapper.data.stream.kafka.cli.KafkaDeleteAllDataCommand;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "createAllSchemaData")
public class CreateAllSchemasCommand extends BaseCLICommand implements Callable<Integer>{
    @Override
    public Integer call() throws Exception {

        return this.Execute(() -> this.CallMultiple(new BaseDataBootstrapperCommand[]
                {
                        new PostgresCreateAllSchemasCommand(),
                        new DgraphCreateAllSchemasCommand(),
                        new KafkaCreateAllSchemasCommand()
                }));
    }
}