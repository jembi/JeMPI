package org.jembi.jempi.postgres;
import java.util.List;
import java.util.ArrayList;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.HashMap;
import java.lang.String;
import java.util.UUID;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.lang.String;


public class PsqlQueries {

    private static final String QUERY = " select N.\"PatientId\", N.\"Id\", N.\"Names\", N.\"Created\", N.\"Reason\",  NS.\"State\", NT.\"Type\", M.\"Score\", M.\"GoldenId\" from \"Notification\" N " +
            "JOIN \"NotificationState\" NS " +
            " ON NS.\"Id\" = N.\"State\" JOIN \"NotificationType\" NT on N.\"Type\" = NT.\"Id\" " +
            "JOIN \"Match\" M " +
                    "ON M.\"NotificationId\" = N.\"Id\" ";


    private static final Logger LOGGER = LogManager.getLogger(PsqlQueries.class);

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
                    row.put(md.getColumnName(i), (rs.getObject(i)));
                }
                list.add(row);
            }
        } catch (Exception e) {
            LOGGER.error(e);
        }
        return list;
    }


    public static void insert(UUID id, UUID type, String patientNames, Float score, Long created, String gID ) throws SQLException {

        Connection conn = dbConnect.connect();
        Statement stmt = conn.createStatement();

        // Set auto-commit to false
        conn.setAutoCommit(false);
        UUID stateId = null;
        Date res = new Date(created);

        ResultSet rs = stmt.executeQuery( "select * from notificationstate");
        while(rs.next()){
            if(rs.getString("name").equals("New"))
                 stateId = UUID.fromString(rs.getString("id"));
        }
        String sql = "INSERT INTO notification (\"Id\", \"Type\", \"State\", \"Name\", \"Created\")" + "VALUES ('"+id+"','"+type+"','"+stateId+"','"+patientNames+"', '"+res+"')";
        stmt.addBatch(sql);

        sql = "INSERT INTO match (notificationid, score, goldenid)" + "VALUES ('"+id+"','"+score+"', '"+gID+"')";
        stmt.addBatch(sql);

        int[] count = stmt.executeBatch();
        conn.commit();


    }
}
