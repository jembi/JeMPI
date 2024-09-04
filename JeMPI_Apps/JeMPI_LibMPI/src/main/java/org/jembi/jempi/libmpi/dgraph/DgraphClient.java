package org.jembi.jempi.libmpi.dgraph;

import io.dgraph.DgraphGrpc;
import io.dgraph.DgraphProto;
// import io.dgraph.TxnConflictException;
import io.grpc.ManagedChannelBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.shared.models.GlobalConstants;
import org.jembi.jempi.shared.utils.AppUtils;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public final class DgraphClient {

   private static final Logger LOGGER = LogManager.getLogger(DgraphClient.class);
   private io.dgraph.DgraphClient dgraphClient;
   private String[] host;
   private int[] port;

   private DgraphClient() {
      dgraphClient = null;
   }

   static DgraphClient getInstance() {
      return ClientHolder.INSTANCE;
   }

   void config(
         final String[] host,
         final int[] port) {
      this.host = host;
      this.port = port;
   }

   /*
    * *****************************************************************************
    * *
    * Database
    * *****************************************************************************
    * *
    */

   void connect() {
      if (dgraphClient == null) {
         var hostList = new ArrayList<DgraphClient.AlphaHost>();
         for (int i = 0; i < host.length; i++) {
            hostList.add(new DgraphClient.AlphaHost(host[i], port[i]));
         }
         var dgraphStubs = new DgraphGrpc.DgraphStub[hostList.size()];
         for (int i = 0; i < hostList.size(); i++) {
            AlphaHost alphaHost = hostList.get(i);
            dgraphStubs[i] = DgraphGrpc.newStub(ManagedChannelBuilder.forAddress(alphaHost.host, alphaHost.port)
                                                                     .maxInboundMessageSize(100 * 1024 * 1024)
                                                                     .usePlaintext()
                                                                     .build());
         }
         dgraphClient = new io.dgraph.DgraphClient(dgraphStubs);
         var version = dgraphClient.checkVersion().getTag();
         if (StringUtils.isBlank(version)) {
            LOGGER.error("Cannot create client");
         }
      }
   }

   void disconnect() {
      if (dgraphClient != null) {
         dgraphClient.shutdown();
         dgraphClient = null;
      }
   }

   private void sleep() {
      try {
         Thread.sleep(GlobalConstants.TIMEOUT_DGRAPH_RECONNECT_SLEEP_SECS * 1000L);
      } catch (InterruptedException ex) {
         LOGGER.error(ex.getLocalizedMessage(), ex);
      }
   }

   void alter(final DgraphProto.Operation op) {
      dgraphClient.alter(op);
   }

   private <T> T runWithRetries(final Supplier<T> supplier, final int retries) {
      int remainingRetries = retries;
      while (remainingRetries > 0) {
         try {
            return supplier.get();
         } catch (RuntimeException ex) {
            LOGGER.warn("Retrying due to exception: {}", ex.getMessage());
            LOGGER.warn("Cause: {}", ex.getCause());
            remainingRetries--;
            if (remainingRetries == 0) {
               LOGGER.warn("Failed after retries.");
               throw ex;
            }
            sleep(); // Take a break and try again
         }
      }
      throw new RuntimeException("Retries exhausted");
   }

   String executeReadOnlyTransaction(
         final String query,
         final Map<String, String> vars) {
      return runWithRetries(() -> {
         final var txn = dgraphClient.newReadOnlyTransaction();
         try {
            io.dgraph.DgraphProto.Response response;
            if (AppUtils.isNullOrEmpty(vars)) {
               response = txn.query(query);
            } else {
               response = txn.queryWithVars(query, vars);
            }
            return response.getJson().toStringUtf8();
         } finally {
            txn.discard();
         }
      }, GlobalConstants.TIMEOUT_DGRAPH_RECONNECT_RETRIES);
   }

   String doMutateTransaction(final DgraphProto.Mutation mutation) {
      return runWithRetries(() -> {
         final var txn = dgraphClient.newTransaction();
         if (txn == null) {
            LOGGER.error("NO TRANSACTION");
            return null;
         }
         try {
            final var request = DgraphProto.Request.newBuilder().setCommitNow(true).addMutations(mutation).build();
            final var response = txn.doRequest(request, GlobalConstants.TIMEOUT_DGRAPH_QUERY_SECS, TimeUnit.SECONDS);
            return response.getUidsMap().values().stream().findFirst().orElse(StringUtils.EMPTY);
         } catch (RuntimeException ex) {
            LOGGER.error("Error during mutation transaction: {}", ex.getMessage(), ex);
            LOGGER.error("Mutation details: {}", mutation);
            throw ex;
         } finally {
            txn.discard();
         }
      }, GlobalConstants.TIMEOUT_DGRAPH_RECONNECT_RETRIES);
   }

   record AlphaHost(
         String host,
         int port) {
   }

   private static class ClientHolder {
      public static final DgraphClient INSTANCE = new DgraphClient();
   }

}
