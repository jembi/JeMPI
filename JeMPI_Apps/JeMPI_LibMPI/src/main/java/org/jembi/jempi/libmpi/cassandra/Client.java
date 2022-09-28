package org.jembi.jempi.libmpi.cassandra;

import com.datastax.oss.driver.api.core.AllNodesFailedException;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.auth.ProgrammaticPlainTextAuthProvider;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.servererrors.QueryExecutionException;
import com.datastax.oss.driver.api.core.servererrors.QueryValidationException;
import com.datastax.oss.driver.api.core.type.DataTypes;
import com.datastax.oss.driver.api.querybuilder.SchemaBuilder;
import io.vavr.control.Option;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.libmpi.MpiGeneralError;
import org.jembi.jempi.libmpi.MpiServiceError;
import org.jembi.jempi.shared.models.CustomEntity;
import org.jembi.jempi.shared.models.CustomGoldenRecord;

import java.net.InetSocketAddress;
import java.util.List;

public class Client {

   private static final Logger LOGGER = LogManager.getLogger(Client.class);
   private static final String KEYSPACE = "jempi";
   private static final String TYPE_SOURCE_ID = "source_id_t";
   private static final String TYPE_ENTITY = "entity_t";
   private static final String TABLE_NAME = "mpi";
   private String[] host;
   private int[] port;

   private CqlSession session;

   private Client() {
   }

   static Client getInstance() {
      return Client.ClientHolder.INSTANCE;
   }

   void config(final String[] host, final int[] port) {
      LOGGER.debug("{} {}", host, port);
      this.host = host;
      this.port = port;
   }

   public void connect() {
      LOGGER.debug("connect");
      session = CqlSession.builder()
                          .addContactPoint(new InetSocketAddress(host[0], port[0]))
                          .addContactPoint(new InetSocketAddress(host[1], port[1]))
                          .addContactPoint(new InetSocketAddress(host[2], port[2]))
                          .withLocalDatacenter("datacenter1")
                          .withAuthProvider(new ProgrammaticPlainTextAuthProvider("cassandra", "cassandra"))
                          .build();
      LOGGER.debug("session name: {}", session.getName());
      ResultSet rs = session.execute("SELECT release_version FROM system.local");
      Row row = rs.one();
      if (row == null) {
         LOGGER.error("NO VERSION");
      } else {
         LOGGER.info("{}", row.getString("release_version"));
      }
   }

   void startTransaction() {
      LOGGER.debug("{}", "startTransaction");
      if (session == null) {
         connect();
      }
   }

   void zapTransaction() {
      session.close();
      session = null;
   }

   void closeTransaction() {
      LOGGER.debug("{}", "closeTransaction");
      session.close();
      session = null;
   }

   Option<MpiGeneralError> dropAll() {
      try {
         session.execute("DROP KEYSPACE IF EXISTS jempi");
      } catch (AllNodesFailedException | QueryExecutionException | QueryValidationException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
         zapTransaction();
         return Option.of(new MpiServiceError.GeneralError(e.getLocalizedMessage()));
      }
      return Option.none();
   }

