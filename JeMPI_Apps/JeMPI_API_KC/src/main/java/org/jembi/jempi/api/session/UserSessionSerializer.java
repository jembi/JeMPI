package org.jembi.jempi.api.session;


import com.softwaremill.session.MultiValueSessionSerializer;
import scala.Function1;
import scala.util.Try;

public class UserSessionSerializer extends MultiValueSessionSerializer<UserSession> {

   public UserSessionSerializer(
         final Function1<UserSession, scala.collection.immutable.Map<String, String>> toMap,
         final Function1<scala.collection.immutable.Map<String, String>, Try<UserSession>> fromMap
                               ) {
      super(toMap, fromMap);
   }

}
