package org.jembi.jempi.fhir_sync_sender;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import okhttp3.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.*;
import org.jembi.jempi.shared.models.CustomSourceRecord;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static java.nio.file.StandardWatchEventKinds.*;

class CustomSender {
   private static final Logger LOGGER = LogManager.getLogger(CustomSender.class.getName());
   private static final String URL = "http://localhost:50040";
   private static final String URL_LINK = String.format("%s/fhir/bundle", URL);
   private final FhirContext ctx = FhirContext.forR4();
   private final IParser parser = ctx.newJsonParser();

   private final OkHttpClient client = new OkHttpClient();

   @SuppressWarnings("unchecked")
   private static <T> WatchEvent<T> cast(WatchEvent<?> event) {
      return (WatchEvent<T>) event;
   }

   private void postBundle(final String json) {

      final HttpUrl.Builder urlBuilder = Objects.requireNonNull(HttpUrl.parse(URL_LINK)).newBuilder();
      final String url = urlBuilder.build().toString();
      var body = RequestBody.create(json, MediaType.parse("application/json; charset=utf-8"));

      final var request = new okhttp3.Request.Builder().url(url).post(body).build();

      final Call call = client.newCall(request);
      try (var response = call.execute()) {
         assert response.body() != null;
         var rsp = response.body().string();
         LOGGER.info("{}", rsp);
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   private void dispatchPatient(final String stan, final String[] fields) {
      final CustomSourceRecord rec =
            new CustomSourceRecord(CustomSourceRecord.RecordType.BATCH_RECORD,
                                   stan,
                                   fields[0], fields[1], fields[2], fields[3],
                                   fields[4], fields[5], fields[6], fields[7],
                                   fields[8], fields[9]);
      final var aux_uid = rec.auxId();
      final var givenName = rec.givenName();
      final var familyName = rec.familyName();
      final var gender = rec.gender();
      final var date = rec.dob();
      final var city = rec.city();
      final var phoneNumber = rec.phoneNumber();
      final var patient = new Patient();
      patient.addIdentifier().setUse(Identifier.IdentifierUse.SECONDARY).setValue(aux_uid);
      patient.addName().setFamily(familyName).addGiven(givenName);
      if (gender != null) {
         if ("male".equals(gender.toLowerCase(Locale.ROOT)) || "female".equals(gender.toLowerCase(Locale.ROOT))) {
            patient.setGender("male".equals(gender.toLowerCase(Locale.ROOT))
                              ? Enumerations.AdministrativeGender.MALE
                              : Enumerations.AdministrativeGender.FEMALE);
         }
         if (!StringUtils.isBlank(date) && date.length() >= 8) {
            try {
               int theYear = Integer.parseInt(date.substring(0, 4));
               int theMonth = Integer.parseInt(date.substring(4, 6));
               int theDay = Integer.parseInt(date.substring(7, 8));
               if (theMonth >= 1 && theMonth <= 12 && theDay >= 1 && theDay <= 31) {
                  patient.setBirthDateElement(new DateType(theYear, theMonth - 1, theDay));
               }
            } catch (NumberFormatException e) {
               LOGGER.error("{} - {}", date, e.getLocalizedMessage());
            }
         }
         if (city != null) {
            final var address = new Address().setCity(city);
            patient.addAddress(address);
         }
         if (phoneNumber != null) {
            final List<ContactPoint> contactPointList = new ArrayList<>();
            ContactPoint contactPoint = new ContactPoint();
            contactPoint.setSystem(ContactPoint.ContactPointSystem.PHONE).setValue(phoneNumber);
            contactPointList.add(contactPoint);
            patient.setTelecom(contactPointList);
         }
         final Bundle bundle = new Bundle();
         bundle.setType(Bundle.BundleType.TRANSACTION);
         bundle.addEntry()
               .setFullUrl(patient.getIdElement().getValue())
               .setResource(patient)
               .getRequest()
               .setMethod(Bundle.HTTPVerb.POST);
         final var json = parser.encodeResourceToString(bundle);
         LOGGER.debug("{}", json);
         postBundle(json);

      }
   }

   private void apacheReadCSV(final String fileName) {
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
            dispatchPatient(stan, csvRecord.toList().toArray(new String[0]));
         }
      } catch (IOException ex) {
         LOGGER.error(ex.getLocalizedMessage(), ex);
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
            apacheReadCSV("csv/" + filename);
         }
      } else if (ENTRY_MODIFY.equals(kind)) {
         LOGGER.info("EVENT:{}", kind);
      } else if (ENTRY_DELETE.equals(kind)) {
         LOGGER.info("EVENT: {}", kind);
      }
   }

   void run() {
      try (WatchService watcher = FileSystems.getDefault().newWatchService()) {
         Path csvDir = Paths.get("./csv");
         csvDir.register(watcher, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);
         while (true) {
            WatchKey key = watcher.take();
            for (WatchEvent<?> event : key.pollEvents()) {
               LOGGER.info("Event: {}", event);
               handleEvent(event);
            }
            key.reset();
         }
      } catch (IOException | InterruptedException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
      }
   }

}