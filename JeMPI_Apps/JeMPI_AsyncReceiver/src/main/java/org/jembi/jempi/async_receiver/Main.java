package org.jembi.jempi.async_receiver;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FilenameUtils;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.jembi.jempi.AppConfig;
import org.jembi.jempi.shared.kafka.MyKafkaProducer;
import org.jembi.jempi.shared.models.GlobalConstants;
import org.jembi.jempi.shared.models.Interaction;
import org.jembi.jempi.shared.models.InteractionEnvelop;
import org.jembi.jempi.shared.models.UploadConfig;
import org.jembi.jempi.shared.serdes.JsonPojoSerializer;
import org.jembi.jempi.shared.utils.AppUtils;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static java.nio.file.StandardWatchEventKinds.*;

public final class Main {

   private static final Logger LOGGER = LogManager.getLogger(Main.class.getName());

   private MyKafkaProducer<String, InteractionEnvelop> interactionEnvelopProducer;

   public Main() {
      Configurator.setLevel(this.getClass(), AppConfig.GET_LOG_LEVEL);
   }

   public static void main(final String[] args) throws InterruptedException, ExecutionException, IOException {
      new Main().run();
   }

   @SuppressWarnings("unchecked")
   private static <T> WatchEvent<T> cast(final WatchEvent<?> event) {
      return (WatchEvent<T>) event;
   }

   static String parseRecordNumber(final String in) {
      final var regex = "^rec-(?<rnum>\\d+)-(?<class>(org|aaa|dup|bbb)?)-?(?<dnum>\\d+)?$";
      final Pattern pattern = Pattern.compile(regex);
      final Matcher matcher = pattern.matcher(in);
      if (matcher.find()) {
         final var rNumber = matcher.group("rnum");
         final var klass = matcher.group("class");
         final var dNumber = matcher.group("dnum");
         return String.format(Locale.ROOT,
               "rec-%010d-%s-%d",
               Integer.parseInt(rNumber),
               klass,
               (("org".equals(klass) || "aaa".equals(klass))
                     ? 0
                     : Integer.parseInt(dNumber)));
      }
      return in;
   }

   private void sendInteractionToKafka(
         final String key,
         final InteractionEnvelop interactionEnvelop) throws InterruptedException, ExecutionException {
      try {
         interactionEnvelopProducer.produceSync(key, interactionEnvelop);
      } catch (NullPointerException ex) {
         LOGGER.error(ex.getLocalizedMessage(), ex);
      }
   }

   private long getRowSize(final String[] values) {
      long size = 0;

      for (String str : values) {
         if (str != null) {
            size += 24 + (str.length() * 2L);
         }
      }
      return size;
   }

   private void apacheReadCSV(final String fileName, final UploadConfig config)
         throws InterruptedException, ExecutionException {
      try {
         final var filePathUri = Paths.get(fileName);
         final var reader = Files.newBufferedReader(filePathUri);
         final var dtf = DateTimeFormatter.ofPattern("uuuu/MM/dd HH:mm:ss");
         final var now = LocalDateTime.now();
         final var stanDate = dtf.format(now);
         final var uuid = UUID.randomUUID().toString();
         final var tag = FilenameUtils.getBaseName(FilenameUtils.removeExtension(fileName));

         //ignore the first line when upload config exists
         if (config != null) {
            reader.readLine();
         }

         final var csvParser = CSVFormat.DEFAULT.builder()
               .setHeader()
               .setSkipHeaderRecord(true)
               .setIgnoreEmptyLines(true)
               .setNullString(null)
               .build()
               .parse(reader);

         int index = 0;
         sendInteractionToKafka(uuid,
                                new InteractionEnvelop(InteractionEnvelop.ContentType.BATCH_START_SENTINEL,
                     tag,
                     String.format(Locale.ROOT, "%s:%07d", stanDate, ++index),
                     null, null));
         for (CSVRecord csvRecord : csvParser) {
            final var interactionEnvelop = new InteractionEnvelop(InteractionEnvelop.ContentType.BATCH_INTERACTION,
                  tag,
                  String.format(Locale.ROOT, "%s:%07d", stanDate, ++index),
                  new Interaction(null,
                        CustomAsyncHelper.customSourceId(csvRecord),
                        CustomAsyncHelper.customUniqueInteractionData(
                              csvRecord),
                        CustomAsyncHelper.customDemographicData(
                              csvRecord)), config);

            sendInteractionToKafka(UUID.randomUUID().toString(), interactionEnvelop);
         }
         sendInteractionToKafka(uuid,
                                new InteractionEnvelop(InteractionEnvelop.ContentType.BATCH_END_SENTINEL,
                     tag,
                     String.format(Locale.ROOT, "%s:%07d", stanDate, ++index),
                     null, null));
      } catch (IOException ex) {
         LOGGER.error(ex.getLocalizedMessage(), ex);
      }
   }

   private UploadConfig readUploadConfigFromFile(final String fileName) {
      try {
         var lines = Files.readAllLines(Path.of(fileName));
         var config = AppUtils.OBJECT_MAPPER.readValue(lines.getFirst(), UploadConfig.class);
         return config;
      } catch (IOException e) {
         LOGGER.error("Failed to read uploadConfig.csv" + e.getLocalizedMessage());
      }
      return null;
   }

   private void handleEvent(final WatchEvent<?> event) throws InterruptedException, ExecutionException {
      WatchEvent.Kind<?> kind = event.kind();
      LOGGER.info("EVENT: {}", kind);
      if (ENTRY_CREATE.equals(kind)) {
         WatchEvent<Path> ev = cast(event);
         Path filename = ev.context();
         String name = filename.toString();
         LOGGER.info("A new file {} was created", filename);
         if (name.endsWith("uploadConfig.csv")) {
            UploadConfig config = readUploadConfigFromFile("csv/" + filename);
            apacheReadCSV("csv/" + filename, config);
         } else if (name.endsWith(".csv")) {
            LOGGER.info("Process CSV file: {}", filename);
            apacheReadCSV("csv/" + filename, null);
         }
      } else if (ENTRY_MODIFY.equals(kind)) {
         LOGGER.info("EVENT: {}", kind);
      } else if (ENTRY_DELETE.equals(kind)) {
         LOGGER.info("EVENT: {}", kind);
      }
   }

   private Serializer<String> keySerializer() {
      return new StringSerializer();
   }

   private Serializer<InteractionEnvelop> interactionSerializer() {
      return new JsonPojoSerializer<>();
   }

   private void run() throws InterruptedException, ExecutionException, IOException {
      LOGGER.info("KAFKA: {} {}", AppConfig.KAFKA_BOOTSTRAP_SERVERS, AppConfig.KAFKA_CLIENT_ID);
      interactionEnvelopProducer = new MyKafkaProducer<>(AppConfig.KAFKA_BOOTSTRAP_SERVERS,
                                                         GlobalConstants.TOPIC_INTERACTION_ETL,
                                                         keySerializer(),
                                                         interactionSerializer(),
                                                         AppConfig.KAFKA_CLIENT_ID);
      try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
         final var csvDir = Path.of("./csv");
         csvDir.register(watchService, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);
         for (;;) {
            WatchKey key = watchService.take();
            for (WatchEvent<?> event : key.pollEvents()) {
               handleEvent(event);
            }
            key.reset();
         }
      } catch (IOException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
      } catch (InterruptedException e) {
         LOGGER.warn(e.getLocalizedMessage(), e);
      }
   }
}
