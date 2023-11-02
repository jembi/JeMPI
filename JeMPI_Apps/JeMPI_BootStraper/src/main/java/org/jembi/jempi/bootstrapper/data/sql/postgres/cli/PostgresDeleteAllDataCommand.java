package org.jembi.jempi.bootstrapper.data.sql.postgres.cli;

import org.jembi.jempi.bootstrapper.data.cli.DeleteAllDataCommand;
import org.jembi.jempi.bootstrapper.data.stream.kafka.KafkaDataBootstrapper;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "deleteAllData")
public class PostgresDeleteAllDataCommand extends DeleteAllDataCommand implements Callable<Integer>{
    @Override
    public Integer call() throws Exception {
        return new KafkaDataBootstrapper(this.config).deleteData() ? 0 : 1;
    }
}