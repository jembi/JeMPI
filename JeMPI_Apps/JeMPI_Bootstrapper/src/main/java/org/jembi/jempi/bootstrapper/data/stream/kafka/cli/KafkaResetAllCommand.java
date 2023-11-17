package org.jembi.jempi.bootstrapper.data.stream.kafka.cli;

import picocli.CommandLine;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "resetAll", mixinStandardHelpOptions = true, description = "Deletes all data and schemas associated with JeMPI kafka instance, then recreates schemas, and add initial data.")
public class KafkaResetAllCommand extends BaseKafkaCommand implements Callable<Integer> {
    @Override
    public Integer call() throws Exception {
        this.init();
        return this.execute(() -> this.bootstrapper.resetAll()  ? 0 : 1);
    }
}
