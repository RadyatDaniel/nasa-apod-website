package main.com.apod;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Executors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import main.com.apod.model.ApodResponse;
import main.com.apod.service.NasaApiService;

public class ApodApplication {
   
    private static final int PORT = 8080;
    private static final String PROJECT_ROOT = Paths.get(System.getProperty("user.dir")).getParent().toString();
    private static final String FRONTEND_DIR = Paths.get(PROJECT_ROOT, "frontend").toString();
    private static final String NASA_API_KEY = "JrkWpXt53vb33k0tQXfQTSCOpvEAEbyuTWeFzE6R";
    private static final NasaApiService nasaApiService = new NasaApiService(NASA_API_KEY);

    public static void main(String[] args) throws IOException {
        System.out.println("NASA APOD Application Starting...");
        
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        
        // API endpoint
        server.createContext("/api/apod", new ApodApiHandler());
        
        // Serve index.html from project root
        server.createContext("/", exchange -> {
            Path filePath = Paths.get(PROJECT_ROOT, "index.html");
            serveFile(exchange, filePath, "text/html");
        });
        
        // Serve login.html from frontend folder
        server.createContext("/login.html", exchange -> {
            Path filePath = Paths.get(FRONTEND_DIR, "login.html");
            serveFile(exchange, filePath, "text/html");
        });
        
        // Serve home.html from frontend folder
        server.createContext("/home.html", exchange -> {
            Path filePath = Paths.get(FRONTEND_DIR, "home.html");
            serveFile(exchange, filePath, "text/html");
        });
        
        // Serve static files from frontend/static
        server.createContext("/static/", exchange -> {
            String requestPath = exchange.getRequestURI().getPath().substring("/static/".length());
            Path filePath = Paths.get(FRONTEND_DIR, "static", requestPath);
            
            String contentType = "application/octet-stream";
            if (requestPath.endsWith(".css")) contentType = "text/css";
            else if (requestPath.endsWith(".js")) contentType = "application/javascript";
            else if (requestPath.endsWith(".png")) contentType = "image/png";
            else if (requestPath.endsWith(".jpg") || requestPath.endsWith(".jpeg")) contentType = "image/jpeg";
            
            serveFile(exchange, filePath, contentType);
        });
        
        server.setExecutor(Executors.newFixedThreadPool(10));
        server.start();
        System.out.println("Server running on http://localhost:" + PORT);
    }

    private static void serveFile(HttpExchange exchange, Path filePath, String contentType) throws IOException {
        if (!Files.exists(filePath)) {
            String response = "404 Not Found";
            exchange.sendResponseHeaders(404, response.length());
            exchange.getResponseBody().write(response.getBytes());
            return;
        }
        
        byte[] fileData = Files.readAllBytes(filePath);
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(200, fileData.length);
        exchange.getResponseBody().write(fileData);
        exchange.close();
    }

    static class ApodApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                String query = exchange.getRequestURI().getQuery();
                String date = extractDateParameter(query);
                
                ApodResponse apodData = nasaApiService.getApod(date);
                String jsonResponse = new ObjectMapper().writeValueAsString(apodData);
                
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                exchange.sendResponseHeaders(200, jsonResponse.length());
                exchange.getResponseBody().write(jsonResponse.getBytes());
            } catch (Exception e) {
                String error = "{\"error\":\"" + e.getMessage() + "\"}";
                exchange.sendResponseHeaders(500, error.length());
                exchange.getResponseBody().write(error.getBytes());
            } finally {
                exchange.close();
            }
        }
        
        private String extractDateParameter(String query) {
            if (query == null) return "";
            for (String param : query.split("&")) {
                if (param.startsWith("date=")) {
                    return param.substring(5);
                }
            }
            return "";
        }
    }
}