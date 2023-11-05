package org.jembi.jempi.bootstrapper.data.stream.kafka.cli;

import org.jembi.jempi.bootstrapper.data.stream.kafka.KafkaDataBootstrapper;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "createAllSchemaData")
public class KafkaCreateAllSchemasCommand extends BaseKafkaCommand implements Callable<Integer> {
    @Override
    public Integer call() throws Exception {
        this.init();
        return this.Execute(() -> this.bootstrapper.createSchema()  ? 0 : 1 );
    }
}