   Option<MpiGeneralError> createSchema() {
      try {
         session.execute(
               SchemaBuilder.createKeyspace(KEYSPACE)
                            .ifNotExists()
                            .withSimpleStrategy(2)
                            .build());
         session.execute(
               SchemaBuilder.createType(KEYSPACE, TYPE_SOURCE_ID)
                            .ifNotExists()
                            .withField("uid", DataTypes.UUID)
                            .withField("facility", DataTypes.TEXT)
                            .withField("patient", DataTypes.TEXT)
                            .build());
         session.execute(
               SchemaBuilder.createType(KEYSPACE, TYPE_ENTITY)
                            .ifNotExists()
                            .withField("uid", DataTypes.UUID)
                            .withField("source_id", SchemaBuilder.udt(TYPE_SOURCE_ID, true))
                            .withField("aux_id", DataTypes.TEXT)
                            .withField("given_name", DataTypes.TEXT)
                            .withField("family_name", DataTypes.TEXT)
                            .withField("gender", DataTypes.TEXT)
                            .withField("dob", DataTypes.TEXT)
                            .withField("city", DataTypes.TEXT)
                            .withField("phone_number", DataTypes.TEXT)
                            .withField("national_id", DataTypes.TEXT)
                            .build());
         session.execute(
               SchemaBuilder.createTable(KEYSPACE, TABLE_NAME)
                            .ifNotExists()
                            .withPartitionKey("uid", DataTypes.UUID)
                            .withClusteringColumn("city", DataTypes.TEXT)
                            .withClusteringColumn("family_name", DataTypes.TEXT)
                            .withClusteringColumn("given_name", DataTypes.TEXT)
                            .withColumn("source_id", DataTypes.listOf(SchemaBuilder.udt(TYPE_SOURCE_ID, true)))
                            .withColumn("aux_id", DataTypes.TEXT)
                            .withColumn("gender", DataTypes.TEXT)
                            .withColumn("dob", DataTypes.TEXT)
                            .withColumn("city", DataTypes.TEXT)
                            .withColumn("phone_number", DataTypes.TEXT)
                            .withColumn("national_id", DataTypes.TEXT)
                            .withColumn("entity_list", DataTypes.listOf(SchemaBuilder.udt(TYPE_ENTITY, true)))
                            .build());
         session.execute(
               SchemaBuilder.createIndex()
                            .ifNotExists()
                            .onTable(KEYSPACE, TABLE_NAME)
                            .andColumn("national_id")
                            .build());
         session.execute(
               SchemaBuilder.createIndex()
                            .ifNotExists()
                            .onTable(KEYSPACE, TABLE_NAME)
                            .andColumn("given_name")
                            .build());
         session.execute(
               SchemaBuilder.createIndex()
                            .ifNotExists()
                            .onTable(KEYSPACE, TABLE_NAME)
                            .andColumn("family_name")
                            .build());
         session.execute(
               SchemaBuilder.createIndex()
                            .ifNotExists()
                            .onTable(KEYSPACE, TABLE_NAME)
                            .andColumn("phone_number")
                            .build());
      } catch (AllNodesFailedException | QueryExecutionException | QueryValidationException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
         zapTransaction();
         return Option.of(new MpiServiceError.GeneralError(e.getLocalizedMessage()));
      }
      return Option.none();
   }

   public List<CustomGoldenRecord> getCandidates(final CustomEntity customEntity,
                                                 final boolean applyDeterministicFilter) {
      LOGGER.error("mpiEntity: {}", customEntity);
      LOGGER.error("deterministic: {}", applyDeterministicFilter);

      final var givenName = customEntity.givenName();
      final var familyName = customEntity.familyName();
      final var phoneNumber = customEntity.phoneNumber();
      final var nationalId = customEntity.nationalId();
      final var givenNameIsBlank = StringUtils.isBlank(givenName);
      final var familyNameIsBlank = StringUtils.isBlank(familyName);
      final var phoneNumberIsBlank = StringUtils.isBlank(phoneNumber);
      final var nationalIdIsBlank = StringUtils.isBlank(nationalId);
      if ((nationalIdIsBlank && (givenNameIsBlank || familyNameIsBlank || phoneNumberIsBlank))) {
         return List.of();
      }
/*
      final var map = Map.of(
            "$given_name",
            StringUtils.isNotBlank(givenName)
            ? givenName
            : Queries.EMPTY_FIELD_SENTINEL,
            "$family_name",
            StringUtils.isNotBlank(familyName)
            ? familyName
            : Queries.EMPTY_FIELD_SENTINEL,
            "$phone_number",
            StringUtils.isNotBlank(phoneNumber)
            ? phoneNumber
            : Queries.EMPTY_FIELD_SENTINEL,
            "$national_id",
            StringUtils.isNotBlank(nationalId)
            ? nationalId
            : Queries.EMPTY_FIELD_SENTINEL);
*/

      return List.of();
   }

   private static class ClientHolder {
      public static final Client INSTANCE = new Client();
   }

}
