package org.jembi.jempi.gui;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.shared.models.ApiModels;

import java.util.Arrays;
import java.util.List;

public final class Cache {

   private static final Logger LOGGER = LogManager.getLogger(Cache.class);
   private static final Integer GID_CACHE_BUFFER_SIZE = 100;
   private final ApiClient apiClient = new ApiClient();
   private final String[][] gidCacheBuffers = new String[3][GID_CACHE_BUFFER_SIZE];
   private Long offset = null;

   Cache() {
      final var numberOfRecords = apiClient.getNumberOfRecords();
      LOGGER.trace("{} {}", numberOfRecords.goldenRecords(), numberOfRecords.interactions());
      offset = numberOfRecords.goldenRecords() - GID_CACHE_BUFFER_SIZE;
   }

   List<ApiModels.ApiExpandedGoldenRecord> fetchExpandedGoldenRecords() {
      return apiClient.getGoldenRecordDocumentsList(Arrays.stream(gidCacheBuffers[0]).toList());
   }

   void fetchGoldenIds() {
      final var ids = apiClient.getGoldenIdList();
      LOGGER.trace("{}", ids);
      gidCacheBuffers[0] = apiClient.getFetchGoldenIdList(offset, GID_CACHE_BUFFER_SIZE).toArray(String[]::new);
      LOGGER.trace("{}", (Object) gidCacheBuffers[0]);
   }

}
