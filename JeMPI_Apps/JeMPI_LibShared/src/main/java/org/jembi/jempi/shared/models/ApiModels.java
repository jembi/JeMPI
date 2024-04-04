package org.jembi.jempi.shared.models;

import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCode;
import akka.http.javadsl.model.StatusCodes;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

import static org.jembi.jempi.shared.utils.AppUtils.OBJECT_MAPPER;

public abstract class ApiModels {

   private static final Logger LOGGER = LogManager.getLogger(ApiModels.class);
   private static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss.SSSSSS";

   public static HttpResponse getHttpErrorResponse(final StatusCode statusCode) {
      try {
         var entity = OBJECT_MAPPER.writeValueAsBytes(new ApiError());
         return HttpResponse.create().withStatus(statusCode).withEntity(entity);
      } catch (JsonProcessingException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
         return HttpResponse.create().withStatus(StatusCodes.INTERNAL_SERVER_ERROR);
      }
   }

   public interface ApiPaginatedResultSet {
   }

   public record ApiError(

         @JsonProperty("module") String module,
         @JsonProperty("class") String klass,
         @JsonProperty("line_number") Integer lineNumber) {

      public ApiError() {
         this(Thread.currentThread().getStackTrace()[3].getModuleName(),
              Thread.currentThread().getStackTrace()[3].getClassName(),
              Thread.currentThread().getStackTrace()[3].getLineNumber());
      }

   }

   public record ApiGoldenRecordCount(Long count) {
   }

   public record ApiInteractionCount(Long count) {
   }

   public record ApiSearchParameter(
         String value,
         String fieldName,
         Integer distance) {
   }

   @JsonInclude(JsonInclude.Include.NON_NULL)
   public record ApiCrCandidatesRequest(
         Float candidateThreshold,
         CustomDemographicData.CustomDemographicDataAPI demographicData) {
   }

   @JsonInclude(JsonInclude.Include.NON_NULL)
   public record ApiCrCandidatesResponse(List<ApiGoldenRecord> goldenRecords) {
   }

   @JsonInclude(JsonInclude.Include.NON_NULL)
   public record ApiCrFindRequest(
         ApiOperand operand,
         List<ApiLogicalOperand> operands) {
      @JsonInclude(JsonInclude.Include.NON_NULL)
      public record ApiOperand(
            String fn,
            Long distance,
            String name,
            String value) {
      }

      public record ApiLogicalOperand(
            String operator,
            ApiOperand operand) {
      }

   }

   @JsonInclude(JsonInclude.Include.NON_NULL)
   public record ApiCrFindResponse(List<ApiGoldenRecord> goldenRecords) {
   }


   @JsonInclude(JsonInclude.Include.NON_NULL)
   public record ApiCrRegisterRequest(
         Float candidateThreshold,
         CustomSourceId sourceId,
         CustomUniqueInteractionData uniqueInteractionData,
         CustomDemographicData.CustomDemographicDataAPI demographicData) {
   }

   @JsonInclude(JsonInclude.Include.NON_NULL)
   public record ApiCrLinkToGidUpdateRequest(
         String gid,
         CustomSourceId sourceId,
         CustomUniqueInteractionData uniqueInteractionData,
         CustomDemographicData.CustomDemographicDataAPI demographicData) {
   }

   @JsonInclude(JsonInclude.Include.NON_NULL)
   public record ApiCrLinkBySourceIdRequest(
         CustomSourceId sourceId,
         CustomUniqueInteractionData uniqueInteractionData,
         CustomDemographicData.CustomDemographicDataAPI demographicData) {
   }

   @JsonInclude(JsonInclude.Include.NON_NULL)
   public record ApiCrLinkBySourceIdUpdateRequest(
         CustomSourceId sourceId,
         CustomUniqueInteractionData uniqueInteractionData,
         CustomDemographicData.CustomDemographicDataAPI demographicData) {
   }

   @JsonInclude(JsonInclude.Include.NON_NULL)
   public record LinkInteractionSyncBody(
         String stan,
         ExternalLinkRange externalLinkRange,
         Float matchThreshold,
         CustomSourceId sourceId,
         CustomUniqueInteractionData uniqueInteractionData,
         CustomDemographicData.CustomDemographicDataAPI demographicData) {
   }

   @JsonInclude(JsonInclude.Include.NON_NULL)
   public record LinkInteractionToGidSyncBody(
         String stan,
         CustomSourceId sourceId,
         CustomUniqueInteractionData uniqueInteractionData,
         CustomDemographicData.CustomDemographicDataAPI demographicData,
         String gid) {
   }


