package org.jembi.jempi.libmpi;

import io.vavr.control.Either;
import io.vavr.control.Option;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.libmpi.dgraph.LibDgraph;
import org.jembi.jempi.shared.utils.LibMPIPaginatedResultSet;
import org.jembi.jempi.shared.models.*;
import org.jembi.jempi.shared.utils.SimpleSearchRequestPayload;

import java.util.List;

public class LibMPI {

    private static final Logger LOGGER = LogManager.getLogger(LibMPI.class);
    private final LibMPIClientInterface client;

    public LibMPI(final String[] host, final int[] port) {
        LOGGER.info("{}", "LibMPI Constructor");
        client = new LibDgraph(host, port);
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

    public LibMPIPaginatedResultSet<MpiExpandedGoldenRecord> simpleSearchGoldenRecords(
            List<SimpleSearchRequestPayload.SearchParameter> params,
            Integer offset,
            Integer limit,
            String sortBy,
            Boolean sortAsc
    ) {
        return client.simpleSearchGoldenRecords(params, offset, limit, sortBy, sortAsc);
    }
    public LibMPIPaginatedResultSet<MpiExpandedGoldenRecord> customSearchGoldenRecords(
            List<SimpleSearchRequestPayload> params,
            Integer offset,
            Integer limit,
            String sortBy,
            Boolean sortAsc
    ) {
        return client.customSearchGoldenRecords(params, offset, limit, sortBy, sortAsc);
    }
    public LibMPIPaginatedResultSet<CustomEntity> simpleSearchPatientRecords(
            List<SimpleSearchRequestPayload.SearchParameter> params,
            Integer offset,
            Integer limit,
            String sortBy,
            Boolean sortAsc
    ) {
        return client.simpleSearchPatientRecords(params, offset, limit, sortBy, sortAsc);
    }
    public LibMPIPaginatedResultSet<CustomEntity> customSearchPatientRecords(
            List<SimpleSearchRequestPayload> params,
            Integer offset,
            Integer limit,
            String sortBy,
            Boolean sortAsc
    ) {
        return client.customSearchPatientRecords(params, offset, limit, sortBy, sortAsc);
    }
    public List<CustomGoldenRecord> getCandidates(final CustomEntity customEntity,
                                                  final boolean applyDeterministicFilter) {
        LOGGER.debug("get candidates <- {}", customEntity);
        final var candidates = client.getCandidates(customEntity, applyDeterministicFilter);
        candidates.forEach(candidate -> LOGGER.debug("get candidates -> {}", candidate));
        return candidates;
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

    public CustomEntity getDocument(String uid) {
        return client.getDocument(uid);
    }

    /*
     * *****************************************************************************
     * *
     * Mutations
     * *****************************************************************************
     * *
     */

    public boolean updateGoldenRecordPredicate(final String goldenID, final String predicate, final String value) {
        return client.updateGoldenRecordPredicate(goldenID, predicate, value);
    }

    public Either<MpiGeneralError, LinkInfo> unLink(final String goldenID, final String entityID,
                                                    final float score) {
        return client.unLink(goldenID, entityID, score);
    }

    public Either<MpiGeneralError, LinkInfo> updateLink(final String goldenID,
                                                        final String newGoldenID,
                                                        final String entityID, final float score) {
        return client.updateLink(goldenID, newGoldenID, entityID, score);
    }

    public LinkInfo createEntityAndLinkToExistingGoldenRecord(
            final CustomEntity mpiEntity,
            final LibMPIClientInterface.GoldenIdScore goldenIdScore) {
        LOGGER.debug("link existing <- {}", mpiEntity);
        LOGGER.debug("link existing <- {}", goldenIdScore);
        final var linkInfo = client.createEntityAndLinkToExistingGoldenRecord(mpiEntity, goldenIdScore);
        LOGGER.debug("link existing -> {}", linkInfo);
        return linkInfo;
    }

    public LinkInfo createEntityAndLinkToClonedGoldenRecord(final CustomEntity customEntity, float score) {
        LOGGER.debug("link new <- {}", customEntity);
        LOGGER.debug("link new <- {}", score);
        final var linkInfo = client.createEntityAndLinkToClonedGoldenRecord(customEntity, score);
        LOGGER.debug("link new -> {}", linkInfo);
        return linkInfo;
    }

}
