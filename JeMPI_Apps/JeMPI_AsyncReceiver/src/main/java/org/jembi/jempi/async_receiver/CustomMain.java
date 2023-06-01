package org.jembi.jempi.async_receiver;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.AppConfig;
import org.jembi.jempi.shared.kafka.MyKafkaProducer;
import org.jembi.jempi.shared.models.CustomDemographicData;
import org.jembi.jempi.shared.models.GlobalConstants;
import org.jembi.jempi.shared.models.Interaction;
import org.jembi.jempi.shared.models.InteractionEnvelop;
import org.jembi.jempi.shared.serdes.JsonPojoSerializer;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.file.StandardWatchEventKinds.*;

public final class CustomMain {

   private static final Logger LOGGER = LogManager.getLogger(CustomMain.class.getName());
   private static final int REC_NUM_IDX = 0;
   private static final int GIVEN_NAME_IDX = 1;
   private static final int FAMILY_NAME_IDX = 2;
   private static final int GENDER_IDX = 3;
   private static final int DOB_IDX = 4;
   private static final int CITY_IDX = 5;
   private static final int PHONE_NUMBER_IDX = 6;
   private static final int NATIONAL_ID_IDX = 7;

   private MyKafkaProducer<String, InteractionEnvelop> interactionEnvelopProducer;

   public static void main(final String[] args)
         throws InterruptedException, ExecutionException, IOException {
      new CustomMain().run();
   }

   @SuppressWarnings("unchecked")
   private static <T> WatchEvent<T> cast(final WatchEvent<?> event) {
      return (WatchEvent<T>) event;
   }

   private static String parseRecordNumber(final String in) {
      final var regex = "^rec-(?<rnum>\\d+)-(?<class>org|aaa|dup|bbb)-?(?<dnum>\\d+)?$";
      final Pattern pattern = Pattern.compile(regex);
      final Matcher matcher = pattern.matcher(in);
      if (matcher.find()) {
         final var rNumber = matcher.group("rnum");
         final var klass = matcher.group("class");
         final var dNumber = matcher.group("dnum");
         final var recNumber = String.format("rec-%010d-%s-%d",
                                             Integer.parseInt(rNumber),
                                             klass,
                                             (("org".equals(klass) || "aaa".equals(klass))
                                                    ? 0
                                                    : Integer.parseInt(dNumber)));
         LOGGER.debug("{}", recNumber);
         return recNumber;
      }
      return null;
   }

   private static CustomDemographicData getCustomDemographicData(final CSVRecord csvRecord) {
      return new CustomDemographicData(
            parseRecordNumber(csvRecord.get(REC_NUM_IDX)),
            csvRecord.get(GIVEN_NAME_IDX),
            csvRecord.get(FAMILY_NAME_IDX),
            csvRecord.get(GENDER_IDX),
            csvRecord.get(DOB_IDX),
            csvRecord.get(CITY_IDX),
            csvRecord.get(PHONE_NUMBER_IDX),
            csvRecord.get(NATIONAL_ID_IDX));
   }

   private void sendToKafka(
         final String key,
         final InteractionEnvelop interactionEnvelop)
         throws InterruptedException, ExecutionException {
      try {
         LOGGER.debug("{}", interactionEnvelop);
         interactionEnvelopProducer.produceSync(key, interactionEnvelop);
      } catch (NullPointerException ex) {
         LOGGER.error(ex.getLocalizedMessage(), ex);
      }
   }

   private void apacheReadCSV(final String fileName)
         throws InterruptedException, ExecutionException {
      try {
         final var reader = Files.newBufferedReader(Paths.get(fileName));
         final var dtf = DateTimeFormatter.ofPattern("uuuu/MM/dd HH:mm:ss");
         final var now = LocalDateTime.now();
         final var stanDate = dtf.format(now);
         final var uuid = UUID.randomUUID().toString();

         final var csvParser = CSVFormat
               .DEFAULT
               .builder()
               .setHeader()
               .setSkipHeaderRecord(true)
               .setIgnoreEmptyLines(true)
               .setNullString(null)
               .build()
               .parse(reader);

         int index = 0;
         sendToKafka(uuid, new InteractionEnvelop(InteractionEnvelop.ContentType.BATCH_START_SENTINEL, fileName,
                                                  String.format("%s:%07d", stanDate, ++index), null));
         for (CSVRecord csvRecord : csvParser) {
            sendToKafka(uuid, new InteractionEnvelop(InteractionEnvelop.ContentType.BATCH_INTERACTION, fileName,
                                                     String.format("%s:%07d", stanDate, ++index),
                                                     new Interaction(null, null, getCustomDemographicData(csvRecord))));
         }
         sendToKafka(uuid, new InteractionEnvelop(InteractionEnvelop.ContentType.BATCH_END_SENTINEL, fileName,
                                                  String.format("%s:%07d", stanDate, ++index), null));
      } catch (IOException ex) {
         LOGGER.error(ex.getLocalizedMessage(), ex);
      }
   }

   private void handleEvent(final WatchEvent<?> event)
         throws InterruptedException, ExecutionException {
      WatchEvent.Kind<?> kind = event.kind();
      LOGGER.info("EVENT: {}", kind);
      if (ENTRY_CREATE.equals(kind)) {
         WatchEvent<Path> ev = cast(event);
         Path filename = ev.context();
         String name = filename.toString();
         LOGGER.info("A new file {} was created", filename);
         if (name.endsWith(".csv")) {
            LOGGER.info("Process CSV file: {}", filename);
            apacheReadCSV("csv/" + filename);
         }
      } else if (ENTRY_MODIFY.equals(kind)) {
         LOGGER.info("EVENT:{}", kind);
      } else if (ENTRY_DELETE.equals(kind)) {
         LOGGER.info("EVENT: {}", kind);
      }
   }

   private Serializer<String> keySerializer() {
      return new StringSerializer();
   }

   private Serializer<InteractionEnvelop> valueSerializer() {
      return new JsonPojoSerializer<>();
   }

   private void run() throws InterruptedException, ExecutionException, IOException {
      LOGGER.info("KAFKA: {} {} {}",
                  AppConfig.KAFKA_BOOTSTRAP_SERVERS,
                  AppConfig.KAFKA_APPLICATION_ID,
                  AppConfig.KAFKA_CLIENT_ID);
      interactionEnvelopProducer = new MyKafkaProducer<>(AppConfig.KAFKA_BOOTSTRAP_SERVERS,
                                                         GlobalConstants.TOPIC_INTERACTION_ASYNC_ETL,
                                                         keySerializer(), valueSerializer(),
                                                         AppConfig.KAFKA_CLIENT_ID);
      try (WatchService watcher = FileSystems.getDefault().newWatchService()) {
         Path csvDir = Paths.get("/app/csv");
         csvDir.register(watcher, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);
         while (true) {
            WatchKey key = watcher.take();
            for (WatchEvent<?> event : key.pollEvents()) {
               handleEvent(event);
            }
            key.reset();
         }
      }
   }
}
