package org.jembi.jempi.monitor.lib.dal;

import java.sql.SQLException;

public interface IDAL {
    boolean deleteAllData() throws Exception;
    boolean deleteTableData(String tableName);
}
