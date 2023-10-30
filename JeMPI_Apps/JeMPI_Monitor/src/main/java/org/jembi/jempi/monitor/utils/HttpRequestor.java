package org.jembi.jempi.monitor.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

interface ThrowingFunction<T, R, E extends Exception> {
    R apply(T t) throws E;
}
public class HttpRequestor {

    public record HttpRequestorResponse(int responseCode, String response){}

    public static HttpRequestorResponse GetRequest(final String requestUrl) throws Exception {
        return HttpRequestor.DoRequest(requestUrl, "GET", con -> null);
    }

    private static HttpRequestorResponse DoRequest(final String requestUrl, final String requestMethod, ThrowingFunction<HttpURLConnection, Void, Exception> beforeRequest) throws Exception{
        URL url = new URL(requestUrl);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        con.setRequestMethod(requestMethod);
        con.setDoOutput(true);

        beforeRequest.apply(con);

        int responseCode = con.getResponseCode();

        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {

            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
        }

        return new HttpRequestorResponse(responseCode, response.toString());
    }
    public static HttpRequestorResponse PostRequest(final String requestUrl, Supplier<String> postContent) throws Exception {
        return HttpRequestor.DoRequest(requestUrl, "POST", con -> {
            OutputStream os = con.getOutputStream();
            byte[] input = postContent.get().getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
            return null;
        });
    }
}
