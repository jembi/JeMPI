package org.jembi.jempi.libmpi.dgraph;

import io.dgraph.DgraphProto;
import io.vavr.control.Either;
import io.vavr.control.Option;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.libmpi.LibMPIClientInterface;
import org.jembi.jempi.libmpi.MpiExpandedGoldenRecord;
import org.jembi.jempi.libmpi.MpiGeneralError;
import org.jembi.jempi.libmpi.MpiServiceError;
import org.jembi.jempi.shared.models.*;
import org.jembi.jempi.shared.utils.LibMPIPaginatedResultSet;
import org.jembi.jempi.shared.utils.SimpleSearchRequestPayload;

import java.util.List;

import static io.dgraph.DgraphProto.Operation.DropOp.DATA;

public class LibDgraph implements LibMPIClientInterface {

    private static final Logger LOGGER = LogManager.getLogger(LibDgraph.class);

    public LibDgraph(final String[] host, final int[] port) {
        LOGGER.info("{}", "LibDgraph Constructor");

        Client.getInstance().config(host, port);
    }

    /*
     * *******************************************************
     * QUERIES
     * *******************************************************
     *
     */

    public LibMPIPaginatedResultSet<MpiExpandedGoldenRecord> simpleSearchGoldenRecords(
            List<SimpleSearchRequestPayload.SearchParameter> params,
            Integer offset,
            Integer limit,
            String sortBy,
            Boolean sortAsc
    ) {
        final var list = Queries.simpleSearchGoldenRecords(params, offset, limit, sortBy, sortAsc);
        if (list == null) {
            return null;
        }
        final var data = list.all().stream().map(CustomLibMPIExpandedGoldenRecord::toMpiExpandedGoldenRecord).toList();
        final var pagination = list.pagination().get(0);
        return new LibMPIPaginatedResultSet(data, pagination);
    }
    public LibMPIPaginatedResultSet<MpiExpandedGoldenRecord> customSearchGoldenRecords(
            List<SimpleSearchRequestPayload> params,
            Integer offset,
            Integer limit,
            String sortBy,
            Boolean sortAsc
    ) {
        final var list = Queries.customSearchGoldenRecords(params, offset, limit, sortBy, sortAsc);
        if (list == null) {
            return null;
        }
        final var data = list.all().stream().map(CustomLibMPIExpandedGoldenRecord::toMpiExpandedGoldenRecord).toList();
        final var pagination = list.pagination().get(0);
        return new LibMPIPaginatedResultSet(data, pagination);
    }
    public LibMPIPaginatedResultSet<CustomEntity> simpleSearchPatientRecords(
            List<SimpleSearchRequestPayload.SearchParameter> params,
            Integer offset,
            Integer limit,
            String sortBy,
            Boolean sortAsc
    ) {
        final var list = Queries.simpleSearchPatientRecords(params, offset, limit, sortBy, sortAsc);
        if (list == null) {
            return null;
        }
        final var data = list.all().stream().map(CustomLibMPIDGraphEntity::toCustomEntity).toList();
        final var pagination = list.pagination().get(0);
        return new LibMPIPaginatedResultSet(data, pagination);
    }
    public LibMPIPaginatedResultSet<CustomEntity> customSearchPatientRecords(
            List<SimpleSearchRequestPayload> params,
            Integer offset,
            Integer limit,
            String sortBy,
            Boolean sortAsc
    ) {
        final var list = Queries.customSearchPatientRecords(params, offset, limit, sortBy, sortAsc);
        if (list == null) {
            return null;
        }
        final var data = list.all().stream().map(CustomLibMPIDGraphEntity::toCustomEntity).toList();
        final var pagination = list.pagination().get(0);
        return new LibMPIPaginatedResultSet(data, pagination);
    }

