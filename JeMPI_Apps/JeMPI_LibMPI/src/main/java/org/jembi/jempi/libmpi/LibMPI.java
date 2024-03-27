package org.jembi.jempi.libmpi;

import io.vavr.control.Either;
import io.vavr.control.Option;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.libmpi.dgraph.LibDgraph;
import org.jembi.jempi.libmpi.lib.hooks.HooksRunner;
import org.jembi.jempi.libmpi.postgresql.LibPostgresql;
import org.jembi.jempi.shared.kafka.MyKafkaProducer;
import org.jembi.jempi.shared.models.*;
import org.jembi.jempi.shared.serdes.JsonPojoSerializer;
import org.jembi.jempi.shared.utils.AuditTrailBridge;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

public final class LibMPI {

   private static final Logger LOGGER = LogManager.getLogger(LibMPI.class);
   private final LibMPIClientInterface client;
   private final MyKafkaProducer<String, AuditEvent> topicAuditEvents;
   private final HooksRunner hooksRunner;
   private final AuditTrailBridge auditTrailUtil;

   public LibMPI(
         final Level level,
         final String[] host,
         final int[] port,
         final String kafkaBootstrapServers,
         final String kafkaClientId) {
      LOGGER.info("{}", "LibMPI Constructor");
      topicAuditEvents = new MyKafkaProducer<>(kafkaBootstrapServers,
                                               GlobalConstants.TOPIC_AUDIT_TRAIL,
                                               new StringSerializer(),
                                               new JsonPojoSerializer<>(),
                                               kafkaClientId);
      client = new LibDgraph(level, host, port);
      auditTrailUtil = new AuditTrailBridge(topicAuditEvents);
      hooksRunner = new HooksRunner(client);
   }

   public LibMPI(
         final String URL,
         final String USR,
         final String PSW,
         final String kafkaBootstrapServers,
         final String kafkaClientId) {
      LOGGER.info("{}", "LibMPI Constructor");
      topicAuditEvents = new MyKafkaProducer<>(kafkaBootstrapServers,
                                               GlobalConstants.TOPIC_AUDIT_TRAIL,
                                               new StringSerializer(),
                                               new JsonPojoSerializer<>(),
                                               kafkaClientId);
      client = new LibPostgresql(URL, USR, PSW);
      auditTrailUtil = new AuditTrailBridge(topicAuditEvents);
      hooksRunner = new HooksRunner(client);
   }

   private void sendAuditEvent(
           final String interactionID,
           final String goldenID,
           final String message,
           final float score,
           final LinkingRule linkingRule) {

      LinkingAuditEventData linkingEvent = new LinkingAuditEventData(message, interactionID, goldenID, score, linkingRule);
      auditTrailUtil.sendAuditEvent(GlobalConstants.AuditEventType.LINKING_EVENT, linkingEvent);
   }

   /*
    * *****************************************************************************
    * *
    * Database
    * *****************************************************************************
    * *
    */

   public void startTransaction() {
      client.startTransaction();
   }

   public void closeTransaction() {
      client.closeTransaction();
   }

   public Option<MpiGeneralError> dropAll() {
      return client.dropAll();
   }

   public Option<MpiGeneralError> dropAllData() {
      return client.dropAllData();
   }

   public Option<MpiGeneralError> createSchema() {
      return client.createSchema();
   }

   /*
    * *****************************************************************************
    * *
    * Queries
    * *****************************************************************************
    * *
    */

   public long countInteractions() {
      return client.countInteractions();
   }

   public long countGoldenRecords() {
      return client.countGoldenRecords();
   }


   public Interaction findInteraction(final String iid) {
      return client.findInteraction(iid);
   }

   public List<Interaction> findInteraction(final List<String> iidList) {
      return client.findInteractions(iidList);
   }

   public List<ExpandedInteraction> findExpandedInteractions(final List<String> interactionIDs) {
      return client.findExpandedInteractions(interactionIDs);
   }

   public GoldenRecord findGoldenRecord(final String goldenId) {
      return client.findGoldenRecord(goldenId);
   }

   public List<GoldenRecord> findGoldenRecords(final List<String> goldenIds) {
      return client.findGoldenRecords(goldenIds);
   }

   public ExpandedGoldenRecord findExpandedGoldenRecord(final String goldenId) {
      final var records = client.findExpandedGoldenRecords(List.of(goldenId));
      if (!records.isEmpty()) {
         return records.get(0);
      }
      return null;
   }

