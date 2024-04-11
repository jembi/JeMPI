package org.jembi.jempi.gui;

import com.fasterxml.jackson.core.type.TypeReference;
import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.shared.models.ApiModels;
import org.jembi.jempi.shared.models.GlobalConstants;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static org.jembi.jempi.shared.utils.AppUtils.OBJECT_MAPPER;

class ApiClient {
   private static final Logger LOGGER = LogManager.getLogger(ApiClient.class);

   private static final String URL = "http://localhost:50000";
   private static final String URL_LINK = String.format(Locale.ROOT, "%s/JeMPI/", URL);

   private final OkHttpClient client = new OkHttpClient();

   ApiModels.ApiNumberOfRecords getNumberOfRecords() {
      final HttpUrl.Builder urlBuilder =
            Objects.requireNonNull(HttpUrl.parse(URL_LINK + GlobalConstants.SEGMENT_COUNT_RECORDS)).newBuilder();
      final String url = urlBuilder.build().toString();
      final Request request = new Request.Builder().url(url).build();
      final Call call = client.newCall(request);
      try (var response = call.execute()) {
         assert response.body() != null;
         var json = response.body().string();
         return OBJECT_MAPPER.readValue(json, ApiModels.ApiNumberOfRecords.class);
      } catch (IOException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
         return null;
      }
   }

   List<String> getGidsAll() {
      final HttpUrl.Builder urlBuilder =
            Objects.requireNonNull(HttpUrl.parse(URL_LINK + GlobalConstants.SEGMENT_POST_GIDS_ALL)).newBuilder();
      final String url = urlBuilder.build().toString();
      final Request request = new Request.Builder().url(url).build();
      final Call call = client.newCall(request);
      try (var response = call.execute()) {
         assert response.body() != null;
         var json = response.body().string();
         return OBJECT_MAPPER.readValue(json, GoldenIds2.class).records;
      } catch (IOException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
         return Collections.emptyList();
      }
   }

   List<String> getGidsPaged(
         final long offset,
         final long length) {
      final HttpUrl.Builder urlBuilder =
            Objects.requireNonNull(HttpUrl.parse(URL_LINK + GlobalConstants.SEGMENT_POST_GIDS_PAGED)).newBuilder();
      urlBuilder.addQueryParameter("offset", Long.toString(offset));
      urlBuilder.addQueryParameter("length", Long.toString(length));
      final String url = urlBuilder.build().toString();
      final Request request = new Request.Builder().url(url).build();
      final Call call = client.newCall(request);
      try (var response = call.execute()) {
         assert response.body() != null;
         var json = response.body().string();
         return OBJECT_MAPPER.readValue(json, GoldenIds.class).goldenIds();
      } catch (IOException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
         return Collections.emptyList();
      }
   }

   List<ApiModels.ApiExpandedGoldenRecord> getGoldenRecordsInteractions(final List<String> gids) {
      final HttpUrl.Builder urlBuilder =
            Objects.requireNonNull(HttpUrl.parse(URL_LINK + GlobalConstants.SEGMENT_POST_EXPANDED_GOLDEN_RECORDS_USING_PARAMETER_LIST))
                   .newBuilder();
      gids.forEach(id -> urlBuilder.addQueryParameter("uid", id));
      final String url = urlBuilder.build().toString();
      final Request request = new Request.Builder().url(url).build();
      final Call call = client.newCall(request);
      try (var response = call.execute()) {
         assert response.body() != null;
         var json = response.body().string();
         return OBJECT_MAPPER.readValue(json, new TypeReference<List<ApiModels.ApiExpandedGoldenRecord>>() {
         });
      } catch (IOException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
         return Collections.emptyList();
      }
   }

   private record GoldenIds(List<String> goldenIds) {
   }

   private record GoldenIds2(List<String> records) {

   }


}