    public List<CustomGoldenRecord> getCandidates(final CustomEntity customEntity,
            final boolean applyDeterministicFilter) {
        // final var dgraphEntity = new CustomLibMPIDGraphEntity(mpiEntity.entity(),
        // mpiEntity.score());
        final var candidates = CustomLibMPIQueries.getCandidates(customEntity, applyDeterministicFilter);
        return candidates.stream().map(CustomLibMPIGoldenRecord::toCustomGoldenRecord).toList();
    }

    public List<MpiExpandedGoldenRecord> getMpiExpandedGoldenRecordList(final List<String> goldenIdList) {
        final var list = Queries.getExpandedGoldenRecordList(goldenIdList);
        return list.stream().map(CustomLibMPIExpandedGoldenRecord::toMpiExpandedGoldenRecord).toList();
    }

    public List<String> getGoldenIdListByPredicate(final String predicate, final String val) {
        return Queries.getGoldenIdListByPredicate(predicate, val);
    }

    public CustomGoldenRecord getGoldenRecordByUid(final String uid) {
        final var rec = Queries.getGoldenRecordByUid(uid);
        if (rec == null) {
            return null;
        }
        return rec.toCustomGoldenRecord();
    }

    public CustomEntity getMpiEntity(final String uid) {
        final var customEntity = Queries.getDGraphEntity(uid);
        return customEntity;
    }

    public List<String> getGoldenIdList() {
        return Queries.getGoldenIdList();
    }

    @Override
    public CustomEntity getDocument(String uid) {
        final var rec = Queries.getDGraphEntity(uid);
        return rec;
    }

    public long countGoldenRecords() {
        return Queries.countGoldenRecords();
    }

    public long countEntities() {
        return Queries.countEntities();
    }

    /*
     * *******************************************************
     * MUTATIONS
     * *******************************************************
     */

    public boolean updateGoldenRecordPredicate(final String uid, final String predicate, final String val) {
        final var rc = Mutations.updateGoldenRecordPredicate(uid, predicate, val);
        return rc;
    }

    public Either<MpiGeneralError, LinkInfo> unLink(final String goldenID, final String entityID, final float score) {
        return Mutations.unLink(goldenID, entityID, score);
    }

    public Either<MpiGeneralError, LinkInfo> updateLink(final String goldenID, final String newGoldenID,
            final String entityID,
            final float score) {
        return Mutations.updateLink(goldenID, newGoldenID, entityID, score);
    }

    public LinkInfo createEntityAndLinkToExistingGoldenRecord(final CustomEntity customEntity,
            final GoldenIdScore goldenIdScore) {
        // final var dgraphEntity = new CustomLibMPIDGraphEntity(customEntity,
        // goldenIdScore);
        return Mutations.linkDGraphEntity(customEntity, goldenIdScore);
    }

    public LinkInfo createEntityAndLinkToClonedGoldenRecord(final CustomEntity customEntity, float score) {
        final var dgraphEntity = new CustomLibMPIDGraphEntity(customEntity, score);
        final var linkInfo = Mutations.addNewDGraphEntity(customEntity);
        return linkInfo;
    }

    public void startTransaction() {
        Client.getInstance().startTransaction();
    }

    public void closeTransaction() {
        Client.getInstance().closeTransaction();
    }

    /*
     * *******************************************************
     * DATABASE
     * *******************************************************
     */

    public Option<MpiGeneralError> dropAll() {
        try {
            Client.getInstance().alter(DgraphProto.Operation.newBuilder().setDropAll(true).build());
            return Option.none();
        } catch (RuntimeException e) {
            LOGGER.error(e.getMessage(), e);
            return Option.of(new MpiServiceError.GeneralError("Drop All Error"));
        }
    }

    public Option<MpiGeneralError> dropAllData() {
        try {
            Client.getInstance().alter(DgraphProto.Operation.newBuilder().setDropOp(DATA).build());
            return Option.none();
        } catch (RuntimeException e) {
            LOGGER.error(e.getMessage());
            return Option.of(new MpiServiceError.GeneralError("Drop All Data Error"));
        }
    }

    public Option<MpiGeneralError> createSchema() {
        return Mutations.createSchema();
    }

}
