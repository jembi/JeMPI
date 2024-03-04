package org.jembi.jempi.libmpi.column_based.scyalladb;

import com.datastax.oss.driver.api.core.CqlSessionBuilder;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import io.vavr.control.Either;
import io.vavr.control.Option;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.impl.XMLResponseParser;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrInputDocument;
import org.jembi.jempi.libmpi.LibMPIClientInterface;
import org.jembi.jempi.libmpi.MpiGeneralError;
import org.jembi.jempi.libmpi.dgraph.CustomDgraphGoldenRecord;
import org.jembi.jempi.libmpi.dgraph.DgraphGoldenRecords;
import org.jembi.jempi.shared.models.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import com.datastax.oss.driver.api.core.CqlSession;

public final class LibScyallaDb implements LibMPIClientInterface {

    private static final String KEY_SPACE = "jempi";
    private static final String SOLR_CORE = "jempi";
    private static final Logger LOGGER = LogManager.getLogger(LibScyallaDb.class);
    private final LibMPIClientInterface baseClient;
    private CqlSession scyallDbClient;
    private HttpSolrClient solr;

    public LibScyallaDb(final LibMPIClientInterface baseClientIn) {
        this.baseClient = baseClientIn;
        LOGGER.info("{}", "LibDgraph ScyallaDb");
        this.connectToScyllaDB("0.0.0.0", 9042);
        this.createScyllaDBSchema();
        this.loadSolr("0.0.0.0", 8983);

    }

    private void loadSolr(final String node, final Integer port) {
        String urlString = String.format("http://%s:%d/solr/%s", node, port, LibScyallaDb.SOLR_CORE);
        solr = new HttpSolrClient.Builder(urlString).build();
        solr.setParser(new XMLResponseParser());
    }

    private void connectToScyllaDB(final String node, final Integer port) {
        CqlSessionBuilder b = CqlSession.builder().addContactPoint(new InetSocketAddress(node, port)).withLocalDatacenter("datacenter1");
        scyallDbClient = b.build();

    }

