package org.jembi.jempi.libmpi.column_based.scyalladb;

import com.datastax.oss.driver.api.core.CqlSessionBuilder;
import io.vavr.control.Either;
import io.vavr.control.Option;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.libmpi.LibMPIClientInterface;
import org.jembi.jempi.libmpi.MpiGeneralError;
import org.jembi.jempi.shared.models.*;

import java.net.InetSocketAddress;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Supplier;

import com.datastax.oss.driver.api.core.CqlSession;

public final class LibScyallaDb implements LibMPIClientInterface {

    private static final Logger LOGGER = LogManager.getLogger(LibScyallaDb.class);
    private final LibMPIClientInterface baseClient;
    private  CqlSession scyallDbClient;

    public LibScyallaDb(final LibMPIClientInterface baseClientIn) {
        this.baseClient = baseClientIn;
        LOGGER.info("{}", "LibDgraph ScyallaDb");
        this.connectToScyllaDB("0.0.0.0", 9042);
        this.createScyllaDBSchema();

    }

    private void connectToScyllaDB(final String node, final Integer port) {
        CqlSessionBuilder b = CqlSession.builder().addContactPoint(new InetSocketAddress(node, port)).withLocalDatacenter("datacenter1");
        scyallDbClient = b.build();

    }

    private void createScyllaDBSchema() {
        for (String query : getSchema()) {
            scyallDbClient.execute(query);
        }

    }
    private List<String> getSchema() {
        return List.of("""
                CREATE KEYSPACE IF NOT EXISTS jempi WITH replication =
                    {'class': 'SimpleStrategy', 'replication_factor' : 1};
                """,
                """
                    CREATE TYPE IF NOT EXISTS jempi.sourceId (
                      facility text,
                      patient text
                    );
                """,
                """
                 CREATE TABLE IF NOT EXISTS jempi.GoldenRecord (
                      source_id uuid,
                      aux_date_created timestamp,
                      aux_auto_update_enabled boolean,
                      aux_id text,
                      given_name text,
                      family_name text,
                      gender text,
                      dob text,
                      city text,
                      phone_number text,
                      national_id text,
                      interactions uuid,
                      PRIMARY KEY (source_id)
                    );
                 """,
                """
                  CREATE TABLE IF NOT EXISTS jempi.Interaction (
                      source_id uuid,
                      aux_date_created timestamp,
                      aux_id text,
                      aux_clinical_data text,
                      given_name text,
                      family_name text,
                      gender text,
                      dob text,
                      city text,
                      phone_number text,
                      national_id text,
                      PRIMARY KEY (source_id)
                    );
                  """
                );
    }

    /*
     * *******************************************************
     * QUERIES
     * *******************************************************
     *
     */

    public long countInteractions() {
        return baseClient.countInteractions();
    }

    public long countGoldenRecords() {
        return baseClient.countGoldenRecords();
    }

    public Interaction findInteraction(final String interactionId) {
        return baseClient.findInteraction(interactionId);
    }

    public List<Interaction> findInteractions(final List<String> interactionIds) {
        return List.of();
    }

    public List<ExpandedInteraction> findExpandedInteractions(final List<String> interactionIds) {
        return baseClient.findExpandedInteractions(interactionIds);
    }

    public GoldenRecord findGoldenRecord(final String goldenId) {
        return dbSwitchS(() -> baseClient.findGoldenRecord(goldenId), null);
    }

    public List<GoldenRecord> findGoldenRecords(final List<String> ids) {
        return dbSwitchS(() -> baseClient.findGoldenRecords(ids), null);
    }

    public List<ExpandedGoldenRecord> findExpandedGoldenRecords(final List<String> goldenIds) {
        return dbSwitchS(() -> baseClient.findExpandedGoldenRecords(goldenIds), null);
    }

    @Override
    public List<String> findGoldenIds() {
        return dbSwitchS(baseClient::findGoldenIds, null);
    }

    public List<String> fetchGoldenIds(
            final long offset,
            final long length) {
        return dbSwitchS(() -> baseClient.fetchGoldenIds(offset, length), null);
    }

    public List<GoldenRecord> findLinkCandidates(final CustomDemographicData demographicData) {
        return dbSwitchS(() -> baseClient.findLinkCandidates(demographicData), null);
    }

    public List<GoldenRecord> findMatchCandidates(final CustomDemographicData demographicData) {
        return dbSwitchS(() -> baseClient.findMatchCandidates(demographicData), null);
    }

    public List<GoldenRecord> findGoldenRecords(final ApiModels.ApiCrFindRequest request) {
        return dbSwitchS(() -> baseClient.findGoldenRecords(request), null);
    }

    public boolean setScore(
            final String interactionUID,
            final String goldenRecordUid,
            final float score) {
        return dbSwitchS(() -> baseClient.setScore(interactionUID, goldenRecordUid, score), null);
    }

    public LibMPIPaginatedResultSet<ExpandedGoldenRecord> simpleSearchGoldenRecords(
            final List<ApiModels.ApiSearchParameter> params,
            final Integer offset,
            final Integer limit,
            final String sortBy,
            final Boolean sortAsc) {
        return dbSwitchS(() -> baseClient.simpleSearchGoldenRecords(params, offset, limit, sortBy, sortAsc), null);

    }

