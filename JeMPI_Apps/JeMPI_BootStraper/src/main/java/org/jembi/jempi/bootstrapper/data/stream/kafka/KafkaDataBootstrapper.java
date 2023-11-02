package org.jembi.jempi.bootstrapper.data.stream.kafka;
import org.jembi.jempi.bootstrapper.data.DataBootstrapper;
public class KafkaDataBootstrapper extends DataBootstrapper {

    public KafkaDataBootstrapper(String configFilePath) {
        super(configFilePath);
    }

    @Override
    public Boolean createSchema() {
        return null;
    }

    @Override
    public Boolean deleteData() {
        return null;
    }

    @Override
    public Boolean resetAll() {
        return null;
    }
}
