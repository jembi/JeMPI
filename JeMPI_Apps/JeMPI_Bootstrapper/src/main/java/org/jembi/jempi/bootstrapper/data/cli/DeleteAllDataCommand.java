package org.jembi.jempi.bootstrapper.data.cli;

import org.jembi.jempi.bootstrapper.data.stream.kafka.cli.KafkaDeleteAllDataCommand;
import picocli.CommandLine;
import picocli.CommandLine.ScopeType;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "deleteAllData")
public class DeleteAllDataCommand implements Callable<Integer>{

    @CommandLine.Option(names = "config", scope = ScopeType.INHERIT)
    protected String config;

    protected DeleteAllDataCommand SetConfig(String config){
        this.config = config;
        return this;
    }
    @Override
    public Integer call() throws Exception {
        return new KafkaDeleteAllDataCommand().SetConfig(config).call();

    }
}