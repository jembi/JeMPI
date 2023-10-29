package org.jembi.jempi.monitor.lib.dal.dgraph;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.libmpi.postgresql.PostgresqlClient;
import org.jembi.jempi.monitor.RestHttpServer;
import org.jembi.jempi.monitor.lib.dal.IDAL;

public class LibDGraph implements IDAL {
    private static final Logger LOGGER = LogManager.getLogger(RestHttpServer.class);
    public LibDGraph(String URL, String USR, String PSW) {
        LOGGER.info("{}", "LibDGraph Constructor");
        PostgresqlClient.getInstance().config(URL, USR, PSW);
    }

    public boolean deleteAllData(){
        return false;
    }

    public boolean deleteTableData(String tableName){
        return  false;
    }
}
