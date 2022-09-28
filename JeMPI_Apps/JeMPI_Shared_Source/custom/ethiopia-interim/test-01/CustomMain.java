package org.jembi.jempi.test;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.enums.CSVReaderNullFieldIndicator;
import com.opencsv.exceptions.CsvValidationException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.AppConfig;
import org.jembi.jempi.shared.kafka.MyKafkaProducer;
import org.jembi.jempi.shared.models.CustomTesterPatientRecord;
import org.jembi.jempi.shared.models.GlobalConstants;
import org.jembi.jempi.shared.serdes.JsonPojoSerializer;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutionException;

import static java.nio.file.StandardWatchEventKinds.*;

public final class CustomMain {

   private static final Logger LOGGER = LogManager.getLogger(CustomMain.class.getName());
   private MyKafkaProducer<String, CustomTesterPatientRecord> producer;

   public static void main(final String[] args)
         throws InterruptedException, ExecutionException, IOException {
      new CustomMain().run();
   }

   @SuppressWarnings("unchecked")
   private static <T> WatchEvent<T> cast(WatchEvent<?> event) {
      return (WatchEvent<T>) event;
   }

   private void sendToKafka(final CustomTesterPatientRecord.RecordType recordType) throws InterruptedException,
                                                                                          ExecutionException {
      try {
         final CustomTesterPatientRecord rec = new CustomTesterPatientRecord(recordType);
         LOGGER.debug("{}", rec);
         producer.produceSync(rec.recordType() == CustomTesterPatientRecord.RecordType.BATCH_START
                              ? "START"
                              : "END",
                              rec);
      } catch (NullPointerException ex) {
         LOGGER.error(ex.getLocalizedMessage(), ex);
      }
   }


   private void sendToKafka(final String stan, final String[] fields) throws InterruptedException, ExecutionException {
      try {
         final CustomTesterPatientRecord rec =
               new CustomTesterPatientRecord(CustomTesterPatientRecord.RecordType.BATCH_RECORD,
                                             stan,
                                             fields[0], fields[1], fields[2], fields[3], fields[4],
                                             fields[5], fields[6], fields[7], fields[8], fields[9],
                                             fields[10]);
         LOGGER.debug("{}", rec);
         producer.produceSync(rec.auxId().substring(0, 13), rec);
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

         final var csvParser = CSVFormat
               .DEFAULT
               .builder()
               .setSkipHeaderRecord(true)
               .setIgnoreEmptyLines(true)
               .setNullString(null)
               .build()
               .parse(reader);

         int index = 0;
         for (CSVRecord csvRecord : csvParser) {
            index += 1;
            final var stan = String.format("%s:%07d", stanDate, index);
            sendToKafka(stan, csvRecord.toList().toArray(new String[0]));
         }
      } catch (IOException ex) {
         LOGGER.error(ex.getLocalizedMessage(), ex);
      }
   }

   private void openCsvReadCSV(final String filename)
         throws InterruptedException, ExecutionException {
      try (CSVReader reader = new CSVReaderBuilder(new FileReader(filename))
            .withSkipLines(1)
            .withFieldAsNull(CSVReaderNullFieldIndicator.BOTH)
            .build()) {
         String[] line;
         sendToKafka(CustomTesterPatientRecord.RecordType.BATCH_START);
         final var dtf = DateTimeFormatter.ofPattern("uuuu/MM/dd HH:mm:ss");
         final var now = LocalDateTime.now();
         final var stanDate = dtf.format(now);
         var index = 0;
         while ((line = reader.readNext()) != null) {
            index += 1;
            final var stan = String.format("%s:%07d", stanDate, index);
            sendToKafka(stan, line);
         }
         sendToKafka(CustomTesterPatientRecord.RecordType.BATCH_END);
      } catch (IOException | CsvValidationException e) {
         LOGGER.error(e.toString());
      }
   }

   private void handleEvent(WatchEvent<?> event)
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
//                apacheReadCSV("csv/" + filename)
            openCsvReadCSV("csv/" + filename);
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

   private Serializer<CustomTesterPatientRecord> valueSerializer() {
      return new JsonPojoSerializer<>();
   }

   private void run() throws InterruptedException, ExecutionException, IOException {
      LOGGER.info("KAFKA: {} {} {}",
                  AppConfig.KAFKA_BOOTSTRAP_SERVERS,
                  AppConfig.KAFKA_APPLICATION_ID,
                  AppConfig.KAFKA_CLIENT_ID);
      producer = new MyKafkaProducer<>(GlobalConstants.TOPIC_PATIENT_STAGING_01,
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
