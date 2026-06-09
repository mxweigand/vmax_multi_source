package com.vmax.vmax_multi_source;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.json.JSONArray;
import org.json.JSONObject;

public class RestClient {

    private HttpClient httpClient;
    private URI targetUriAbox;
    private URI targetUriTbox;

    public RestClient(Integer port) {
        httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(10))
            .build();
        try {
            this.targetUriAbox = new URI("http://localhost:" + port + "/triple");
            this.targetUriTbox = new URI("http://localhost:" + port + "/tbox");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    /**
     * Request triples matching a triple pattern.
     * Request body: JSONObject {"subject": node, "predicate": node, "object": node}
     * Response:     JSONArray of JSONObject triples, or [] if empty
     */
    public JSONArray getAboxTriples(JSONObject requestJson) {
        String requestBody = requestJson.toString();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(targetUriAbox)
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .header("Content-Type", "application/json")
            .build();
        HttpResponse<String> response = null;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
        return new JSONArray(response.body());
    }

    /**
     * Retrieve complete tbox.
     * Response: JSONArray of JSONObject triples, or [] if empty
     */
    public JSONArray getTboxTriples() {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(targetUriTbox)
            .GET()
            .build();
        HttpResponse<String> response = null;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return new JSONArray(response.body());
    }
}