    private void createScyllaDBSchema() {
        scyallDbClient.execute(String.format("DROP KEYSPACE IF EXISTS %s", LibScyallaDb.KEY_SPACE));
        for (String query : getSchema()) {
            scyallDbClient.execute(query);
        }

    }
    private List<String> getSchema() {

        /*
         IMPORTANT: Understanding primary key is cassandra/sycalladb
         */

        return List.of(String.format("""
                CREATE KEYSPACE IF NOT EXISTS %s WITH replication =
                    {'class': 'SimpleStrategy', 'replication_factor' : 1};
                """, LibScyallaDb.KEY_SPACE),
                """
                    CREATE TABLE IF NOT EXISTS jempi.sourceId (
                      facility text,
                      patient text,
                      uid uuid,
                      PRIMARY KEY ((facility, patient), uid)
                    );
                """,
                """
                 CREATE TABLE IF NOT EXISTS jempi.GoldenRecord (
                      uid uuid,
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
                      PRIMARY KEY (national_id, uid)
                    );
                 """,
                """
                  CREATE TABLE IF NOT EXISTS jempi.Interaction (
                      uid uuid,
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
                      PRIMARY KEY (national_id, uid)
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

    private DgraphGoldenRecords rowsToInteraction(final List<Row> diResults) {
        return new DgraphGoldenRecords(diResults.stream().map(v ->
            new CustomDgraphGoldenRecord(
                    v.getUuid("uid").toString(),
                    null,
                    LocalDateTime.ofInstant(v.getInstant("aux_date_created"),  ZoneId.systemDefault()),
                    v.getBoolean("aux_auto_update_enabled"),
                    v.getString("aux_id"),
                    v.getString("given_name"),
                    v.getString("family_name"),
                    v.getString("gender"),
                    v.getString("dob"),
                    v.getString("city"),
                    v.getString("phone_number"),
                    v.getString("national_id")
                )
        ).toList());
    }
    private void findLinkCandidatesInternalProbablistcally(final CustomDemographicData demographicData) {
        SolrQuery query = new SolrQuery();
        query.set("q", String.format("given_name:'%s' AND family_name:'%s' AND phone_number:'%s' AND city:'%s'",
                demographicData.givenName, demographicData.familyName, demographicData.phoneNumber, demographicData.city));
        QueryResponse response = solr.query(query);
    }
    private DgraphGoldenRecords findLinkCandidatesInternalDeterminstacally(final CustomDemographicData demographicData) {
        // todo: Move to Custom
        var d1 = SimpleStatement.builder("SELECT * FROM jempi.GoldenRecord WHERE national_id = :national_id")
                .addNamedValue("national_id", demographicData.nationalId).build();

        var d2 = SimpleStatement.builder(" SELECT * FROM jempi.GoldenRecord WHERE given_name = :given_name and family_name = :family_name and phone_number = :phone_number ALLOW FILTERING")
                .addNamedValue("given_name", demographicData.givenName)
                .addNamedValue("family_name", demographicData.familyName)
                .addNamedValue("phone_number", demographicData.phoneNumber).build();

        for (SimpleStatement q : List.of(d1, d2)) {
            ResultSet r = scyallDbClient.execute(q);
            List<Row> diResults = r.all();
            if (!diResults.isEmpty()) {
                return rowsToInteraction(diResults);
            }
        }

        return null;

    }
    private List<GoldenRecord> findLinkCandidatesInternal(final CustomDemographicData demographicData) {
        for (DgraphGoldenRecords record : List.of(findLinkCandidatesInternalDeterminstacally(demographicData))) {
            if (record != null) {
                return record.all().stream().map(CustomDgraphGoldenRecord::toGoldenRecord).toList();
            }
        }
        return List.of();
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
        return dbSwitchS(() -> baseClient.findLinkCandidates(demographicData),
                () -> findLinkCandidatesInternal(demographicData));
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

    private UUID getSourceId(final Interaction interaction) {
        var sourceId = interaction.sourceId();
        var results = scyallDbClient.execute(
                "SELECT * FROM jempi.sourceId WHERE facility = ? and patient = ?",
                        sourceId.facility() == null ? "" : sourceId.facility(), sourceId.patient() == null ? "" : sourceId.patient() );

        UUID uuid = UUID.randomUUID();
        var row = results.all();
        if (row.isEmpty()) {
            scyallDbClient.execute(
                    "INSERT INTO jempi.sourceId (uid, facility, patient) VALUES (?, ?, ?)",
                            uuid, sourceId.facility() == null ? "" : sourceId.facility(), sourceId.patient() == null ? "" : sourceId.patient());
        } else {
            uuid = row.get(0).getUuid("uid");
        }
        return uuid;
    }

    private record InsertInteractionResult(
            UUID interactionUID,
            UUID sourceUID) {
    }
    private InsertInteractionResult insertInteraction(final Interaction interaction) {

        final String insertQuery = """
                INSERT INTO jempi.Interaction (
                    uid,
                    source_id,
                    aux_date_created,
                    aux_id,
                    aux_clinical_data,
                    given_name,
                    family_name,
                    gender,
                    dob,
                    city,
                    phone_number,
                    national_id
                )
                VALUES (
                    :uid,
                    :source_id,
                    :aux_date_created,
                    :aux_id,
                    :aux_clinical_data,
                    :given_name,
                    :family_name,
                    :gender,
                    :dob,
                    :city,
                    :phone_number,
                    :national_id
                );
            """;
        var sourceId = getSourceId(interaction);
        var uniqueInteractionData = interaction.uniqueInteractionData();
        var demographicData = interaction.demographicData();

        var uidToUse = UUID.randomUUID();
        SimpleStatement statement = SimpleStatement.builder(insertQuery)
                .addNamedValue("uid", uidToUse)
                .addNamedValue("source_id", sourceId)
                .addNamedValue("aux_date_created", uniqueInteractionData.auxDateCreated().toInstant(ZoneOffset.UTC).toEpochMilli())
                .addNamedValue("aux_id", uniqueInteractionData.auxId())
                .addNamedValue("aux_clinical_data", uniqueInteractionData.auxClinicalData())
                .addNamedValue("given_name", demographicData.givenName)
                .addNamedValue("family_name", demographicData.familyName)
                .addNamedValue("gender", demographicData.gender)
                .addNamedValue("dob", demographicData.dob)
                .addNamedValue("city", demographicData.city)
                .addNamedValue("phone_number", demographicData.phoneNumber)
                .addNamedValue("national_id", demographicData.nationalId)
                .build();

        scyallDbClient.execute(statement);

        return new InsertInteractionResult(uidToUse, sourceId);
    }

    private UUID cloneGoldenRecordFromInteraction(
            final CustomDemographicData interaction,
            final UUID interactionUID,
            final UUID sourceUID,
            final float score,
            final CustomUniqueGoldenRecordData customUniqueGoldenRecordData) {


        final String insertQuery = """
                INSERT INTO jempi.GoldenRecord (
                    uid,
                    source_id,
                    aux_date_created,
                    aux_id,
                    aux_auto_update_enabled,
                    given_name,
                    family_name,
                    gender,
                    dob,
                    city,
                    phone_number,
                    national_id,
                    interactions
                )
                VALUES (
                    :uid,
                    :source_id,
                    :aux_date_created,
                    :aux_id,
                    :aux_auto_update_enabled,
                    :given_name,
                    :family_name,
                    :gender,
                    :dob,
                    :city,
                    :phone_number,
                    :national_id,
                    :interactions
                );
            """;

        var uidToUse = UUID.randomUUID();
        SimpleStatement statement = SimpleStatement.builder(insertQuery)
                .addNamedValue("uid", uidToUse)
                .addNamedValue("source_id", sourceUID)
                .addNamedValue("aux_date_created", customUniqueGoldenRecordData.auxDateCreated().toInstant(ZoneOffset.UTC).toEpochMilli())
                .addNamedValue("aux_id", customUniqueGoldenRecordData.auxId())
                .addNamedValue("aux_auto_update_enabled", customUniqueGoldenRecordData.auxAutoUpdateEnabled())
                .addNamedValue("given_name", interaction.givenName)
                .addNamedValue("family_name", interaction.familyName)
                .addNamedValue("gender", interaction.gender)
                .addNamedValue("dob", interaction.dob)
                .addNamedValue("city", interaction.city)
                .addNamedValue("phone_number", interaction.phoneNumber)
                .addNamedValue("national_id", interaction.nationalId)
                .addNamedValue("interactions", interactionUID)
                .build();

        scyallDbClient.execute(statement);
        addToSolr(uidToUse, interaction);
        return uidToUse;
    }

    private void addToSolr(final UUID uidToUse, final CustomDemographicData interaction) {

        SolrInputDocument document = new SolrInputDocument();
        document.addField("uid", uidToUse.toString());
        document.addField("given_name", interaction.givenName);
        document.addField("family_name", interaction.familyName);
        document.addField("city", interaction.city);
        document.addField("dob", interaction.dob);
        document.addField("phoneNumber", interaction.phoneNumber);
        document.addField("national_id", interaction.nationalId);
        try {
            solr.add(document);
            solr.commit();
        } catch (SolrServerException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
    public LinkInfo createInteractionAndLinkToClonedGoldenRecord(
            final Interaction interaction,
            final float score) {
        return dbSwitchS(() -> baseClient.createInteractionAndLinkToClonedGoldenRecord(interaction, score),
                            () -> {
                                var result = insertInteraction(interaction);
                                if (result.interactionUID == null) {
                                    LOGGER.error("Failed to insert interaction");
                                    return null;
                                }
                                final var grUID = cloneGoldenRecordFromInteraction(interaction.demographicData(),
                                        result.interactionUID,
                                        result.sourceUID,
                                        1.0F,
                                        new CustomUniqueGoldenRecordData(interaction.uniqueInteractionData()));

                                if (grUID == null) {
                                    LOGGER.error("Failed to insert golden record");
                                    return null;
                                }

                                return new LinkInfo(grUID.toString(), result.interactionUID.toString(), result.sourceUID.toString(), 1.0F);

                            });
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

