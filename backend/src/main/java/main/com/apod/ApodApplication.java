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
   
    public static void main(String[] args) throws IOException {
        System.out.println("NASA APOD Application Starting...");
        
        // Get port from environment variable or use default 8080
        int port = System.getenv("PORT") != null ? Integer.parseInt(System.getenv("PORT")) : 8080;
        String frontendDir = System.getenv("FRONTEND_DIR") != null ? System.getenv("FRONTEND_DIR") 
                         : Paths.get(System.getProperty("user.dir"), "../frontend").toString();
        
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        NasaApiService nasaApiService = new NasaApiService(System.getenv("NASA_API_KEY"));
        
        // API endpoint
        server.createContext("/api/apod", new ApodApiHandler(nasaApiService));
        
        // Static files
       // In the main method, update the file paths:
server.createContext("/", exchange -> {
    Path filePath = Paths.get(frontendDir, "index.html");
    serveFile(exchange, filePath, "text/html");
});

server.createContext("/home.html", exchange -> {
    Path filePath = Paths.get(frontendDir, "home.html");
    serveFile(exchange, filePath, "text/html");
});
        
        server.createContext("/login.html", exchange -> {
            Path filePath = Paths.get(frontendDir, "login.html");
            serveFile(exchange, filePath, "text/html");
        });
        
        server.createContext("/static/", exchange -> {
            String requestPath = exchange.getRequestURI().getPath().substring("/static/".length());
            Path filePath = Paths.get(frontendDir, "static", requestPath);
            String contentType = getContentType(requestPath);
            serveFile(exchange, filePath, contentType);
        });
        
        server.setExecutor(Executors.newFixedThreadPool(10));
        server.start();
        System.out.println("Server running on port " + port);
    }

    private static String getContentType(String filename) {
        if (filename.endsWith(".css")) return "text/css";
        if (filename.endsWith(".js")) return "application/javascript";
        if (filename.endsWith(".png")) return "image/png";
        if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) return "image/jpeg";
        return "application/octet-stream";
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
        private final NasaApiService nasaApiService;
        
        public ApodApiHandler(NasaApiService nasaApiService) {
            this.nasaApiService = nasaApiService;
        }
        
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                String query = exchange.getRequestURI().getQuery();
                String date = extractDateParameter(query);
                
                ApodResponse apodData = nasaApiService.getApod(date);
                String jsonResponse = new ObjectMapper().writeValueAsString(apodData);
                
                exchange.getResponseHeaders().set("Content-Type", "application/json");
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