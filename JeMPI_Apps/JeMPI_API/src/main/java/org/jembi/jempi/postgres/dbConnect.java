package org.jembi.jempi.postgres;

import org.jembi.jempi.AppConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class dbConnect {
    private static Connection conn;
    private final static String url = "jdbc:postgresql://"+ AppConfig.POSTGRES_SERVER +"/notifications";
    private final static String user = "postgres";

    public static Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url, user, null);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return conn;
    }
}
