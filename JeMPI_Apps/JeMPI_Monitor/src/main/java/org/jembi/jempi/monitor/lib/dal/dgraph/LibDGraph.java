package org.jembi.jempi.monitor.lib.dal.dgraph;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dgraph.DgraphClient;
import io.dgraph.DgraphGrpc;
import io.dgraph.Transaction;
import io.grpc.ManagedChannelBuilder;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import java.io.BufferedReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.monitor.RestHttpServer;
import org.jembi.jempi.monitor.lib.dal.IDAL;

import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.StringJoiner;
import java.util.function.Supplier;

@FunctionalInterface
interface ThrowingFunction<T, R, E extends Exception> {
    R apply(T t) throws E;
}
public class LibDGraph implements IDAL {
    private static final Logger LOGGER = LogManager.getLogger(RestHttpServer.class);
    private static DgraphClient dGraphClient;
    private final String[] host;
    private final int[] port;
    private final int[] httpPorts;
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class PostResponse{
        @JsonIgnoreProperties(ignoreUnknown = true)
        static class ErrorMessage{
            String message;

            public String getMessage() {
                return message;
            }

            public void setMessage(String message) {
                this.message = message;
            }
        }

        private ErrorMessage[] errors;

        public ErrorMessage[] getErrors() {
            return errors;
        }

        public void setErrors(ErrorMessage[] errors) {
            this.errors = errors;
        }
    }
    public DgraphClient getDGraphClient() throws Exception {
        if (dGraphClient == null){
            if (host.length != port.length){
                throw new ArrayIndexOutOfBoundsException("The length of dgraph hosts should match that of ports");
            }

            var dgraphStubs = new DgraphGrpc.DgraphStub[host.length];
            for (int i = 0; i < host.length; i++) {
                dgraphStubs[i] = DgraphGrpc.newStub(ManagedChannelBuilder.forAddress(host[i], port[i])
                        .maxInboundMessageSize(100 * 1024 * 1024)
                        .usePlaintext()
                        .build());
            }
            DgraphClient dgraphClient = new DgraphClient(dgraphStubs);
            var version = dgraphClient.checkVersion().getTag();
            if (StringUtils.isBlank(version)) {
                throw new Exception("Could not create dgraph clients");
            }
            dGraphClient =  dgraphClient;
        }
        return dGraphClient;

    }
    public LibDGraph(String[] host, int[] port, int[] httpPorts) {
        LOGGER.info("{}", "LibDGraph Constructor");
        this.host = host;
        this.port = port;
        this.httpPorts = httpPorts;
    }

    private <T> Boolean RunQuery(ThrowingFunction<Transaction, T, Exception> getStatement) throws Exception {
        DgraphClient client = getDGraphClient();
        Transaction transaction = client.newTransaction();

        try{
            getStatement.apply(transaction);
            transaction.commit();
            return true;
        } catch (Exception e){
            transaction.discard();
            throw e;
        }
    }

    private Boolean PostRequest(final String urlSuffix, Supplier<String> postContent) throws Exception {
        URL url = new URL(String.format("http://%s:%d/%s", host[0], httpPorts[0], urlSuffix));
        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        con.setRequestMethod("POST");
        con.setDoOutput(true);

        OutputStream os = con.getOutputStream();
        byte[] input = postContent.get().getBytes(StandardCharsets.UTF_8);
        os.write(input, 0, input.length);
        int responseCode = con.getResponseCode();

        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {

            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
        }
        if (responseCode >= 200 && responseCode < 300){
            ObjectMapper objectMapper = new ObjectMapper();

            PostResponse responseObject = objectMapper.readValue(response.toString(), PostResponse.class);
            if (responseObject != null && responseObject.getErrors() != null && responseObject.getErrors().length > 0){
                StringJoiner joiner = new StringJoiner(" ");
                for (PostResponse.ErrorMessage error : responseObject.getErrors()) {
                    joiner.add(error.getMessage());
                }
                throw new Exception(joiner.toString());
            }
            return true;
        }
        throw new Exception(response.toString());
    }
    public boolean deleteAllData() throws Exception {
        return this.PostRequest("alter", () -> "{\"drop_op\": \"DATA\"}");
    }

    public boolean deleteTableData(String tableName) {
        throw new NotImplementedException("Dgraph does not support delete table");
    }
}
