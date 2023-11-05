package org.jembi.jempi.bootstrapper.data.stream.kafka.cli;

import picocli.CommandLine;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "deleteAllData")
public class KafkaDeleteAllDataCommand extends BaseKafkaCommand implements Callable<Integer>{

    @Override
    public Integer call() throws Exception {
        this.init();
        return this.Execute(() -> this.bootstrapper.deleteData()  ? 0 : 1);
    }
}