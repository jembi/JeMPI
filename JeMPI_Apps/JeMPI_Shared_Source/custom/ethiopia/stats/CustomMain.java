package org.jembi.jempi.stats;

import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.AppConfig;
import org.jembi.jempi.libmpi.MpiExpandedGoldenRecord;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static java.lang.Math.min;
import static org.jembi.jempi.shared.utils.AppUtils.OBJECT_MAPPER;
import static org.jembi.jempi.shared.utils.AppUtils.isNullOrEmpty;

public final class CustomMain {

   private static final Logger LOGGER = LogManager.getLogger(CustomMain.class);
   private static final String URL = String.format("http://%s:%d", AppConfig.API_SERVER_HOST,
                                                   AppConfig.API_SERVER_PORT);
   private static final String URL_LINK = String.format("%s/JeMPI/", URL);
   private final OkHttpClient client = new OkHttpClient();

   private final Map<String, List<GoldenRecordMembers>> dataSet = new HashMap<>();


   private final int[] truePositives = {0};
   private final int[] falsePositives = {0};
   private final int[] falseNegatives = {0};

   public static void main(final String[] args) throws IOException {
      new CustomMain().run();
   }

   private Long getCount(final String field) throws IOException {
      final HttpUrl.Builder urlBuilder =
            Objects.requireNonNull(HttpUrl.parse(URL_LINK + field)).newBuilder();
      final String url = urlBuilder.build().toString();
      final Request request = new Request.Builder().url(url).build();
      final Call call = client.newCall(request);
      try (var response = call.execute()) {
         assert response.body() != null;
         var json = response.body().string();
         LOGGER.info("{}", json);
         return OBJECT_MAPPER.readValue(json, Count.class).count;
      }
   }

   private NumberOfRecords getNumberOfRecords() throws IOException {
      final HttpUrl.Builder urlBuilder =
            Objects.requireNonNull(HttpUrl.parse(URL_LINK + "NumberOfRecords")).newBuilder();
      final String url = urlBuilder.build().toString();
      final Request request = new Request.Builder().url(url).build();
      final Call call = client.newCall(request);
      try (var response = call.execute()) {
         assert response.body() != null;
         var json = response.body().string();
         LOGGER.info("{}", json);
         return OBJECT_MAPPER.readValue(json, NumberOfRecords.class);
      }
   }

   private GoldenIdList getGoldenIdList() throws IOException {
      final HttpUrl.Builder urlBuilder =
            Objects.requireNonNull(HttpUrl.parse(URL_LINK + "GoldenIdList")).newBuilder();
      final String url = urlBuilder.build().toString();
      final Request request = new Request.Builder().url(url).build();
      final Call call = client.newCall(request);
      try (var response = call.execute()) {
         assert response.body() != null;
         var json = response.body().string();
         return OBJECT_MAPPER.readValue(json, GoldenIdList.class);
      }
   }

   private GoldenRecordDocuments getGoldenRecordDocumentsList(final List<String> ids) throws IOException {
      final HttpUrl.Builder urlBuilder =
            Objects.requireNonNull(HttpUrl.parse(URL_LINK + "GoldenRecordDocuments")).newBuilder();
      ids.forEach(id -> urlBuilder.addQueryParameter("uid", id));
      final String url = urlBuilder.build().toString();
      final Request request = new Request.Builder().url(url).build();
      final Call call = client.newCall(request);
      try (var response = call.execute()) {
         assert response.body() != null;
         var json = response.body().string();
         LOGGER.info("{}", json);
         return OBJECT_MAPPER.readValue(json, GoldenRecordDocuments.class);
      }
   }

   private void updateStatsDataSet(MpiExpandedGoldenRecord goldenRecord) {
      final String goldenRecordAuxId = goldenRecord.customGoldenRecord().auxId();
      final String goldenRecordNumber = goldenRecordAuxId.substring(0, 12);

      final var entry = dataSet.get(goldenRecordNumber);
      final List<String> list = new ArrayList<>();
      goldenRecord.mpiEntityList().forEach(mpiEntity -> list.add(mpiEntity.entity().auxId()));
      if (isNullOrEmpty(entry)) {
         final List<GoldenRecordMembers> membersList = new ArrayList<>();
         membersList.add(new GoldenRecordMembers(goldenRecordAuxId, list));
         dataSet.put(goldenRecordNumber, membersList);
      } else {
         entry.add(new GoldenRecordMembers(goldenRecordAuxId, list));
      }
   }

