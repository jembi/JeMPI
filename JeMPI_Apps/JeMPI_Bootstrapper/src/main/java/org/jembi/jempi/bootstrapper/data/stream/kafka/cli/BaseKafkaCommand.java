package org.jembi.jempi.bootstrapper.data.stream.kafka.cli;

import org.jembi.jempi.bootstrapper.data.BaseDataBootstrapperCommand;
import org.jembi.jempi.bootstrapper.data.stream.kafka.KafkaDataBootstrapper;

import java.io.IOException;
import java.util.concurrent.Callable;

public abstract class BaseKafkaCommand extends BaseDataBootstrapperCommand<KafkaDataBootstrapper> implements Callable<Integer> {
    @Override
    protected KafkaDataBootstrapper getBootstrapper(final String configPath) throws IOException {
        return new KafkaDataBootstrapper(configPath);
    }
}
