package org.jembi.jempi.stats;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.shared.models.CustomDemographicData;
import org.jembi.jempi.shared.models.GlobalConstants;
import org.jembi.jempi.shared.models.SourceId;

import java.io.IOException;
import java.util.*;

import static java.lang.Math.min;
import static org.jembi.jempi.shared.utils.AppUtils.OBJECT_MAPPER;
import static org.jembi.jempi.shared.utils.AppUtils.isNullOrEmpty;

public final class StatsTask {
   // 01234567890123456
   // rec-00000000-bbb-7
   // rec-0000000001-00

   private static final Logger LOGGER = LogManager.getLogger(StatsTask.class);
   private static final String URL = "http://api:50000";
   private static final String URL_LINK = String.format("%s/JeMPI/", URL);
   // 01234567890123456
   // rec-0000000001-00
   private static final int AUX_ID_SIGNIFICANT_CHARACTERS = 12;
   private final OkHttpClient client = new OkHttpClient();
   private final Map<String, List<GoldenRecordMembers>> dataSet = new HashMap<>();
   private final int[] truePositives = {0};
   private final int[] falsePositives = {0};
   private final int[] falseNegatives = {0};

   private Long getCount(final String field) throws IOException {
      final HttpUrl.Builder urlBuilder =
            Objects.requireNonNull(HttpUrl.parse(URL_LINK + field)).newBuilder();
      final String url = urlBuilder.build().toString();
      final Request request = new Request.Builder().url(url).build();
      final Call call = client.newCall(request);
      try (var response = call.execute()) {
         assert response.body() != null;
         var json = response.body().string();
         return OBJECT_MAPPER.readValue(json, Count.class).count;
      }
   }

   private NumberOfRecords getNumberOfRecords() throws IOException {
      final HttpUrl.Builder urlBuilder =
            Objects.requireNonNull(HttpUrl.parse(URL_LINK + GlobalConstants.SEGMENT_COUNT_RECORDS)).newBuilder();
      final String url = urlBuilder.build().toString();
      final Request request = new Request.Builder().url(url).build();
      final Call call = client.newCall(request);
      try (var response = call.execute()) {
         assert response.body() != null;
         var json = response.body().string();
         return OBJECT_MAPPER.readValue(json, NumberOfRecords.class);
      }
   }

   private GoldenIdList getGoldenIdList() throws IOException {
      final HttpUrl.Builder urlBuilder =
            Objects.requireNonNull(HttpUrl.parse(URL_LINK + GlobalConstants.SEGMENT_GOLDEN_IDS)).newBuilder();
      final String url = urlBuilder.build().toString();
      final Request request = new Request.Builder().url(url).build();
      final Call call = client.newCall(request);
      try (var response = call.execute()) {
         assert response.body() != null;
         var json = response.body().string();
         return OBJECT_MAPPER.readValue(json, GoldenIdList.class);
      }
   }

   private List<ApiExpandedGoldenRecord> getGoldenRecordDocumentsList(final List<String> ids) throws IOException {
      final HttpUrl.Builder urlBuilder =
            Objects.requireNonNull(HttpUrl.parse(URL_LINK + GlobalConstants.SEGMENT_GET_GOLDEN_ID_DOCUMENTS)).newBuilder();
      ids.forEach(id -> urlBuilder.addQueryParameter("uid", id));
      final String url = urlBuilder.build().toString();
      final Request request = new Request.Builder().url(url).build();
      final Call call = client.newCall(request);
      try (var response = call.execute()) {
         assert response.body() != null;
         var json = response.body().string();
         return OBJECT_MAPPER.readValue(json, new TypeReference<List<ApiExpandedGoldenRecord>>() {
         });
      }
   }

   private void updateStatsDataSet(final ApiExpandedGoldenRecord goldenRecord) {
      final String goldenRecordAuxId = goldenRecord.goldenRecord.demographicData.auxId;
      final String goldenRecordNumber = goldenRecordAuxId.substring(0, AUX_ID_SIGNIFICANT_CHARACTERS);

      final var entry = dataSet.get(goldenRecordNumber);
      final List<String> list = new ArrayList<>();
      goldenRecord.mpiPatientRecords.forEach(mpiPatientRecord -> list.add(mpiPatientRecord.patientRecord.demographicData.auxId));
      if (isNullOrEmpty(entry)) {
         final List<GoldenRecordMembers> membersList = new ArrayList<>();
         membersList.add(new GoldenRecordMembers(goldenRecordAuxId, list));
         dataSet.put(goldenRecordNumber, membersList);
      } else {
         entry.add(new GoldenRecordMembers(goldenRecordAuxId, list));
      }
   }

   private void processSubList(
         final int fromIdx,
         final int toIdx,
         final List<String> ids) throws IOException {
      var subList = ids.subList(fromIdx, toIdx);
      var goldenRecordDocuments = getGoldenRecordDocumentsList(subList);
      goldenRecordDocuments.forEach(this::updateStatsDataSet);
   }

