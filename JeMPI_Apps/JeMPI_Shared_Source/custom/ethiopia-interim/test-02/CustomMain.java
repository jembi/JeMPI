package org.jembi.jempi.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.enums.CSVReaderNullFieldIndicator;
import com.opencsv.exceptions.CsvValidationException;
import okhttp3.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.AppConfig;
import org.jembi.jempi.shared.models.CustomEntity;
import org.jembi.jempi.shared.models.RestLink;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import static java.nio.file.StandardWatchEventKinds.*;
import static org.jembi.jempi.shared.utils.AppUtils.OBJECT_MAPPER;

public final class CustomMain {
   private static final Logger LOGGER = LogManager.getLogger(CustomMain.class);
   private static final String URL = String.format("http://%s:%d", AppConfig.LINKER_SERVER_HOST, AppConfig.LINKER_SERVER_PORT);
   final String URL_LINK = String.format("%s/JeMPI/link", URL);
   private final OkHttpClient client = new OkHttpClient();

   public static void main(final String[] args) {
      new CustomMain().run();
   }

   private static <T> WatchEvent<T> cast(WatchEvent<?> event) {
      return (WatchEvent<T>) event;
   }

   private void linkEntity(final String stan, final CustomEntity entity) {
      final HttpUrl.Builder urlBuilder = Objects.requireNonNull(HttpUrl.parse(URL_LINK)).newBuilder();
      final String url = urlBuilder.build().toString();
      try {
         var body = RequestBody.create(OBJECT_MAPPER.writeValueAsString(new RestLink(stan, 0.65F, entity)),
                                       MediaType.parse("application/json; charset=utf-8"));

         final Request request = new Request.Builder().url(url).post(body).build();
         final Call call = client.newCall(request);
         try (var response = call.execute()) {
            assert response.body() != null;
            var rsp = response.body().string();
            LOGGER.info("{}", rsp);
         } catch (IOException e) {
            throw new RuntimeException(e);
         }
      } catch (JsonProcessingException ex) {
         throw new RuntimeException(ex);
      }
   }

   private void openCsvReadCSV(final String filename) {
      try (CSVReader reader = new CSVReaderBuilder(new FileReader(filename)).withSkipLines(1).withFieldAsNull(
            CSVReaderNullFieldIndicator.BOTH).build()) {
         String[] line;
         final var dtf = DateTimeFormatter.ofPattern("uuuu/MM/dd HH:mm:ss");
         final var now = LocalDateTime.now();
         final var stanDate = dtf.format(now);
         var index = 0;
         while ((line = reader.readNext()) != null) {
            index += 1;
            final var stan = String.format("%s:%07d", stanDate, index);
            final var entity = new CustomEntity(null, line[0], line[1], line[2], line[3], line[4], line[5], line[6], line[7],
                                                line[8], line[9]);
            linkEntity(stan, entity);
         }
      } catch (IOException | CsvValidationException e) {
         LOGGER.error(e.toString());
      }
   }

   private void handleEvent(WatchEvent<?> event) {
      WatchEvent.Kind<?> kind = event.kind();
      LOGGER.info("EVENT: {}", kind);
      if (ENTRY_CREATE.equals(kind)) {
         WatchEvent<Path> ev = cast(event);
         Path filename = ev.context();
         String name = filename.toString();
         LOGGER.info("A new file {} was created", filename);
         if (name.endsWith(".csv")) {
            LOGGER.info("Process CSV file: {}", filename);
            openCsvReadCSV("csv/" + filename);
         }
      } else if (ENTRY_MODIFY.equals(kind)) {
         LOGGER.info("EVENT:{}", kind);
      } else if (ENTRY_DELETE.equals(kind)) {
         LOGGER.info("EVENT: {}", kind);
      }
   }

   private void run() {
      LOGGER.info("{}", URL);
      try (WatchService watcher = FileSystems.getDefault().newWatchService()) {
         Path csvDir = Paths.get("./csv");
         csvDir.register(watcher, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);
         while (true) {
            try {
               WatchKey key = watcher.take();
               for (WatchEvent<?> event : key.pollEvents()) {
                  handleEvent(event);
               }
               key.reset();
            } catch (InterruptedException ex) {
               throw new RuntimeException(ex);
            }
         }
      } catch (IOException ex) {
         throw new RuntimeException(ex);
      }
   }

}
