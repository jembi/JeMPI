package org.jembi.jempi.linker.linker_processor.utls;

import org.jembi.jempi.AppConfig;
import org.jembi.jempi.libmpi.LibMPI;

import java.util.UUID;

public class MockLibMPI {

    public static LibMPI getLibMPI(){
        // TODO: Load env variable  correctly. currently using intellj library
        final var host = AppConfig.getDGraphHosts();
        final var port = AppConfig.getDGraphPorts();
        return new LibMPI(AppConfig.GET_LOG_LEVEL,
                host,
                port,
                AppConfig.KAFKA_BOOTSTRAP_SERVERS,
                "CLIENT_ID_LINKER-" + UUID.randomUUID());
    }

}
