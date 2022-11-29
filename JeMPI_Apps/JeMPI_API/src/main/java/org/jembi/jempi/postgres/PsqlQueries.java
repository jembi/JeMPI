package org.jembi.jempi.postgres;
import java.util.List;
import org.jembi.jempi.shared.models.MatchForReview;
import java.util.ArrayList;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.lang.String;


public class PsqlQueries {
//    private static final String QUERY = "select id,given_name,family_name, reason, match, state, mydate from patients";
    private static final String QUERY = "SELECT N.\"Id\", N.\"PatientID\", N.\"Reason\", N.\"Name\", N.\"Created\", N.\"State\", MA.\"Score\" FROM\n" +
        "\"Notification\" N INNER JOIN \"Match\" MA ON N.\"Id\" =  MA.\"NotificationId\"";
    private final String Insert_Query = "INSERT INTO public."User"(
            "Id", "Name")
    VALUES ("2c11b5a3-a580-4c13-8f2b-8ac1bc9fa6e5", "Another Test")";
    public static List getMatchesForReview() {
        ArrayList list = new ArrayList();
        try (Connection connection = dbConnect.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(QUERY);) {
             ResultSet rs = preparedStatement.executeQuery();
             ResultSetMetaData md = rs.getMetaData();
             int columns = md.getColumnCount();

            while (rs.next()) {
                HashMap row = new HashMap(columns);
                for (int i = 1; i <= columns; i++) {
                     row.put(md.getColumnName(i), (rs.getObject(i)).toString());

                }
                list.add(row);
            }
        } catch (SQLException e) {
            list.add(e.toString());
        }
        return list;
    }


    public static Insert(){
        try (Connection connection = dbConnect.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(QUERY)){
            int insertedRows = preparedStatement.executeUpdate();

        }
    }
}
