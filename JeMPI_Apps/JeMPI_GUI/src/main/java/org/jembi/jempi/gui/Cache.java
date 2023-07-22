package org.jembi.jempi.gui;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.shared.models.ApiModels;
import org.jembi.jempi.shared.models.CustomDemographicData;

import java.util.ArrayList;
import java.util.List;

public final class Cache {

   private static final Logger LOGGER = LogManager.getLogger(Cache.class);
   private static final Object LOCK = new Object();
   private static final ApiClient API_CLIENT = new ApiClient();
   private static final ArrayList<String> GID_BUFFER = new ArrayList<>();
   private static int totalFetched = 0;
   private static ApiModels.ApiNumberOfRecords apiNumberOfRecords = null;
   private static GoldenRecordBuffer goldenRecordBuffer = null;

   private Cache() {
   }

   private static void init() {
      if (goldenRecordBuffer == null) {
         apiNumberOfRecords = API_CLIENT.getNumberOfRecords();
         LOGGER.trace("{}", API_CLIENT.getGidsAll());
         GID_BUFFER.addAll(API_CLIENT.getGidsPaged(0, apiNumberOfRecords.goldenRecords()));
         LOGGER.trace("{} -- {}", GID_BUFFER.size(), GID_BUFFER);
         goldenRecordBuffer = new GoldenRecordBuffer();
      }
   }

   static long getNumberRows() {
      init();
      return apiNumberOfRecords.goldenRecords() + apiNumberOfRecords.interactions();
   }

   static String[] get(final int i) {
      init();
      return goldenRecordBuffer.getRow(i);
   }

   private static final class GoldenRecordBuffer {
      private static final Integer BUFFER_SIZE = 100;
      private static final Integer BUFFER_FILL_SIZE = 25;
      final BufferItem[] buffer;
      int base = 0;

      GoldenRecordBuffer() {
         buffer = new BufferItem[BUFFER_SIZE];
         bufferFill();
      }

      private static String[] getGoldenRecordVector(final ApiModels.ApiExpandedGoldenRecord expandedGoldenRecord) {
         final var demographicFields = CustomDemographicData.class.getDeclaredFields();
         final String[] vector = new String[3 + demographicFields.length + 1];
         vector[0] = expandedGoldenRecord.goldenRecord().uniqueGoldenRecordData().auxId();
         vector[1] = expandedGoldenRecord.goldenRecord().uid();
         vector[2] = expandedGoldenRecord.goldenRecord().uniqueGoldenRecordData().auxDateCreated().toString();
         for (int i = 0; i < demographicFields.length; i++) {
            try {
               vector[3 + i] = demographicFields[i].get(expandedGoldenRecord.goldenRecord().demographicData()).toString();
            } catch (IllegalAccessException e) {
               LOGGER.error(e.getLocalizedMessage(), e);
            }
         }
         vector[3 + demographicFields.length] = null;
         return vector;
      }

      private static String[] getInteractionVector(final ApiModels.ApiInteractionWithScore interactionWithScore) {
         final var demographicFields = CustomDemographicData.class.getDeclaredFields();
         final String[] vector = new String[3 + demographicFields.length + 1];
         vector[0] = interactionWithScore.interaction().uniqueInteractionData().auxId();
         vector[1] = interactionWithScore.interaction().uid();
         vector[2] = interactionWithScore.interaction().uniqueInteractionData().auxDateCreated().toString();
         for (int i = 0; i < demographicFields.length; i++) {
            try {
               vector[3 + i] = demographicFields[i].get(interactionWithScore.interaction().demographicData()).toString();
            } catch (IllegalAccessException e) {
               LOGGER.error(e.getLocalizedMessage(), e);
            }
         }
         vector[3 + demographicFields.length] = Float.toString(interactionWithScore.score());
         return vector;
      }

