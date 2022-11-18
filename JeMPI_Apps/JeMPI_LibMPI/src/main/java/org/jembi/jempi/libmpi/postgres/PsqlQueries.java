package org.jembi.jempi.libmpi.postgres;
import java.util.List;
import org.jembi.jempi.shared.models.MatchForReview;
import java.util.ArrayList;
import java.sql.*;


public class PsqlQueries {
    private static final String QUERY = "select id,given_name,family_name, reason, match, state, mydate from patients";

    public static List<MatchForReview> getMatchesForReview() {

        List<MatchForReview> list = new ArrayList<MatchForReview>();
        try (Connection connection = dbConnect.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(QUERY);) {
            ResultSet rs = preparedStatement.executeQuery();

            while (rs.next()) {
                MatchForReview obj = new MatchForReview();
                obj.setId(rs.getInt("id"));
                obj.setGiven_name(rs.getString("given_name"));
                obj.setFamily_name(rs.getString("family_name"));
                obj.setReason(rs.getString("reason"));
                obj.setMatch(rs.getInt("match"));
                obj.setState(rs.getString("state"));
                obj.setDate(rs.getString("mydate"));
                list.add(obj);

            }
        } catch (SQLException e) {
        }
        return list;
    }
}
