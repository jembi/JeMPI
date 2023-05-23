package org.jembi.jempi.async_receiver;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.AppConfig;
import org.jembi.jempi.shared.kafka.MyKafkaProducer;
import org.jembi.jempi.shared.models.AsyncSourceRecord;
import org.jembi.jempi.shared.models.BatchMetaData;
import org.jembi.jempi.shared.models.CustomSourceRecord;
import org.jembi.jempi.shared.models.GlobalConstants;
import org.jembi.jempi.shared.serdes.JsonPojoSerializer;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

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

   private MyKafkaProducer<String, AsyncSourceRecord> sourceRecordProducer;

   public static void main(final String[] args)
         throws InterruptedException, ExecutionException, IOException {
      new CustomMain().run();
   }

   @SuppressWarnings("unchecked")
   private static <T> WatchEvent<T> cast(final WatchEvent<?> event) {
      return (WatchEvent<T>) event;
   }

   private void sendToKafka(
         final String key,
         final AsyncSourceRecord asyncSourceRecord)
         throws InterruptedException, ExecutionException {
      try {
         LOGGER.debug("{}", asyncSourceRecord);
         sourceRecordProducer.produceSync(key, asyncSourceRecord);
      } catch (NullPointerException ex) {
         LOGGER.error(ex.getLocalizedMessage(), ex);
      }
   }

   /*
   private void sendToKafka(
         final CustomSourceRecord.RecordType recordType,
         final String fileName) throws InterruptedException,
                                 ExecutionException {
      try {
         final String userName = System.getProperty("user.name");
         final Boolean delayLinker = false;
         final String tag = recordType == CustomSourceRecord.RecordType.BATCH_START
               ? "START"
               : "END";
         final BatchMetaData metaData = new BatchMetaData(BatchMetaData.FileType.CSV,
                                                          LocalDateTime.now().toString(),
                                                          fileName,
                                                          userName,
                                                          delayLinker,
                                                          tag);
         LOGGER.debug("{}", metaData);
         metaDataProducer.produceSync(tag, metaData);
      } catch (NullPointerException ex) {
         LOGGER.error(ex.getLocalizedMessage(), ex);
      }
   }
*/


//   private void sendToKafka(
//         final String stan,
//         final String[] fields) throws InterruptedException, ExecutionException {
//      try {
//         final CustomSourceRecord sourceRecord =
//               new CustomSourceRecord(CustomSourceRecord.RecordType.BATCH_RECORD,
//                                      stan,
//                                      fields[0], fields[1], fields[2], fields[3],
//                                      fields[4], fields[5], fields[6], fields[7],
//                                      fields[8], fields[9], fields[10]);
//
//         LOGGER.debug("{}", sourceRecord);
//         sourceRecordProducer.produceSync(sourceRecord.auxId().substring(0, 13), sourceRecord);
//      } catch (NullPointerException ex) {
//         LOGGER.error(ex.getLocalizedMessage(), ex);
//      }
//   }

   private void apacheReadCSV(final String fileName)
         throws InterruptedException, ExecutionException {
      try {
         final var reader = Files.newBufferedReader(Paths.get(fileName));
         final var dtf = DateTimeFormatter.ofPattern("uuuu/MM/dd HH:mm:ss");
         final var now = LocalDateTime.now();
         final var stanDate = dtf.format(now);
         final var uuid = UUID.randomUUID().toString();
         final var batchMetaData = new BatchMetaData(BatchMetaData.FileType.CSV,
                                                     LocalDateTime.now().toString(),
                                                     fileName,
                                                     null,
                                                     null,
                                                     null);

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
         sendToKafka(uuid,
                     new AsyncSourceRecord(AsyncSourceRecord.RecordType.BATCH_START,
                                           batchMetaData, null));
         for (CSVRecord csvRecord : csvParser) {
            final var customSourceRecord = new CustomSourceRecord(
                  String.format("%s:%07d", stanDate, ++index),
                  csvRecord.get(REC_NUM_IDX),
                  csvRecord.get(GIVEN_NAME_IDX),
                  csvRecord.get(FAMILY_NAME_IDX),
                  csvRecord.get(GENDER_IDX),
                  csvRecord.get(DOB_IDX),
                  csvRecord.get(CITY_IDX),
                  csvRecord.get(PHONE_NUMBER_IDX),
                  csvRecord.get(NATIONAL_ID_IDX));

            final var asyncSourceRecord = new AsyncSourceRecord(AsyncSourceRecord.RecordType.BATCH_RECORD,
                                                                batchMetaData,
                                                                customSourceRecord);
            sendToKafka(uuid, asyncSourceRecord);

         }
         sendToKafka(uuid,
                     new AsyncSourceRecord(AsyncSourceRecord.RecordType.BATCH_END,
                                           batchMetaData,
                                           null));
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

   private Serializer<AsyncSourceRecord> valueSerializer() {
      return new JsonPojoSerializer<>();
   }

   private void run() throws InterruptedException, ExecutionException, IOException {
      LOGGER.info("KAFKA: {} {} {}",
                  AppConfig.KAFKA_BOOTSTRAP_SERVERS,
                  AppConfig.KAFKA_APPLICATION_ID,
                  AppConfig.KAFKA_CLIENT_ID);
      sourceRecordProducer = new MyKafkaProducer<>(AppConfig.KAFKA_BOOTSTRAP_SERVERS,
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
