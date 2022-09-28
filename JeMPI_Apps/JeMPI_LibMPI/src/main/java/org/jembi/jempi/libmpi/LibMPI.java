package org.jembi.jempi.libmpi;

import io.vavr.control.Either;
import io.vavr.control.Option;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.libmpi.dgraph.LibDgraph;
import org.jembi.jempi.shared.models.CustomEntity;
import org.jembi.jempi.shared.models.CustomGoldenRecord;

import java.util.List;

public class LibMPI {

   private static final Logger LOGGER = LogManager.getLogger(LibMPI.class);
   private final LibMPIClientInterface client;

   public LibMPI(final String[] host, final int[] port) {
      LOGGER.info("{}", "LibMPI Constructor");
      client = new LibDgraph(host, port);
//      client = new LibCassandra(host, port);
   }

   /*
    * ******************************************************************************
    *  Database
    * ******************************************************************************
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
    * ******************************************************************************
    *  Queries
    * ******************************************************************************
    */

   public List<CustomGoldenRecord> getCandidates(final CustomEntity customEntity, final boolean applyDeterministicFilter) {
      return client.getCandidates(customEntity, applyDeterministicFilter);
   }

   public List<MpiExpandedGoldenRecord> getMpiExpandedGoldenRecordList(final List<String> goldenIdList) {
      return client.getMpiExpandedGoldenRecordList(goldenIdList);
   }

   public List<String> getGoldenIdListByPredicate(final String predicate, final String val) {
      return client.getGoldenIdListByPredicate(predicate, val);
   }

   public CustomGoldenRecord getGoldenRecord(final String uid) {
      return client.getGoldenRecordByUid(uid);
   }

   public long countGoldenRecords() {
      return client.countGoldenRecords();
   }

   public List<String> getGoldenIdList() {
      return client.getGoldenIdList();
   }

   public CustomEntity getMpiEntity(final String uid) {
      return client.getMpiEntity(uid);
   }

   public long countEntities() {
      return client.countEntities();
   }

   /*
    * ******************************************************************************
    *  Mutations
    * ******************************************************************************
    */

   public boolean updateGoldenRecordPredicate(final String goldenID, final String predicate, final String value) {
      return client.updateGoldenRecordPredicate(goldenID, predicate, value);
   }

   public Either<MpiGeneralError, LibMPIClientInterface.LinkInfo> unLink(final String goldenID, final
   String entityID,
                                                                         final float score) {
      return client.unLink(goldenID, entityID, score);
   }

   public Either<MpiGeneralError, LibMPIClientInterface.LinkInfo> updateLink(final String goldenID, final String newGoldenID,
                                                                             final String entityID, final float score) {
      return client.updateLink(goldenID, newGoldenID, entityID, score);
   }

   public LibMPIClientInterface.LinkInfo createEntityAndLinkToExistingGoldenRecord(
         final CustomEntity mpiEntity,
         final LibMPIClientInterface.GoldenIdScore goldenIdScore) {
      return client.createEntityAndLinkToExistingGoldenRecord(mpiEntity, goldenIdScore);
   }


   public LibMPIClientInterface.LinkInfo createEntityAndLinkToClonedGoldenRecord(final CustomEntity customEntity, float score) {
      return client.createEntityAndLinkToClonedGoldenRecord(customEntity, score);
   }

}
