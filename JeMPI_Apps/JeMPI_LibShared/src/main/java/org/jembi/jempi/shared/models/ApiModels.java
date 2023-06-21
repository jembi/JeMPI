package org.jembi.jempi.shared.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.text.SimpleDateFormat;
import java.util.List;

public abstract class ApiModels {

   private static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss.SSSSSS";

   public interface ApiPaginatedResultSet {
   }

   public record ApiGoldenRecordCount(Long count) {
   }

   public record ApiInterationCount(Long count) {
   }

   @JsonInclude(JsonInclude.Include.NON_NULL)
   public record ApiPagination(@JsonProperty("total") Integer total) {
      static ApiPagination fromLibMPIPagination(final LibMPIPagination pagination) {
         return new ApiPagination(pagination.total());
      }
   }

   public record ApiExpandedGoldenRecordsPaginatedResultSet(
         List<ApiExpandedGoldenRecord> data,
         ApiPagination pagination) implements ApiPaginatedResultSet {
      public static ApiExpandedGoldenRecordsPaginatedResultSet fromLibMPIPaginatedResultSet(
            final LibMPIPaginatedResultSet<ExpandedGoldenRecord> resultSet) {
         final var data = resultSet.data()
                                   .stream()
                                   .map(ApiExpandedGoldenRecord::fromExpandedGoldenRecord)
                                   .toList();
         return new ApiExpandedGoldenRecordsPaginatedResultSet(data, ApiPagination.fromLibMPIPagination(resultSet.pagination()));
      }
   }

   public record ApiInteractionsPaginatedResultSet(
         List<ApiInteraction> data,
         ApiPagination pagination) implements ApiPaginatedResultSet {
      public static ApiInteractionsPaginatedResultSet fromLibMPIPaginatedResultSet(
            final LibMPIPaginatedResultSet<Interaction> resultSet) {
         final var data = resultSet.data()
                                   .stream()
                                   .map(ApiInteraction::fromInteraction)
                                   .toList();
         return new ApiInteractionsPaginatedResultSet(data, ApiPagination.fromLibMPIPagination(resultSet.pagination()));
      }
   }

   @JsonInclude(JsonInclude.Include.NON_NULL)
   public record ApiGoldenRecord(
         String uid,
         List<CustomSourceId> sourceId,
         CustomUniqueGoldenRecordData uniqueGoldenRecordData,
         CustomDemographicData demographicData) {
      static ApiGoldenRecord fromGoldenRecord(final GoldenRecord goldenRecord) {
         return new ApiGoldenRecord(goldenRecord.goldenId(),
                                    goldenRecord.sourceId(),
                                    goldenRecord.customUniqueGoldenRecordData(),
                                    goldenRecord.demographicData());
      }
   }

   @JsonInclude(JsonInclude.Include.NON_NULL)
   public record ApiGoldenRecordWithScore(
         ApiGoldenRecord goldenRecord,
         Float score) {
      static ApiGoldenRecordWithScore fromGoldenRecordWithScore(final GoldenRecordWithScore goldenRecordWithScore) {
         return new ApiGoldenRecordWithScore(ApiGoldenRecord.fromGoldenRecord(goldenRecordWithScore.goldenRecord()),
                                             goldenRecordWithScore.score());
      }
   }

   public record ApiExpandedGoldenRecord(
         ApiGoldenRecord goldenRecord,
         List<ApiInteractionWithScore> interactionsWithScore) {
      public static ApiExpandedGoldenRecord fromExpandedGoldenRecord(final ExpandedGoldenRecord expandedGoldenRecord) {
         return new ApiExpandedGoldenRecord(ApiGoldenRecord.fromGoldenRecord(expandedGoldenRecord.goldenRecord()),
                                            expandedGoldenRecord.interactionsWithScore()
                                                                .stream()
                                                                .map(ApiInteractionWithScore::fromPatientRecordWithScore)
                                                                .toList());
      }
   }

   public record ApiExpandedInteraction(
         ApiInteraction interaction,
         List<ApiGoldenRecordWithScore> goldenRecordsWithScore) {
      public static ApiExpandedInteraction fromExpandedInteraction(final ExpandedInteraction expandedInteraction) {
         return new ApiExpandedInteraction(ApiInteraction.fromInteraction(expandedInteraction.interaction()),
                                           expandedInteraction.goldenRecordsWithScore()
                                                              .stream()
                                                              .map(ApiGoldenRecordWithScore::fromGoldenRecordWithScore)
                                                              .toList());
      }
   }

   @JsonInclude(JsonInclude.Include.NON_NULL)
   public record ApiInteraction(
         String uid,
         CustomSourceId sourceId,
         CustomUniqueInteractionData uniqueInteractionData,
         CustomDemographicData demographicData) {
      public static ApiInteraction fromInteraction(final Interaction interaction) {
         return new ApiInteraction(interaction.interactionId(),
                                   interaction.sourceId(),
                                   interaction.uniqueInteractionData(),
                                   interaction.demographicData());
      }
   }

   @JsonInclude(JsonInclude.Include.NON_NULL)
   public record ApiInteractionWithScore(
         ApiInteraction interaction,
         Float score) {
      static ApiInteractionWithScore fromPatientRecordWithScore(final InteractionWithScore interactionWithScore) {
         return new ApiInteractionWithScore(ApiInteraction.fromInteraction(interactionWithScore.interaction()),
                                            interactionWithScore.score());
      }
   }

   public record ApiNumberOfRecords(
         Long goldenRecords,
         Long interactions) {
   }

   @JsonInclude(JsonInclude.Include.NON_NULL)
   public record ApiAuditTrail(
         List<AuditEntry> entries) {
      public static ApiAuditTrail fromAuditTrail(final List<AuditEvent> trail) {
         final var apiDateFormat = new SimpleDateFormat(DATE_PATTERN);
         return new ApiAuditTrail(trail.stream().map(x -> new AuditEntry(apiDateFormat.format(x.insertedAt()),
                                                                         apiDateFormat.format(x.createdAt()),
                                                                         x.interactionID(),
                                                                         x.goldenID(),
                                                                         x.event()))
                                       .toList());
      }

      @JsonInclude(JsonInclude.Include.NON_NULL)
      public record AuditEntry(
            @JsonProperty("inserted_at") String insertedAt,
            @JsonProperty("created_at") String createdAt,
            @JsonProperty("interaction_id") String interactionId,
            @JsonProperty("golden_id") String goldenId,
            @JsonProperty("entry") String entry) {
      }
   }

   public record ApiCalculateScoresRequest(
         String interactionId,
         List<String> goldenIds) {
   }

   public record ApiCalculateScoresResponse(
         String interactionId,
         List<ApiScore> scores) {

      public record ApiScore(
            String goldenId,
            float score) {
      }

   }

}
