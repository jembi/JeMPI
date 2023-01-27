package org.jembi.jempi.api.session;


import com.softwaremill.session.MultiValueSessionSerializer;
import com.softwaremill.session.converters.MapConverters;
import org.jembi.jempi.api.models.User;
import scala.Function1;
import scala.collection.immutable.Map;
import scala.compat.java8.JFunction0;
import scala.compat.java8.JFunction1;
import scala.util.Try;

import java.util.HashMap;

public class UserSessionSerializer extends MultiValueSessionSerializer<UserSession> {

    public UserSessionSerializer(
            Function1<UserSession, scala.collection.immutable.Map<String, String>> toMap,
            Function1<scala.collection.immutable.Map<String, String>, Try<UserSession>> fromMap
    ) {
        super(toMap, fromMap);
    }

}