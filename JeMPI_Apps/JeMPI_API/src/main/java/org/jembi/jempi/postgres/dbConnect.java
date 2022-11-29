package org.jembi.jempi.postgres;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class dbConnect {
    private static Connection conn;
    private final static String url = "jdbc:postgresql://192.168.0.195:5432/notifications";
    private final static String user = "postgres";
    private final static String password = "12345";

    public static Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return conn;
    }
}
