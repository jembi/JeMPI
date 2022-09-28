package org.jembi.jempi.staging;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.AppConfig;

import java.util.concurrent.CountDownLatch;

public final class Main {

   private static final Logger LOGGER = LogManager.getLogger(Main.class);

   private Main() {
   }

   public static void main(final String[] args) {
      new Main().run();
   }

   private void run() {
      LOGGER.info("KAFKA: {} {} {}",
                  AppConfig.KAFKA_BOOTSTRAP_SERVERS,
                  AppConfig.KAFKA_APPLICATION_ID,
                  AppConfig.KAFKA_CLIENT_ID);
      final var frontEndStream = new CustomFrontEndStream();
      final var latch = new CountDownLatch(1);
      Runtime.getRuntime().addShutdownHook(new Thread(() -> {
         frontEndStream.close();
         latch.countDown();
      }));
      try {
         latch.await();
      } catch (InterruptedException e) {
         LOGGER.error(e.getLocalizedMessage());
      }
   }

}
