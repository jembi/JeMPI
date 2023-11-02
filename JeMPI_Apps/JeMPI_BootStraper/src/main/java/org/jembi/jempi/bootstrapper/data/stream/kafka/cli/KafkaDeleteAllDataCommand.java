package org.jembi.jempi.bootstrapper.data.stream.kafka.cli;

import org.jembi.jempi.bootstrapper.data.cli.DeleteAllDataCommand;
import org.jembi.jempi.bootstrapper.data.stream.kafka.KafkaDataBootstrapper;
import picocli.CommandLine;
import picocli.CommandLine.ScopeType;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "deleteAllData")
public class KafkaDeleteAllDataCommand extends DeleteAllDataCommand implements Callable<Integer>{
    @Override
    public Integer call() throws Exception {
        return new KafkaDataBootstrapper(this.config).deleteData() ? 0 : 1;
    }
}