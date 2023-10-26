package org.jembi.jempi.monitor.lib.dal.postgres;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.libmpi.postgresql.PostgresqlClient;
import org.jembi.jempi.monitor.RestHttpServer;

public class LibPostgres  {
    private static final Logger LOGGER = LogManager.getLogger(RestHttpServer.class);
    public LibPostgres(String URL, String USR, String PSW) {
        LOGGER.info("{}", "LibPostgresql Constructor");
        PostgresqlClient.getInstance().config(URL, USR, PSW);
    }
}
