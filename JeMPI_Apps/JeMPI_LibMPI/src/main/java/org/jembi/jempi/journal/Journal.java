package org.jembi.jempi.journal;

import io.vavr.control.Option;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.libmpi.MpiGeneralError;

public class Journal {

   private static final Logger LOGGER = LogManager.getLogger(Journal.class);

//   private final JournalInterface journalInterface;

   public Journal() {
      LOGGER.debug("new Journal");
//      journalInterface = new JournalCassandra();
   }

   public Option<MpiGeneralError> createSchema() {
      return Option.none();
//      LOGGER.debug("create schema");
//      return journalInterface.createSchema();
   }

   public void startTransaction() {
//      LOGGER.debug("start transaction");
//      journalInterface.startTransaction();
   }

   public void logEvent(final String uid, final String gid, final String did, final String event) {
//      journalInterface.logEvent(uid, gid, did, event);
   }

}
