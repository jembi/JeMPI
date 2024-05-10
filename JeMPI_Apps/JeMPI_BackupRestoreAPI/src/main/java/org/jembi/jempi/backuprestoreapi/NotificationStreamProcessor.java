package org.jembi.jempi.backuprestoreapi;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class NotificationStreamProcessor {

   private static final Logger LOGGER = LogManager.getLogger(NotificationStreamProcessor.class);
   private final KafkaStreams notificationKafkaStreams = null;
   private PsqlNotifications psqlNotifications;

}
