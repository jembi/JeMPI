package org.jembi.jempi.journal;

import io.vavr.control.Option;
import org.jembi.jempi.libmpi.MpiGeneralError;

public interface JournalInterface {
   Option<MpiGeneralError> createSchema();

   void startTransaction();

   void logEvent(final String uid, final String gid, final String did, final String event);

}
