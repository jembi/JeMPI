package org.jembi.jempi.monitor.lib.dal;

import java.sql.SQLException;

public interface IDAL {
    boolean deleteAllData() throws SQLException;
    boolean deleteTableData(String tableName);
}
