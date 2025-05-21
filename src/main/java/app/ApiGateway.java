package app;

import io.javalin.Javalin;
import io.javalin.http.Context;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;

public class ApiGateway {
    private static final String PROPERTY_SERVICE_URL = "http://localhost:7001";
    private static final String ANALYTICS_SERVICE_URL = "http://localhost:7002";
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public ApiGateway() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public void start() {
        Javalin app = Javalin.create(config -> {
            config.plugins.enableCors(cors -> {
                cors.add(it -> it.anyHost());
            });
        }).start(7000);

        // Property endpoints
        app.get("/properties/{id}", this::getProperty);
        app.get("/properties", this::getAllProperties);
        app.post("/properties", this::createProperty);

        // Analytics endpoints
        app.get("/analytics/sales/{id}", this::getSalesAnalytics);
        app.get("/analytics/postcode/{postcode}", this::getPostcodeAnalytics);
    }

    private void getProperty(Context ctx) {
        String id = ctx.pathParam("id");
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(PROPERTY_SERVICE_URL + "/properties/" + id))
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // Forward the request to analytics service
            HttpRequest analyticsRequest = HttpRequest.newBuilder()
                .uri(URI.create(ANALYTICS_SERVICE_URL + "/analytics/sales/" + id))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
            httpClient.send(analyticsRequest, HttpResponse.BodyHandlers.ofString());

            ctx.status(response.statusCode()).result(response.body());
        } catch (Exception e) {
            ctx.status(500).result("Error: " + e.getMessage());
        }
    }

    private void getAllProperties(Context ctx) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(PROPERTY_SERVICE_URL + "/properties"))
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            ctx.status(response.statusCode()).result(response.body());
        } catch (Exception e) {
            ctx.status(500).result("Error: " + e.getMessage());
        }
    }

    private void createProperty(Context ctx) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(PROPERTY_SERVICE_URL + "/properties"))
                .POST(HttpRequest.BodyPublishers.ofString(ctx.body()))
                .header("Content-Type", "application/json")
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            ctx.status(response.statusCode()).result(response.body());
        } catch (Exception e) {
            ctx.status(500).result("Error: " + e.getMessage());
        }
    }

    private void getSalesAnalytics(Context ctx) {
        String id = ctx.pathParam("id");
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ANALYTICS_SERVICE_URL + "/analytics/sales/" + id))
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            ctx.status(response.statusCode()).result(response.body());
        } catch (Exception e) {
            ctx.status(500).result("Error: " + e.getMessage());
        }
    }

    private void getPostcodeAnalytics(Context ctx) {
        String postcode = ctx.pathParam("postcode");
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ANALYTICS_SERVICE_URL + "/analytics/postcode/" + postcode))
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            ctx.status(response.statusCode()).result(response.body());
        } catch (Exception e) {
            ctx.status(500).result("Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        new ApiGateway().start();
    }
}