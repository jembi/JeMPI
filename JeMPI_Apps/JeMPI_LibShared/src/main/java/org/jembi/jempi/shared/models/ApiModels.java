package org.jembi.jempi.shared.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.ObjectUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public abstract class ApiModels {

   private static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss.SSSSSS";

   public interface ApiPaginatedResultSet {
   }

   public record ApiGoldenRecordCount(Long count) {
   }

   public record ApiInterationCount(Long count) {
   }

   public record ApiSearchParameter(
         String value,
         String fieldName,
         Integer distance) {
   }

   @JsonInclude(JsonInclude.Include.NON_NULL)
   public record ApiCrFindRequest(List<ApiSearchParameter> parameters) {
   }

   @JsonInclude(JsonInclude.Include.NON_NULL)
   public record ApiCrFindResponse(List<GoldenRecord> goldenRecords) {
   }

   @JsonInclude(JsonInclude.Include.NON_NULL)
   public record ApiCrRegisterRequest(Interaction interaction) {
   }

   @JsonInclude(JsonInclude.Include.NON_NULL)
   public record ApiCrRegisterResponse(String goldenId) {
   }

   @JsonInclude(JsonInclude.Include.NON_NULL)
   public record ApiCrUpdateFieldRequest(
         String goldenId,
         String field,
         String value) {
   }

   @JsonInclude(JsonInclude.Include.NON_NULL)
   public record ApiCrUpdateFieldResponse(
         String goldenId,
         String field,
         String value) {
   }


   @JsonInclude(JsonInclude.Include.NON_NULL)
   public record ApiSimpleSearchRequestPayload(
         List<ApiSearchParameter> parameters,
         Integer offset,
         Integer limit,
         String sortBy,
         Boolean sortAsc) {

      public ApiSimpleSearchRequestPayload(
            final List<ApiSearchParameter> parameters,
            final Integer offset,
            final Integer limit,
            final String sortBy,
            final Boolean sortAsc) {
         this.parameters = ObjectUtils.defaultIfNull(parameters, new ArrayList<>());
         this.offset = ObjectUtils.defaultIfNull(offset, 0);
         this.limit = ObjectUtils.defaultIfNull(limit, 10);
         this.sortBy = ObjectUtils.defaultIfNull(sortBy, "uid");
         this.sortAsc = ObjectUtils.defaultIfNull(sortAsc, false);
      }
   }

   @JsonInclude(JsonInclude.Include.NON_NULL)
   public record InteractionCount(@JsonProperty("total") Integer total) {
      static InteractionCount fromInteractionCount(final LibMPIInteractionCount interactionCount) {
         return new InteractionCount(interactionCount.total());
      }
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

   public record ApiFiteredGidsPaginatedResultSet(
         List<String> data,
         ApiPagination pagination) implements ApiPaginatedResultSet {
      public static ApiFiteredGidsPaginatedResultSet fromLibMPIPaginatedResultSet(
            final LibMPIPaginatedResultSet<String> resultSet) {
         final var data = resultSet.data()
                                   .stream().toList();
         return new ApiFiteredGidsPaginatedResultSet(data, ApiPagination.fromLibMPIPagination(resultSet.pagination()));
      }
   }

   public record ApiFiteredGidsWithInteractionCountPaginatedResultSet(
         List<String> data,
         InteractionCount interationCount,
         ApiPagination pagination
         ) implements ApiPaginatedResultSet {
      public static ApiFiteredGidsWithInteractionCountPaginatedResultSet fromPaginatedGidsWithInteractionCount(
            final PaginatedGIDsWithInteractionCount resultSet) {
         final var data = resultSet.data()
                                   .stream().toList();
         return new ApiFiteredGidsWithInteractionCountPaginatedResultSet(data, InteractionCount.fromInteractionCount(resultSet.interactionCount()), ApiPagination.fromLibMPIPagination(resultSet.pagination()));
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

   public record ApiExtendedLinkInfo(
         String stan,
         LinkInfo linkInfo,
         List<ExternalLinkCandidate> externalLinkCandidateList) {
   }

}
