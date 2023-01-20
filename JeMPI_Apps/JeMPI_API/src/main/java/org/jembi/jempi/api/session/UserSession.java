package org.jembi.jempi.api.session;

import com.softwaremill.session.converters.MapConverters;
import org.jembi.jempi.api.models.User;
import scala.collection.immutable.Map;
import scala.compat.java8.JFunction0;
import scala.compat.java8.JFunction1;
import scala.util.Try;

import java.util.HashMap;

public class UserSession extends User {

    /**
     * This session serializer converts a session type into a value (always a String type). The first two arguments are just conversion functions.
     * The third argument is a serializer responsible for preparing the data to be sent/received over the wire. There are some ready-to-use serializers available
     * in the com.softwaremill.session.SessionSerializer companion object, like stringToString and mapToString, just to name a few.
     */
    private static final UserSessionSerializer serializer = new UserSessionSerializer(
            (JFunction1<UserSession, Map<String, String>>) user -> {
                final java.util.Map<String, String> m = new HashMap<>();
                m.put("id", user.getId());
                m.put("email", user.getEmail());
                m.put("username", user.getUsername());
                m.put("givenName", user.getGivenName());
                m.put("familyName", user.getFamilyName());
                return MapConverters.toImmutableMap(m);
            },
            (JFunction1<Map<String, String>, Try<UserSession>>) value ->
                    Try.apply((JFunction0<UserSession>) () -> new UserSession(new User(
                            value.get("id").get(),
                            value.get("username").get(),
                            value.get("email").get(),
                            value.get("familyName").get(),
                            value.get("givenName").get()
                    )))
    );

     public UserSession(User user) {
         super(user.getId(),user.getUsername(), user.getEmail(), user.getFamilyName(), user.getGivenName());
     }

    public static UserSessionSerializer getSerializer() {
        return serializer;
    }

}
