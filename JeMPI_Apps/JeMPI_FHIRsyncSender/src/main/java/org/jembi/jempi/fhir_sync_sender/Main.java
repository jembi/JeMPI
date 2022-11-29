package org.jembi.jempi.fhir_sync_sender;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {

   private static final Logger LOGGER = LogManager.getLogger(Main.class);

   public static void main(String[] args) {
      LOGGER.info ("FHIR Sync Sender");
      new CustomSender().run();
   }
}