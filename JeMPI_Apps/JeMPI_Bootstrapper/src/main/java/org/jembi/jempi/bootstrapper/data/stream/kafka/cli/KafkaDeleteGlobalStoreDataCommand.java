package org.jembi.jempi.bootstrapper.data.stream.kafka.cli;

import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "deleteGlobalStoreData", mixinStandardHelpOptions = true, description = "Delete all global store topics used by JeMPI.")
public class KafkaDeleteGlobalStoreDataCommand extends BaseKafkaCommand implements Callable<Integer> {
    @Override
    public Integer call() throws Exception {
        this.init();
        return this.execute(() -> this.bootstrapper.deleteGlobalStoreTopicsData()  ? 0 : 1);
    }
}
