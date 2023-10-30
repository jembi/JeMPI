package org.jembi.jempi.monitor.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

public class HttpRequestor {

    public record HttpRequestorResponse(int responseCode, String response){}
    public static HttpRequestorResponse PostRequest(final String requestUrl, Supplier<String> postContent) throws Exception {
        URL url = new URL(requestUrl);
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

        return new HttpRequestorResponse(responseCode, response.toString());
    }
}
