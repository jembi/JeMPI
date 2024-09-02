package org.jembi.jempi.async_receiver;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import org.jembi.jempi.shared.models.*;
import org.jembi.jempi.shared.serdes.JsonPojoSerializer;
import org.jembi.jempi.shared.utils.AppUtils;
import org.apache.commons.codec.language.Soundex;

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
import static org.jembi.jempi.shared.config.Config.FIELDS_CONFIG;
import static org.jembi.jempi.shared.config.Config.INPUT_INTERFACE_CONFIG;
import static org.jembi.jempi.shared.utils.AppUtils.OBJECT_MAPPER;

public final class Main {

   private static final Logger LOGGER = LogManager.getLogger(Main.class.getName());

   private MyKafkaProducer<String, InteractionEnvelop> interactionEnvelopProducer;

   public Main() {
      Configurator.setLevel(this.getClass(), AppConfig.GET_LOG_LEVEL);
   }

   public static void main(final String[] args) throws ExecutionException {
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

   static AuxInteractionData auxInteractionData(final CSVRecord csvRecord) {
      return new AuxInteractionData(
            java.time.LocalDateTime.now(),
            FIELDS_CONFIG.userAuxInteractionFields
                  .stream()
                  .map(f -> new AuxInteractionData.AuxInteractionUserField(f.scName(),
                                                                           f.ccName(),
                                                                           csvRecord.get(f.source().csvCol())))
                  .toList());
   }

   static SourceId sourceIdData(final CSVRecord csvRecord) {
      return new SourceId(
            null,
            csvRecord.get(INPUT_INTERFACE_CONFIG.sourceIdFacilityCsvCol),
            csvRecord.get(INPUT_INTERFACE_CONFIG.sourceIdPatientCsvCol));
   }

   private static DemographicData demographicData(final CSVRecord csvRecord) {

      final var data = new DemographicData(INPUT_INTERFACE_CONFIG.demographicDataSource
                                                 .stream()
                                                 .map(f -> new DemographicData.DemographicField(
                                                       f.getLeft(),
                                                       (f.getRight().csvCol() != null)
                                                             ? csvRecord.get(f.getRight().csvCol())
                                                             : AppUtils.applyFunction(f.getRight().generate().func())))
                                                 .toList());

      try {
         LOGGER.debug("{}", OBJECT_MAPPER.writeValueAsString(data));
      } catch (JsonProcessingException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
      }
      return data;
   }

   private void sendToKafka(
         final String key,
         final InteractionEnvelop interactionEnvelop) throws InterruptedException, ExecutionException {
      try {
         interactionEnvelopProducer.produceSync(key, interactionEnvelop);
      } catch (NullPointerException ex) {
         LOGGER.error(ex.getLocalizedMessage(), ex);
      }
   }

   private void apacheReadCSV(
         final String fileName,
         final UploadConfig config)
         throws InterruptedException, ExecutionException {
      try {
         final var filePathUri = Paths.get(fileName);
         final var dtf = DateTimeFormatter.ofPattern("uuuu/MM/dd HH:mm:ss");
         final var now = LocalDateTime.now();
         final var stanDate = dtf.format(now);
         final var uuid = UUID.randomUUID().toString();
         final var tag = FilenameUtils.getBaseName(FilenameUtils.removeExtension(fileName));

         try (var reader = Files.newBufferedReader(filePathUri)) {

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
            sendToKafka(uuid,
                        new InteractionEnvelop(InteractionEnvelop.ContentType.BATCH_START_SENTINEL,
                                               tag,
                                               updateStan(stanDate, index),
                                               null,
                                               createSessionMetadata(index, updateStan(stanDate, index), config)));

            for (CSVRecord csvRecord : csvParser) {
               final var patientRecord = demographicData(csvRecord);
               String givenName = patientRecord.fields.stream()
                  .filter(field -> "given_name".equals(field.ccTag()))
                  .map(DemographicData.DemographicField::value)
                  .findFirst()
                  .orElse("");
               String familyName = patientRecord.fields.stream()
                  .filter(field -> "family_name".equals(field.ccTag()))
                  .map(DemographicData.DemographicField::value)
                  .findFirst()
                  .orElse("");

               String partitionKey = "";
               if (!givenName.isEmpty()) {
                  partitionKey += new Soundex().soundex(givenName);
               }
               if (!familyName.isEmpty()) {
                  partitionKey += new Soundex().soundex(familyName);
               }
               if (givenName.isEmpty() && familyName.isEmpty()) {
                  partitionKey += "Unknown";
               }
               LOGGER.info("Using Kafka topic/partition: " + partitionKey);

               final var interactionEnvelop = new InteractionEnvelop(InteractionEnvelop.ContentType.BATCH_INTERACTION,
                                                                     tag,
                                                                     updateStan(stanDate, ++index),
                                                                     new Interaction(null,
                                                                                     sourceIdData(csvRecord),
                                                                                     auxInteractionData(csvRecord),
                                                                                     demographicData(csvRecord)),
                                                                     createSessionMetadata(index,
                                                                                           updateStan(stanDate, index),
                                                                                           config));
               sendToKafka(partitionKey, interactionEnvelop);
            }

            sendToKafka(uuid,
                        new InteractionEnvelop(InteractionEnvelop.ContentType.BATCH_END_SENTINEL,
                                               tag,
                                               updateStan(stanDate, ++index),
                                               null,
                                               createSessionMetadata(index,
                                                                     updateStan(stanDate, index),
                                                                     config)));
         }
      } catch (IOException ex) {
         LOGGER.error(ex.getLocalizedMessage(), ex);
      }
   }

   private String updateStan(
         final String stanDate,
         final int recCount) {
      return String.format(Locale.ROOT, "%s:%07d", stanDate, recCount);
   }

   private UploadConfig readUploadConfigFromFile(final String fileName) {
      try (var reader = Files.newBufferedReader(Path.of(fileName))) {
         var firstLine = reader.readLine();
         try {
            var config = AppUtils.OBJECT_MAPPER.readValue(firstLine, UploadConfig.class);
            return config;
         } catch (JsonProcessingException e) {
            LOGGER.error("Failed to map uploadConfig in file to json: " + e.getLocalizedMessage());
         }

      } catch (IOException e) {
         LOGGER.error("Failed to read uploadConfig.csv: " + e.getLocalizedMessage());
      }
      return null;
   }

   private void handleEvent(final WatchEvent<?> event) throws InterruptedException, ExecutionException {
      WatchEvent.Kind<?> kind = event.kind();
      LOGGER.info("EVENT: {}", kind);
      if (ENTRY_CREATE.equals(kind)) {
         final WatchEvent<Path> ev = cast(event);
         final Path filename = ev.context();
         final String name = filename.toString();
         LOGGER.info("A new file {} was created", filename);
         if (name.endsWith("uploadConfig.csv")) {
            UploadConfig config = readUploadConfigFromFile("csv/" + filename);
            apacheReadCSV("csv/" + filename, config);
         } else if (name.endsWith(".csv")) {
            LOGGER.info("Process CSV file: {}", filename);
            apacheReadCSV("csv/" + filename, null);
         }
      } else if (ENTRY_MODIFY.equals(kind) || ENTRY_DELETE.equals(kind)) {
         LOGGER.info("EVENT: {}", kind);
      }
   }

   private Serializer<String> keySerializer() {
      return new StringSerializer();
   }

   private Serializer<InteractionEnvelop> interactionSerializer() {
      return new JsonPojoSerializer<>();
   }

   private void run() throws ExecutionException {
      LOGGER.info("KAFKA: {} {}", AppConfig.KAFKA_BOOTSTRAP_SERVERS, AppConfig.KAFKA_CLIENT_ID);
      interactionEnvelopProducer = new MyKafkaProducer<>(AppConfig.KAFKA_BOOTSTRAP_SERVERS,
                                                         GlobalConstants.TOPIC_INTERACTION_ETL,
                                                         keySerializer(),
                                                         interactionSerializer(),
                                                         AppConfig.KAFKA_CLIENT_ID);
      try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
         final var csvDir = Path.of("./csv");
         csvDir.register(watchService, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);
         do {
            WatchKey key = watchService.take();
            for (WatchEvent<?> event : key.pollEvents()) {
               handleEvent(event);
            }
            key.reset();
         } while (true);
      } catch (IOException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
      } catch (InterruptedException e) {
         LOGGER.warn(e.getLocalizedMessage(), e);
      }
   }

   private SessionMetadata createSessionMetadata(
         final int index,
         final String stan,
         final UploadConfig config) {
      return new SessionMetadata(new CommonMetaData(stan, config),
                                 new UIMetadata(null),
                                 new AsyncReceiverMetadata(AppUtils.timeStamp()),
                                 new ETLMetadata(null),
                                 new ControllerMetadata(null),
                                 new LinkerMetadata(null));
   }
}