    public LibMPIPaginatedResultSet<ExpandedGoldenRecord> customSearchGoldenRecords(
            final List<ApiModels.ApiSimpleSearchRequestPayload> params,
            final Integer offset,
            final Integer limit,
            final String sortBy,
            final Boolean sortAsc) {
        return dbSwitchS(() -> baseClient.customSearchGoldenRecords(params, offset, limit, sortBy, sortAsc), null);
    }

    public LibMPIPaginatedResultSet<Interaction> simpleSearchInteractions(
            final List<ApiModels.ApiSearchParameter> params,
            final Integer offset,
            final Integer limit,
            final String sortBy,
            final Boolean sortAsc) {
        return dbSwitchS(() -> baseClient.simpleSearchInteractions(params, offset, limit, sortBy, sortAsc), null);
    }

    public LibMPIPaginatedResultSet<Interaction> customSearchInteractions(
            final List<ApiModels.ApiSimpleSearchRequestPayload> params,
            final Integer offset,
            final Integer limit,
            final String sortBy,
            final Boolean sortAsc) {
        return dbSwitchS(() -> baseClient.customSearchInteractions(params, offset, limit, sortBy, sortAsc), null);
    }

    public LibMPIPaginatedResultSet<String> filterGids(
            final List<ApiModels.ApiSearchParameter> params,
            final LocalDateTime createdAt,
            final PaginationOptions paginationOptions) {
        return dbSwitchS(() -> baseClient.filterGids(params, createdAt, paginationOptions), null);
    }

    public PaginatedGIDsWithInteractionCount filterGidsWithInteractionCount(
            final List<ApiModels.ApiSearchParameter> params,
            final LocalDateTime createdAt,
            final PaginationOptions paginationOptions) {
        return dbSwitchS(() -> baseClient.filterGidsWithInteractionCount(params, createdAt, paginationOptions), null);
    }


    /*
     * *******************************************************
     * MUTATIONS
     * *******************************************************
     */

    public boolean updateGoldenRecordField(
            final String goldenId,
            final String fieldName,
            final String val) {
        return dbSwitchS(() -> baseClient.updateGoldenRecordField(goldenId, fieldName, val), null);

    }

    public boolean updateGoldenRecordField(
            final String goldenId,
            final String fieldName,
            final Boolean val) {
        return dbSwitchS(() -> baseClient.updateGoldenRecordField(goldenId, fieldName, val), null);
    }

    public boolean updateGoldenRecordField(
            final String goldenId,
            final String fieldName,
            final Double val) {
        return dbSwitchS(() -> baseClient.updateGoldenRecordField(goldenId, fieldName, val), null);
    }

    public boolean updateGoldenRecordField(
            final String goldenId,
            final String fieldName,
            final Long val) {
        return dbSwitchS(() -> baseClient.updateGoldenRecordField(goldenId, fieldName, val), null);
    }

    public Either<MpiGeneralError, LinkInfo> linkToNewGoldenRecord(
            final String goldenUID,
            final String interactionUID,
            final float score) {
        return dbSwitchS(() -> baseClient.linkToNewGoldenRecord(goldenUID, interactionUID, score), null);
    }

    public Either<MpiGeneralError, LinkInfo> updateLink(
            final String goldenUID,
            final String newGoldenUID,
            final String interactionUID,
            final float score) {
        return dbSwitchS(() -> baseClient.updateLink(goldenUID, newGoldenUID, interactionUID, score), null);
    }

    public LinkInfo createInteractionAndLinkToExistingGoldenRecord(
            final Interaction interaction,
            final GoldenIdScore goldenIdScore) {
        return dbSwitchS(() -> baseClient.createInteractionAndLinkToExistingGoldenRecord(interaction, goldenIdScore), null);
    }

    public LinkInfo createInteractionAndLinkToClonedGoldenRecord(
            final Interaction interaction,
            final float score) {
        return dbSwitchS(() -> baseClient.createInteractionAndLinkToClonedGoldenRecord(interaction, score), null);
    }

    public void startTransaction() {
        dbSwitchR(baseClient::startTransaction, null);
    }

    public void closeTransaction() {
        dbSwitchR(baseClient::closeTransaction, null);
    }

    /*
     * *******************************************************
     * DATABASE
     * *******************************************************
     */

    public Option<MpiGeneralError> dropAll() {
        return dbSwitchS(baseClient::dropAll, null);
    }

    public Option<MpiGeneralError> dropAllData() {
        return dbSwitchS(baseClient::dropAllData, null);
    }

    public Option<MpiGeneralError> createSchema() {
        return dbSwitchS(baseClient::createSchema, null);
    }

    private <T> T dbSwitchS(final Supplier<T> func1, final Supplier<T> func2) {
        if (func2 != null) {
            return func2.get();
        }
        return func1.get();
    }

    private void dbSwitchR(final Runnable func1, final Runnable func2) {
        if (func2 != null) {
             func2.run();
        } else {
            func1.run();
        }

    }
}

