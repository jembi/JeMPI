package org.jembi.jempi.journal.cassandra;

import com.datastax.oss.driver.api.core.AllNodesFailedException;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.DriverTimeoutException;
import com.datastax.oss.driver.api.core.auth.ProgrammaticPlainTextAuthProvider;
import com.datastax.oss.driver.api.core.connection.ClosedConnectionException;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.servererrors.QueryExecutionException;
import com.datastax.oss.driver.api.core.servererrors.QueryValidationException;
import com.datastax.oss.driver.api.core.type.DataTypes;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import com.datastax.oss.driver.api.querybuilder.SchemaBuilder;
import com.datastax.oss.driver.api.querybuilder.term.Term;
import io.vavr.control.Option;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.journal.JournalInterface;
import org.jembi.jempi.libmpi.MpiGeneralError;
import org.jembi.jempi.libmpi.MpiServiceError;

import java.net.InetSocketAddress;
import java.util.HashMap;

public class JournalCassandra implements JournalInterface {

   private static final Logger LOGGER = LogManager.getLogger(JournalCassandra.class);
   private static final String KEYSPACE = "jempi";
   private static final String TYPE_SOURCE_ID = "source_id_t";
   private static final String TYPE_ENTITY = "entity_t";
   private static final String TABLE_NAME = "journal";
   private String[] host;
   private int[] port;

   private CqlSession session;

   public JournalCassandra() {
      LOGGER.debug("New Journal client");
      final String[] altHosts = new String[]{"cassandra-1", "cassandra-2", "cassandra-3"};
      final int[] altPorts = new int[]{9042, 9042, 9042};
      config(altHosts, altPorts);
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

   public void startTransaction() {
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
         session.execute(String.format("DROP KEYSPACE IF EXISTS %s", KEYSPACE));
      } catch (AllNodesFailedException | QueryExecutionException | QueryValidationException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
         zapTransaction();
         return Option.of(new MpiServiceError.GeneralError(e.getLocalizedMessage()));
      }
      return Option.none();
   }

   public Option<MpiGeneralError> createSchema() {
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
                            .withPartitionKey("uid", DataTypes.TEXT)
                            .withPartitionKey("event_date", DataTypes.DATE)
                            .withClusteringColumn("event_timestamp", DataTypes.TIMESTAMP)
                            .withColumn("gid", DataTypes.TEXT)
                            .withColumn("did", DataTypes.TEXT)
                            .withColumn("event", DataTypes.TEXT)
                            .build());
      } catch (AllNodesFailedException | QueryExecutionException | QueryValidationException | IllegalArgumentException
               | ClosedConnectionException | DriverTimeoutException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
         zapTransaction();
         return Option.of(new MpiServiceError.GeneralError(e.getLocalizedMessage()));
      }
      return Option.none();
   }

   public void logEvent(final String uid, final String gid, final String did, final String event) {
      final var map = new HashMap<String, Term>();
      map.put("uid", QueryBuilder.literal(uid));
      map.put("event_date", QueryBuilder.currentDate());
      if (StringUtils.isNotBlank(gid)) {
         map.put("gid", QueryBuilder.literal(gid));
      }
      if (StringUtils.isNotBlank(did)) {
         map.put("did", QueryBuilder.literal(did));
      }
      map.put("event_timestamp", QueryBuilder.currentTimestamp());
      map.put("event", QueryBuilder.literal(event));
      try {
         session.execute(
               QueryBuilder
                     .insertInto(KEYSPACE, TABLE_NAME)
//                     .value("uid", QueryBuilder.literal(uid))
//                     .value("event_date", QueryBuilder.currentDate())
//                     .value("event_timestamp", QueryBuilder.currentTimestamp())
                     .values(map)
//                     .value("event", QueryBuilder.literal(event))
                     .build());
      } catch (AllNodesFailedException | QueryExecutionException | QueryValidationException | IllegalArgumentException
               | ClosedConnectionException | DriverTimeoutException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
         zapTransaction();
      }
   }

}