   private void displayGoldenRecordDocuments(final PrintWriter writer, final MpiExpandedGoldenRecord goldenRecord) {
      final var rot = goldenRecord.customGoldenRecord();
      if (writer != null) {
         writer.printf("GoldenRecord,%s,%s,%s,%s,%s,%s,%s,%s%n",
                       rot.uid(), rot.auxId(),
                       rot.nameGiven(), rot.nameFather(),
                       rot.nameFathersFather(), rot.nameMother(), rot.nameMothersFather(),
                       rot.gender(),
                       rot.dob(),
                       rot.phoneNumber());
         goldenRecord.mpiEntityList().forEach(mpiEntity -> {
            final var entity = mpiEntity.entity();
            writer.format(Locale.ENGLISH,
                          "document,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%f%n",
                          entity.uid(), entity.auxId(),
                          entity.nameGiven(), entity.nameFather(), 
                          entity.nameFathersFather(), entity.nameMother(), entity.nameMothersFather(),
                          entity.gender(),
                          entity.dob(),
                          entity.phoneNumber(),
                          mpiEntity.score());
         });
      }
   }

   private void processSubList(final PrintWriter writer, final int fromIdx, final int toIdx,
                               final List<String> ids) throws IOException {
      var subList = ids.subList(fromIdx, toIdx);
      var goldenRecordDocuments = getGoldenRecordDocumentsList(subList);
      goldenRecordDocuments.goldenRecords.forEach(this::updateStatsDataSet);
      goldenRecordDocuments.goldenRecords.forEach(rec -> displayGoldenRecordDocuments(writer, rec));
   }

   private void run() throws IOException {
      var documentCount = getCount("DocumentCount");
      var goldenRecordCount = getCount("GoldenRecordCount");
      var numberOfRecords = getNumberOfRecords();
      var goldenIdList = getGoldenIdList();
      System.out.printf("Document Count:      %d%n", documentCount);
      System.out.printf("Golden Record Count: %d%n", goldenRecordCount);
      System.out.printf("Number of Records:   %d,%d%n", numberOfRecords.documents, numberOfRecords.goldenRecords);
      System.out.printf("Number if id's:      %d%n", goldenIdList.records.size());
      final var goldenRecords = goldenIdList.records.size();
      final var subListSize = 100L;
      final var subLists = goldenRecords / min(subListSize, goldenRecords);
      final var finalSubListSize = goldenRecords % subListSize;
      System.out.printf("Golden Records:      %d%n", goldenRecords);
      System.out.printf("Sub List Size:       %d%n", subListSize);
      System.out.printf("Sub Lists:           %d%n", subLists);
      System.out.printf("Final Sub List Size: %d%n", finalSubListSize);

      int fromIdx;
      int toIdx;
      PrintWriter writer = new PrintWriter("results.csv", StandardCharsets.UTF_8);
      for (long i = 0; i < subLists; i++) {
         fromIdx = (int) (i * subListSize);
         toIdx = (int) ((i + 1) * subListSize);
         processSubList(writer, fromIdx, toIdx, goldenIdList.records);
      }
      fromIdx = (int) (subLists * subListSize);
      toIdx = goldenRecords;
      processSubList(writer, fromIdx, toIdx, goldenIdList.records);
      writer.close();
      dataSet.forEach((k, v) -> {
         int maxGoldenRecordCount = 0;
         for (GoldenRecordMembers goldenRecordMembers : v) {
            int n = 0;
            for (String id : goldenRecordMembers.member) {
               if (k.equals(id.substring(0, 12))) {
                  n += 1;
               }
            }
            if (n > maxGoldenRecordCount) {
               maxGoldenRecordCount = n;
            }
         }
         v.forEach(gr -> {
            gr.member.forEach(m -> {
               if (m.substring(0, 12).equals(k)) {
                  falseNegatives[0] += 1;
               } else {
                  falsePositives[0] += 1;
               }
            });
         });
         falseNegatives[0] -= maxGoldenRecordCount;
         truePositives[0] += maxGoldenRecordCount;
      });
      double precision = (double) truePositives[0] / ((double) (truePositives[0] + falsePositives[0]));
      double recall = (double) truePositives[0] / ((double) (truePositives[0] + falseNegatives[0]));
      double fScore = 2 * (precision * recall) / (precision + recall);

      System.out.format(Locale.ENGLISH,
                        "%n%nGolden Records Found:%d%nTP:%d  FP:%d  FN:%d  Precision:%.5f  Recall:%.5f  F-score:%" +
                        ".5f%n",
                        dataSet.size(),
                        truePositives[0], falsePositives[0], falseNegatives[0],
                        precision, recall, fScore);
   }

   private record Count(Long count) {
   }

   private record NumberOfRecords(Long documents,
                                  Long goldenRecords) {
   }

   private record GoldenIdList(List<String> records) {
   }

   private record GoldenRecordDocuments(List<MpiExpandedGoldenRecord> goldenRecords) {
   }

   private record GoldenRecordMembers(String id,
                                      List<String> member) {
   }

}