      private void bufferFillPrev() {
         final var fillSize = buffer[base].gidIndex >= BUFFER_FILL_SIZE
               ? BUFFER_FILL_SIZE
               : buffer[base].gidIndex;
         if (fillSize == 0) {
            return;
         }
         final int[] fromGidIndex = {buffer[base].gidIndex - fillSize};
         final var subList = GID_BUFFER.subList(fromGidIndex[0], fromGidIndex[0] + fillSize);
         final var expandedGoldenRecords = API_CLIENT.getGoldenRecordsInteractions(subList);
         for (int i = expandedGoldenRecords.size() - 1; i >= 0; i--) {
            final var expandedGoldenRecord = expandedGoldenRecords.get(i);
            final var rowData = new ArrayList<String[]>();
            rowData.add(getGoldenRecordVector(expandedGoldenRecord));
            expandedGoldenRecord.interactionsWithScore()
                                .forEach(interactionWithScore -> rowData.add(getInteractionVector(interactionWithScore)));
            base = (base + (BUFFER_SIZE - 1)) % BUFFER_SIZE;
            buffer[base] = new BufferItem(fromGidIndex[0] + i,
                                          buffer[(base + 1) % BUFFER_SIZE].rowNumber - rowData.size(),
                                          rowData);
         }
         synchronized (LOCK) {
            Cache.totalFetched -= fillSize;
            LOGGER.trace("Fetched: {}", totalFetched);
         }
      }

      private void bufferFillNext() {
         final int[] prevRowIndex = {(base + (BUFFER_SIZE - 1)) % BUFFER_SIZE};
         final int[] fromGidIndex = {buffer[prevRowIndex[0]].gidIndex + 1};
         final var fillSize = Math.min(BUFFER_FILL_SIZE, GID_BUFFER.size() - fromGidIndex[0]);
         final var subList = GID_BUFFER.subList(fromGidIndex[0], fromGidIndex[0] + fillSize);
         final var expandedGoldenRecords = API_CLIENT.getGoldenRecordsInteractions(subList);
         expandedGoldenRecords.forEach(expandedGoldenRecord -> {
            final var rowData = new ArrayList<String[]>();
            rowData.add(getGoldenRecordVector(expandedGoldenRecord));
            expandedGoldenRecord.interactionsWithScore()
                                .forEach(interactionWithScore -> rowData.add(getInteractionVector(interactionWithScore)));
            buffer[base] = new BufferItem(fromGidIndex[0],
                                          buffer[prevRowIndex[0]].rowNumber + buffer[prevRowIndex[0]].numberRows,
                                          rowData);
            base = (base + 1) % BUFFER_SIZE;
            prevRowIndex[0] = (prevRowIndex[0] + 1) % BUFFER_SIZE;
            fromGidIndex[0]++;
         });
         synchronized (LOCK) {
            Cache.totalFetched += fillSize;
            LOGGER.trace("Fetched: {}", totalFetched);
         }
      }

      private void bufferFill() {
         int rowNumber = 0;
         for (int i = 0; i < BUFFER_SIZE; i++) {
            int startRow = rowNumber;
            final var expandedGoldenRecord =
                  API_CLIENT.getGoldenRecordsInteractions(List.of(GID_BUFFER.get(i))).get(0);
            final ArrayList<String[]> rowData = new ArrayList<>();
            rowData.add(getGoldenRecordVector(expandedGoldenRecord));
            expandedGoldenRecord
                  .interactionsWithScore()
                  .forEach(interactionWithScore -> rowData.add(getInteractionVector(interactionWithScore)));
            rowNumber += (1 + expandedGoldenRecord.interactionsWithScore().size());
            buffer[i] = new BufferItem(i, startRow, rowData);
         }
         synchronized (LOCK) {
            Cache.totalFetched += BUFFER_SIZE;
            LOGGER.trace("Fetched: {}", totalFetched);
         }
      }

      String[] getRow(final int row) {
         if (row < buffer[base].rowNumber) {
            bufferFillPrev();
            return getRow(row);
         } else {
            int k = base;
            while (buffer[k].rowNumber + buffer[k].numberRows <= row) {
               k = (k + 1) % BUFFER_SIZE;
               if (k == base) {
                  break;
               }
            }
            if (k == base && buffer[k].rowNumber + buffer[k].numberRows < row) {
               bufferFillNext();
               return getRow(row);
            } else {
               return buffer[k].rowData.get(row - buffer[k].rowNumber);
            }
         }
      }

      private static class BufferItem {
         int gidIndex;
         int rowNumber;
         int numberRows;
         ArrayList<String[]> rowData;

         BufferItem(
               final int gidIdx,
               final int row,
               final ArrayList<String[]> data) {
            gidIndex = gidIdx;
            rowNumber = row;
            numberRows = data.size();
            rowData = data;
         }
      }

   }

}