   @JsonInclude(JsonInclude.Include.NON_NULL)
   public record ApiCrRegisterResponse(LinkInfo linkInfo) {
   }

   @JsonInclude(JsonInclude.Include.NON_NULL)
   public record ApiCrLinkUpdateResponse(LinkInfo linkInfo) {
   }

   @JsonInclude(JsonInclude.Include.NON_NULL)
   public record ApiCrUpdateFieldsRequest(
         String goldenId,
         List<ApiCrUpdateField> fields) {

      public record ApiCrUpdateField(
            String name,
            String value) {
      }

   }

   @JsonInclude(JsonInclude.Include.NON_NULL)
   public record ApiCrUpdateFieldsResponse(
         String goldenId,
         List<String> updated,
         List<String> failed) {
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
         final var data = resultSet.data().stream().map(ApiExpandedGoldenRecord::fromExpandedGoldenRecord).toList();
         return new ApiExpandedGoldenRecordsPaginatedResultSet(data, ApiPagination.fromLibMPIPagination(resultSet.pagination()));
      }
   }

   public record ApiInteractionsPaginatedResultSet(
         List<ApiInteraction> data,
         ApiPagination pagination) implements ApiPaginatedResultSet {
      public static ApiInteractionsPaginatedResultSet fromLibMPIPaginatedResultSet(
            final LibMPIPaginatedResultSet<Interaction> resultSet) {
         final var data = resultSet.data().stream().map(ApiInteraction::fromInteraction).toList();
         return new ApiInteractionsPaginatedResultSet(data, ApiPagination.fromLibMPIPagination(resultSet.pagination()));
      }
   }

   public record ApiFilteredGidsPaginatedResultSet(
         List<String> data,
         ApiPagination pagination) implements ApiPaginatedResultSet {
      public static ApiFilteredGidsPaginatedResultSet fromLibMPIPaginatedResultSet(
            final LibMPIPaginatedResultSet<String> resultSet) {
         final var data = resultSet.data().stream().toList();
         return new ApiFilteredGidsPaginatedResultSet(data, ApiPagination.fromLibMPIPagination(resultSet.pagination()));
      }
   }

   public record ApiFilteredGidsWithInteractionCountPaginatedResultSet(
         List<String> data,
         InteractionCount interactionCount,
         ApiPagination pagination) implements ApiPaginatedResultSet {
      public static ApiFilteredGidsWithInteractionCountPaginatedResultSet fromPaginatedGidsWithInteractionCount(
            final PaginatedGIDsWithInteractionCount resultSet) {
         final var data = resultSet.data().stream().toList();
         return new ApiFilteredGidsWithInteractionCountPaginatedResultSet(data,
                                                                          InteractionCount.fromInteractionCount(resultSet.interactionCount()),
                                                                          ApiPagination.fromLibMPIPagination(resultSet.pagination()));
      }
   }

   @JsonInclude(JsonInclude.Include.NON_NULL)
   public record ApiGoldenRecord(
         String uid,
         List<CustomSourceId> sourceId,
         CustomUniqueGoldenRecordData uniqueGoldenRecordData,
         CustomDemographicData.CustomDemographicDataAPI demographicData) {
      public static ApiGoldenRecord fromGoldenRecord(final GoldenRecord goldenRecord) {
         return new ApiGoldenRecord(goldenRecord.goldenId(),
                                    goldenRecord.sourceId(),
                                    goldenRecord.customUniqueGoldenRecordData(),
                                    DemographicData.fromDemographicData(goldenRecord.demographicData()));
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
         CustomDemographicData.CustomDemographicDataAPI demographicData) {
      public static ApiInteraction fromInteraction(final Interaction interaction) {
         return new ApiInteraction(interaction.interactionId(),
                                   interaction.sourceId(),
                                   interaction.uniqueInteractionData(),
                                   DemographicData.fromDemographicData(interaction.demographicData()));
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
         List<LinkingAuditEntry> entries) {

      @JsonInclude(JsonInclude.Include.NON_NULL)
      public record LinkingAuditEntry(
            @JsonProperty("inserted_at") String insertedAt,
            @JsonProperty("created_at") String createdAt,
            @JsonProperty("interaction_id") String interactionId,
            @JsonProperty("golden_id") String goldenId,
            @JsonProperty("entry") String entry,
            @JsonProperty("score") Float score,
            @JsonProperty("linking_rule") String linkingRule) {
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
