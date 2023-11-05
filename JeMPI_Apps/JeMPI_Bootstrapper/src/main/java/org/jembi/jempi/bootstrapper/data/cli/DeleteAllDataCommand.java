package org.jembi.jempi.bootstrapper.data.cli;

import org.jembi.jempi.bootstrapper.data.BaseDataBootstrapperCommand;
import org.jembi.jempi.bootstrapper.data.DataBootstrapper;
import org.jembi.jempi.bootstrapper.data.graph.dgraph.cli.DgraphDeleteAllDataCommand;
import org.jembi.jempi.bootstrapper.data.sql.postgres.cli.PostgresDeleteAllDataAndSchemaCommand;
import org.jembi.jempi.bootstrapper.data.stream.kafka.cli.KafkaDeleteAllDataCommand;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "deleteAllData")
public class DeleteAllDataCommand<T extends DataBootstrapper> extends BaseDataBootstrapperCommand<T> implements Callable<Integer>{
    @Override
    public Integer call() throws Exception {
        return this.Execute(() -> new PostgresDeleteAllDataAndSchemaCommand().setConfigPath(this.config).init().call() +
                                        new DgraphDeleteAllDataCommand().setConfigPath(this.config).init().call() +
                                        new KafkaDeleteAllDataCommand().setConfigPath(this.config).init().call());
    }

    @Override
    public DeleteAllDataCommand<T> init() throws Exception {
        super.init();
        return this;
    }

    @Override
    protected T getBootstrapper(String configPath) {
        return null;
    }
}