   public StatsResults run() {
      try {
         var documentCount = getCount(GlobalConstants.SEGMENT_COUNT_PATIENT_RECORDS);
         var goldenRecordCount = getCount(GlobalConstants.SEGMENT_COUNT_GOLDEN_RECORDS);
         var numberOfRecords = getNumberOfRecords();
         var goldenIdList = getGoldenIdList();
         LOGGER.info("Document Count:       {}", documentCount);
         LOGGER.info("Golden Record Count:  {}", goldenRecordCount);
         LOGGER.info("Number of Records:    {},{}", numberOfRecords.patientRecords, numberOfRecords.goldenRecords);
         LOGGER.info("Number if id's:       {}", goldenIdList.records.size());
         final var goldenRecords = goldenIdList.records.size();
         final var subListSize = 20L;
         final var subLists = goldenRecords / min(subListSize, goldenRecords);
         final var finalSubListSize = goldenRecords % subListSize;
         LOGGER.info("Golden Records:       {}", goldenRecords);
         LOGGER.info("Sub List Size:        {}", subListSize);
         LOGGER.info("Sub Lists:            {}", subLists);
         LOGGER.info("Final Sub List Size:  {}", finalSubListSize);
         int fromIdx;
         int toIdx;
         for (long i = 0; i < subLists; i++) {
            fromIdx = (int) (i * subListSize);
            toIdx = (int) ((i + 1) * subListSize);
            processSubList(fromIdx, toIdx, goldenIdList.records);
         }
         fromIdx = (int) (subLists * subListSize);
         toIdx = goldenRecords;
         processSubList(fromIdx, toIdx, goldenIdList.records);
         dataSet.forEach((k, v) -> {
            int maxGoldenRecordCount = 0;
            for (GoldenRecordMembers goldenRecordMembers : v) {
               int n = 0;
               for (String id : goldenRecordMembers.member) {
                  if (k.equals(id.substring(0, AUX_ID_SIGNIFICANT_CHARACTERS))) {
                     n += 1;
                  }
               }
               if (n > maxGoldenRecordCount) {
                  maxGoldenRecordCount = n;
               }
            }
            v.forEach(gr -> gr.member.forEach(m -> {
               if (m.substring(0, AUX_ID_SIGNIFICANT_CHARACTERS).equals(k)) {
                  falseNegatives[0] += 1;
               } else {
                  falsePositives[0] += 1;
               }
            }));
            falseNegatives[0] -= maxGoldenRecordCount;
            truePositives[0] += maxGoldenRecordCount;
         });
         double precision = (double) truePositives[0] / ((double) (truePositives[0] + falsePositives[0]));
         double recall = (double) truePositives[0] / ((double) (truePositives[0] + falseNegatives[0]));
         double fScore = 2 * (precision * recall) / (precision + recall);

         LOGGER.info("Golden Records Found: {}", dataSet.size());
         LOGGER.info("TP:{}  FP:{}  FN:{}  Precision:{}  Recall:{}  F-score:{}",
                     truePositives[0], falsePositives[0], falseNegatives[0],
                     precision, recall, fScore);
         return new StatsResults(
               documentCount,
               goldenRecords,
               truePositives[0],
               falsePositives[0],
               falseNegatives[0],
               precision,
               recall,
               fScore);

      } catch (IOException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
      }
      return new StatsResults(null, null, null, null, null, null, null, null);
   }

   private record Count(Long count) {
   }

   private record NumberOfRecords(
         Long patientRecords,
         Long goldenRecords) {
   }

   private record GoldenIdList(List<String> records) {
   }

   private record ApiGoldenRecord(
         String uid,
         List<SourceId> sourceId,
         CustomDemographicData demographicData) {
   }

   @JsonInclude(JsonInclude.Include.NON_NULL)
   public record ApiPatientRecord(
         String uid,
         SourceId sourceId,
         CustomDemographicData demographicData) {
   }

   @JsonInclude(JsonInclude.Include.NON_NULL)
   private record ApiPatientRecordWithScore(
         ApiPatientRecord patientRecord,
         Float score) {
   }

   private record ApiExpandedGoldenRecord(
         ApiGoldenRecord goldenRecord,
         List<ApiPatientRecordWithScore> mpiPatientRecords) {
   }

   private record GoldenRecordDocuments(List<ApiExpandedGoldenRecord> expandedGoldenRecords) {
   }

   private record GoldenRecordMembers(
         String id,
         List<String> member) {
   }

   public record StatsResults(
         Long interactions,
         Integer goldenRecords,
         Integer truePositives,
         Integer falsePositives,
         Integer falseNegatives,
         Double precision,
         Double recall,
         Double fScore) {
   }


}
