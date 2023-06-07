package org.jembi.jempi.libapi;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.shared.models.*;

import java.text.SimpleDateFormat;
import java.util.List;

public abstract class ApiModels {

   private static final Logger LOGGER = LogManager.getLogger(ApiModels.class);
   private static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss.SSSSSS";

   public interface ApiPaginatedResultSet {
   }

   public record ApiGoldenRecordCount(Long count) {
   }

   public record ApiPatientCount(Long count) {
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

   public record ApiPatientRecordsPaginatedResultSet(
         List<ApiPatientRecord> data,
         ApiPagination pagination) implements ApiPaginatedResultSet {
      public static ApiPatientRecordsPaginatedResultSet fromLibMPIPaginatedResultSet(
            final LibMPIPaginatedResultSet<Interaction> resultSet) {
         final var data = resultSet.data()
                                   .stream()
                                   .map(ApiPatientRecord::fromPatientRecord)
                                   .toList();
         return new ApiPatientRecordsPaginatedResultSet(data, ApiPagination.fromLibMPIPagination(resultSet.pagination()));
      }
   }

   @JsonInclude(JsonInclude.Include.NON_NULL)
   public record ApiGoldenRecord(
         String uid,
         List<SourceId> sourceId,
         CustomDemographicData demographicData) {
      static ApiGoldenRecord fromGoldenRecord(final GoldenRecord goldenRecord) {
         return new ApiGoldenRecord(goldenRecord.goldenId(), goldenRecord.sourceId(), goldenRecord.demographicData());
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
         List<ApiPatientRecordWithScore> mpiPatientRecords) {
      public static ApiExpandedGoldenRecord fromExpandedGoldenRecord(final ExpandedGoldenRecord expandedGoldenRecord) {
         return new ApiExpandedGoldenRecord(ApiGoldenRecord.fromGoldenRecord(expandedGoldenRecord.goldenRecord()),
                                            expandedGoldenRecord.interactionsWithScore()
                                                                .stream()
                                                                .map(ApiPatientRecordWithScore::fromPatientRecordWithScore)
                                                                .toList());
      }
   }

   public record ApiExpandedPatientRecord(
         ApiPatientRecord patientRecord,
         List<ApiGoldenRecordWithScore> goldenRecordsWithScore) {
      public static ApiExpandedPatientRecord fromExpandedPatientRecord(final ExpandedInteraction expandedInteraction) {
         return new ApiExpandedPatientRecord(ApiPatientRecord.fromPatientRecord(expandedInteraction.interaction()),
                                             expandedInteraction.goldenRecordsWithScore()
                                                                .stream()
                                                                .map(ApiGoldenRecordWithScore::fromGoldenRecordWithScore)
                                                                .toList());
      }
   }

   @JsonInclude(JsonInclude.Include.NON_NULL)
   public record ApiPatientRecord(
         String uid,
         SourceId sourceId,
         CustomDemographicData demographicData) {
      public static ApiPatientRecord fromPatientRecord(final Interaction interaction) {
         return new ApiPatientRecord(interaction.interactionId(), interaction.sourceId(), interaction.demographicData());
      }
   }

   @JsonInclude(JsonInclude.Include.NON_NULL)
   public record ApiPatientRecordWithScore(
         ApiPatientRecord patientRecord,
         Float score) {
      static ApiPatientRecordWithScore fromPatientRecordWithScore(final InteractionWithScore interactionWithScore) {
         return new ApiPatientRecordWithScore(ApiPatientRecord.fromPatientRecord(interactionWithScore.interaction()),
                                              interactionWithScore.score());
      }
   }

   public record ApiNumberOfRecords(
         Long goldenRecords,
         Long patientRecords) {
   }

   @JsonInclude(JsonInclude.Include.NON_NULL)
   public record ApiAuditTrail(
         List<AuditEntry> entries) {
      static ApiAuditTrail fromAuditTrail(final List<AuditEvent> trail) {
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
}
