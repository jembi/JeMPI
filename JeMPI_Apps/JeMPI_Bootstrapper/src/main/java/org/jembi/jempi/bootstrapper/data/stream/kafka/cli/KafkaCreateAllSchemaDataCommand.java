package org.jembi.jempi.bootstrapper.data.stream.kafka.cli;

import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "createAllSchemaData", mixinStandardHelpOptions = true, description = "Create all the required schema's and data for JeMPI Kafka instance.")
public class KafkaCreateAllSchemaDataCommand extends BaseKafkaCommand implements Callable<Integer> {
    @Override
    public Integer call() throws Exception {
        this.init();
        return this.Execute(() -> this.bootstrapper.createSchema()  ? 0 : 1 );
    }
}