   public List<ExpandedGoldenRecord> findExpandedGoldenRecords(final List<String> goldenIds) {
      return client.findExpandedGoldenRecords(goldenIds);
   }

   public List<String> findGoldenIds() {
      return client.findGoldenIds();
   }

   public List<String> fetchGoldenIds(
         final long offset,
         final long length) {
      return client.fetchGoldenIds(offset, length);
   }

   public List<GoldenRecord> findLinkCandidates(final CustomDemographicData demographicData) {
      return client.findLinkCandidates(demographicData);
   }

   public List<GoldenRecord> findMatchCandidates(final CustomDemographicData demographicData) {
      return client.findMatchCandidates(demographicData);
   }

   public Either<List<GoldenRecord>, MpiGeneralError> findGoldenRecords(final ApiModels.ApiCrFindRequest request) {
      return client.findGoldenRecords(request);
   }

   public LibMPIPaginatedResultSet<ExpandedGoldenRecord> simpleSearchGoldenRecords(
         final List<ApiModels.ApiSearchParameter> params,
         final Integer offset,
         final Integer limit,
         final String sortBy,
         final Boolean sortAsc) {
      return client.simpleSearchGoldenRecords(params, offset, limit, sortBy, sortAsc);
   }

   public LibMPIPaginatedResultSet<ExpandedGoldenRecord> customSearchGoldenRecords(
         final List<ApiModels.ApiSimpleSearchRequestPayload> params,
         final Integer offset,
         final Integer limit,
         final String sortBy,
         final Boolean sortAsc) {
      return client.customSearchGoldenRecords(params, offset, limit, sortBy, sortAsc);
   }

   public LibMPIPaginatedResultSet<Interaction> simpleSearchInteractions(
         final List<ApiModels.ApiSearchParameter> params,
         final Integer offset,
         final Integer limit,
         final String sortBy,
         final Boolean sortAsc) {
      return client.simpleSearchInteractions(params, offset, limit, sortBy, sortAsc);
   }

   public LibMPIPaginatedResultSet<Interaction> customSearchInteractions(
         final List<ApiModels.ApiSimpleSearchRequestPayload> params,
         final Integer offset,
         final Integer limit,
         final String sortBy,
         final Boolean sortAsc) {
      return client.customSearchInteractions(params, offset, limit, sortBy, sortAsc);
   }

   public LibMPIPaginatedResultSet<String> filterGids(
         final List<ApiModels.ApiSearchParameter> params,
         final LocalDateTime createdAt,
         final PaginationOptions paginationOptions) {
      return client.filterGids(params, createdAt, paginationOptions);
   }

   public PaginatedGIDsWithInteractionCount filterGidsWithInteractionCount(
         final List<ApiModels.ApiSearchParameter> params,
         final LocalDateTime createdAt,
         final PaginationOptions paginationOptions) {
      return client.filterGidsWithInteractionCount(params, createdAt, paginationOptions);
   }

   /*
    * *****************************************************************************
    * *
    * Mutations
    * *****************************************************************************
    * *
    */

   public boolean setScore(
         final String interactionID,
         final String goldenID,
         final float oldScore,
         final float newScore) {
      final var result = client.setScore(interactionID, goldenID, newScore);
      if (result) {
         sendAuditEvent(interactionID, goldenID, String.format(Locale.ROOT, "score: %.5f -> %.5f", oldScore, newScore), newScore, LinkingRule.UNMATCHED);
      } else {
         sendAuditEvent(interactionID, goldenID, String.format(Locale.ROOT, "set score error: %.5f -> %.5f", oldScore, newScore), newScore, LinkingRule.UNMATCHED);

      }
      return result;
   }

   public boolean updateGoldenRecordField(
         final String goldenId,
         final String fieldName,
         final String newValue) {
      return client.updateGoldenRecordField(goldenId, fieldName, newValue);
   }


   public boolean updateGoldenRecordField(
         final String interactionId,
         final String goldenId,
         final String fieldName,
         final String oldValue,
         final String newValue) {
      final var result = client.updateGoldenRecordField(goldenId, fieldName, newValue);
      if (result) {
         sendAuditEvent(interactionId, goldenId, String.format(Locale.ROOT,
                 "%s: '%s' -> '%s'",
                 fieldName,
                 oldValue,
                 newValue),
                 -1.0F,
                 LinkingRule.UNMATCHED);
      } else {
         sendAuditEvent(interactionId, goldenId, String.format(Locale.ROOT,
                "%s: error updating '%s' -> '%s'",
                fieldName,
                oldValue,
                newValue),
                -1.0F,
                LinkingRule.UNMATCHED);
      }
      return result;
   }

