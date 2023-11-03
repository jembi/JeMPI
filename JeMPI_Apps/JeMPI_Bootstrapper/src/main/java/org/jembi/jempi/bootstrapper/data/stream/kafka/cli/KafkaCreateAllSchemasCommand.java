package org.jembi.jempi.bootstrapper.data.stream.kafka.cli;

import org.jembi.jempi.bootstrapper.data.stream.kafka.KafkaDataBootstrapper;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "createAllSchemaData")
public class KafkaCreateAllSchemasCommand<T> implements Callable<Integer> {

    @CommandLine.Option(names = "config", scope = CommandLine.ScopeType.INHERIT)
    private String config;
    @Override
    public Integer call() throws Exception {
        return new KafkaDataBootstrapper(this.config).createSchema() ? 0 : 1;
    }
}