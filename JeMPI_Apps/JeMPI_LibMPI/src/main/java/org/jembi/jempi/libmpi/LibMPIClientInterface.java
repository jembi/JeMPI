package org.jembi.jempi.libmpi;

import io.vavr.control.Either;
import io.vavr.control.Option;
import org.jembi.jempi.shared.models.CustomEntity;
import org.jembi.jempi.shared.models.CustomGoldenRecord;

import java.util.List;

public interface LibMPIClientInterface {

    /*
     * *****************************************************************************
     * *
     * Database
     * *****************************************************************************
     * *
     */
    void startTransaction();

    void closeTransaction();

    Option<MpiGeneralError> dropAll();

    Option<MpiGeneralError> dropAllData();

    Option<MpiGeneralError> createSchema();

    /*
     * *****************************************************************************
     * *
     * Queries
     * *****************************************************************************
     * *
     */

    List<CustomGoldenRecord> getCandidates(final CustomEntity customEntity, boolean applyDeterministicFilter);

    List<MpiExpandedGoldenRecord> getMpiExpandedGoldenRecordList(final List<String> goldenIdList);

    List<String> getGoldenIdListByPredicate(final String predicate, final String val);

    CustomGoldenRecord getGoldenRecordByUid(final String uid);

    CustomEntity getMpiEntity(final String uid);

    List<String> getGoldenIdList();

    CustomEntity getDocument(String uid);

    long countGoldenRecords();

    long countEntities();

    /*
     * *****************************************************************************
     * *
     * Mutations
     * *****************************************************************************
     * *
     */

    boolean updateGoldenRecordPredicate(final String uid, final String predicate, final String value);

    Either<MpiGeneralError, LinkInfo> unLink(final String goldenID, final String entityID, final float score);

    Either<MpiGeneralError, LinkInfo> updateLink(
            final String goldenID, final String newGoldenID, final String entityID, final float score);

    LinkInfo createEntityAndLinkToExistingGoldenRecord(final CustomEntity customEntity,
            final GoldenIdScore goldenIdScore);

    LinkInfo createEntityAndLinkToClonedGoldenRecord(final CustomEntity customEntity, float score);

    record LinkInfo(String goldenId, String entityId, float score) {
    }

    record GoldenIdScore(String goldenId, float score) {
    }

}