   public Either<MpiGeneralError, LinkInfo> linkToNewGoldenRecord(
         final String currentGoldenId,
         final String interactionId,
         final float score) {
      final var result = client.linkToNewGoldenRecord(currentGoldenId, interactionId, score);
      if (result.isRight()) {
         sendAuditEvent(interactionId,
                        result.get().goldenUID(),
                        String.format(Locale.ROOT,
                                      "Interaction -> new GoldenID: old(%s) new(%s) [%f]",
                                      currentGoldenId,
                                      result.get().goldenUID(),
                                      score), score, LinkingRule.UNMATCHED);
      } else {
         sendAuditEvent(interactionId,
                       currentGoldenId,
                       String.format(Locale.ROOT,
                               "Interaction -> update GoldenID error: old(%s) [%f]",
                               currentGoldenId, score),
                               score,
                               LinkingRule.UNMATCHED);
      }
      return result;
   }

   public Either<MpiGeneralError, LinkInfo> updateLink(
         final String goldenID,
         final String newGoldenID,
         final String interactionID,
         final float score) {
      final var result = client.updateLink(goldenID, newGoldenID, interactionID, score);
      if (result.isRight()) {
         sendAuditEvent(interactionID,
                        newGoldenID,
                        String.format(Locale.ROOT,
                                      "Interaction -> update GoldenID: old(%s) new(%s) [%f]",
                                      goldenID,
                                      newGoldenID,
                                      score), score, LinkingRule.UNMATCHED);
      } else {
         sendAuditEvent(interactionID,
                        newGoldenID,
                        String.format(Locale.ROOT,
                                      "Interaction -> update GoldenID error: old(%s) new(%s) [%f]",
                                      goldenID,
                                      newGoldenID,
                                      score), score, LinkingRule.UNMATCHED);
      }
      return result;
   }

   public LinkInfo createInteractionAndLinkToExistingGoldenRecord(
         final Interaction interaction,
         final LibMPIClientInterface.GoldenIdScore goldenIdScore,
         final boolean deterministicValidation,
         final float probabilisticValidation,
         final LinkingRule linkingRule) {
      final var result = client.createInteractionAndLinkToExistingGoldenRecord(interaction, goldenIdScore);
      if (result != null) {
         sendAuditEvent(result.interactionUID(),
                        result.goldenUID(),
                        String.format(Locale.ROOT,
                                      "Interaction -> Existing GoldenRecord (%.5f)  /  Validation: Deterministic(%s), "
                                      + "Probabilistic(%.3f)",
                                      result.score(),
                                      deterministicValidation,
                                      probabilisticValidation), result.score(), linkingRule);
      } else {
         sendAuditEvent(interaction.interactionId(),
                        goldenIdScore.goldenId(),
                        String.format(Locale.ROOT,
                                      "Interaction -> error linking to existing GoldenRecord (%.5f)",
                                      goldenIdScore.score()), goldenIdScore.score(), linkingRule);
      }
      return result;

   }

   public LinkInfo createInteractionAndLinkToClonedGoldenRecord(
         final Interaction interaction,
         final float score) {
      final var result = client.createInteractionAndLinkToClonedGoldenRecord(interaction, score);
      if (result != null) {
         sendAuditEvent(result.interactionUID(),
                 result.goldenUID(),
                 String.format(Locale.ROOT,
                         "Interaction -> New GoldenRecord (%f)", score),
                 score, LinkingRule.UNMATCHED);
      } else {
         sendAuditEvent(interaction.interactionId(),
                 null,
                 String.format(Locale.ROOT,
                         "Interaction -> error linking to new GoldenRecord (%f)", score),
                 score, LinkingRule.UNMATCHED);
      }
      return result;
   }

   /*
    * *****************************************************************************
    * *
    * Hooks
    * *****************************************************************************
    * *
    */

    public List<MpiGeneralError> beforeLinkingHook() {
       return hooksRunner.beforeLinkingHook();
    }

   public List<MpiGeneralError> afterLinkingHook() {
      return hooksRunner.afterLinkingHook();
   }

}
