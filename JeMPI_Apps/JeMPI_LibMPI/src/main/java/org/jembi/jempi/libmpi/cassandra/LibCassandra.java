package org.jembi.jempi.libmpi.cassandra;

import io.vavr.control.Either;
import io.vavr.control.Option;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.libmpi.LibMPIClientInterface;
import org.jembi.jempi.libmpi.MpiExpandedGoldenRecord;
import org.jembi.jempi.libmpi.MpiGeneralError;
import org.jembi.jempi.libmpi.MpiServiceError;
import org.jembi.jempi.shared.models.CustomEntity;
import org.jembi.jempi.shared.models.CustomGoldenRecord;

import java.util.List;

public class LibCassandra implements LibMPIClientInterface {

   private static final Logger LOGGER = LogManager.getLogger(LibCassandra.class);


   public LibCassandra(final String[] host, final int[] port) {
      LOGGER.info("{} {} {}", "LibCassandra Constructor", host, port);
      final String[] altHosts = new String[]{"cassandra-1", "cassandra-2", "cassandra-3"};
      final int[] altPorts = new int[]{9042, 9042, 9042};
      Client.getInstance().config(altHosts, altPorts);
   }

   /*
    * *******************************************************
    *   DATABASE
    * *******************************************************
    */

   public void startTransaction() {
      Client.getInstance().startTransaction();
   }

   public void closeTransaction() {
      Client.getInstance().closeTransaction();
   }

   public Option<MpiGeneralError> dropAllData() {
      LOGGER.error("dropAllData");
      return Option.none();
   }

   public Option<MpiGeneralError> dropAll() {
      return Client.getInstance().dropAll();
   }

   public Option<MpiGeneralError> createSchema() {
      return Client.getInstance().createSchema();
   }


   /*
    * *******************************************************
    *   QUERIES
    * *******************************************************
    *
    */

   public List<CustomGoldenRecord> getCandidates(final CustomEntity customEntity,
                                                 final boolean applyDeterministicFilter) {
      return Client.getInstance().getCandidates(customEntity, applyDeterministicFilter);
   }

   public List<MpiExpandedGoldenRecord> getMpiExpandedGoldenRecordList(final List<String> goldenIdList) {
      LOGGER.error("getMpiExpandedGoldenRecordList");
      return List.of();
   }

   public List<String> getGoldenIdListByPredicate(final String predicate, final String val) {
      LOGGER.error("getGoldenIdListByPredicate");
      return List.of();
   }

   public CustomGoldenRecord getGoldenRecordByUid(final String uid) {
      LOGGER.error("getGoldenRecordByUid");
      return null;
   }

   public CustomEntity getMpiEntity(final String uid) {
      LOGGER.error("getMpiEntity");
      return null;
   }

   public List<String> getGoldenIdList() {
      LOGGER.error("getGoldenIdList");
      return List.of();
   }

   public long countGoldenRecords() {
      LOGGER.error("countGoldenRecords");
      return 0;
   }

   public long countEntities() {
      LOGGER.error("countEntities");
      return 0;
   }

   /*
    * *******************************************************
    *   MUTATIONS
    * *******************************************************
    */
   public boolean updateGoldenRecordPredicate(final String uid, final String predicate, final String val) {
      LOGGER.error("updateGoldenRecordPredicate");
      return false;
   }

   public Either<MpiGeneralError, LinkInfo> unLink(final String goldenID, final String entityID, final float score) {
      LOGGER.error("unLink");
      return Either.left(new MpiServiceError.GeneralError("Not implemented"));
   }

   public Either<MpiGeneralError, LinkInfo> updateLink(final String goldenID, final String newGoldenID,
                                                       final String entityID, final float score) {
      LOGGER.error("updateLink");
      return Either.left(new MpiServiceError.GeneralError("Not implemented"));
   }

   public LinkInfo createEntityAndLinkToExistingGoldenRecord(final CustomEntity customEntity,
                                                             final GoldenIdScore goldenIdScore) {
      LOGGER.error("createEntityAndLinkToExistingGoldenRecords");
      return null;
   }

   public LinkInfo createEntityAndLinkToClonedGoldenRecord(final CustomEntity customEntity, float score) {
      LOGGER.error("createEntityAndLinkToClonedGoldenRecord");
      return null;
   }

}
