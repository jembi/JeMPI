package org.jembi.jempi.api.user;


import com.softwaremill.session.MultiValueSessionSerializer;
import scala.Function1;
import scala.util.Try;

class UserSessionSerializer extends MultiValueSessionSerializer<UserSession> {

   UserSessionSerializer(
         final Function1<UserSession, scala.collection.immutable.Map<String, String>> toMap,
         final Function1<scala.collection.immutable.Map<String, String>, Try<UserSession>> fromMap) {
      super(toMap, fromMap);
   }

}
