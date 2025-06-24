package main.com.apod.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import com.fasterxml.jackson.databind.ObjectMapper;

import main.com.apod.model.ApodResponse;

public class NasaApiService {
    private static final String BASE_URL = "https://api.nasa.gov/planetary/apod";
    private final String apiKey;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public NasaApiService(String apiKey) {
        this.apiKey = apiKey;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public ApodResponse getApod(String date) throws IOException, InterruptedException {
        String requestUrl = buildRequestUrl(date);
        System.out.println("Making request to: " + requestUrl);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(requestUrl))
                .timeout(Duration.ofSeconds(15))
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(
                request, 
                HttpResponse.BodyHandlers.ofString()
        );

        System.out.println("Received response: " + response.statusCode());
        System.out.println("Response body: " + response.body());

        if (response.statusCode() != 200) {
            throw new IOException("NASA API request failed with status: " + response.statusCode() + 
                               ", response: " + response.body());
        }

        try {
            return objectMapper.readValue(response.body(), ApodResponse.class);
        } catch (Exception e) {
            System.err.println("Failed to parse JSON: " + e.getMessage());
            throw new IOException("Failed to parse NASA API response: " + e.getMessage());
        }
    }

    private String buildRequestUrl(String date) {
        StringBuilder urlBuilder = new StringBuilder(BASE_URL)
                .append("?api_key=").append(apiKey);
        
        if (date != null && !date.isEmpty()) {
            urlBuilder.append("&date=").append(date);
        }
        
        urlBuilder.append("&thumbs=true");
        
        return urlBuilder.toString();
    }
}