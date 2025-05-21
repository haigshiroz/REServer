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

        // Sales endpoints
        app.get("/sales", this::getAllSales);
        app.post("/sales", this::createSale);
        app.get("/sales/{saleID}", this::getSaleByID);
        app.get("/sales/postcode/{postcodeID}", this::findSaleByPostCode);
        app.get("/sales/area_type/{area_type}", this::findSaleByAreaType);
        app.get("/sales/{minPrice}/{maxPrice}", this::findSalesByPriceRange);

        // Analytics endpoints
        app.get("/sales/stats/sales/{id}", this::getSaleViews);
        app.get("/sales/stats/postcode/{postcode}", this::getPostcodeViews);
    }

    private void getAllSales(Context ctx) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(PROPERTY_SERVICE_URL + "/sales"))
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            ctx.status(response.statusCode()).result(response.body());
        } catch (Exception e) {
            ctx.status(500).result("Error: " + e.getMessage());
        }
    }

    private void createSale(Context ctx) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(PROPERTY_SERVICE_URL + "/sales"))
                .POST(HttpRequest.BodyPublishers.ofString(ctx.body()))
                .header("Content-Type", "application/json")
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            ctx.status(response.statusCode()).result(response.body());
        } catch (Exception e) {
            ctx.status(500).result("Error: " + e.getMessage());
        }
    }

    private void getSaleByID(Context ctx) {
        String id = ctx.pathParam("saleID");
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(PROPERTY_SERVICE_URL + "/sales/" + id))
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // Forward the request to analytics service
            HttpRequest analyticsRequest = HttpRequest.newBuilder()
                .uri(URI.create(ANALYTICS_SERVICE_URL + "/sales/stats/sales/" + id))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
            httpClient.send(analyticsRequest, HttpResponse.BodyHandlers.ofString());

            ctx.status(response.statusCode()).result(response.body());
        } catch (Exception e) {
            ctx.status(500).result("Error: " + e.getMessage());
        }
    }

    private void findSaleByPostCode(Context ctx) {
        String postcode = ctx.pathParam("postcodeID");
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(PROPERTY_SERVICE_URL + "/sales/postcode/" + postcode))
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // Forward the request to analytics service
            HttpRequest analyticsRequest = HttpRequest.newBuilder()
                .uri(URI.create(ANALYTICS_SERVICE_URL + "/sales/stats/postcode/" + postcode))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
            httpClient.send(analyticsRequest, HttpResponse.BodyHandlers.ofString());

            ctx.status(response.statusCode()).result(response.body());
        } catch (Exception e) {
            ctx.status(500).result("Error: " + e.getMessage());
        }
    }

    private void findSaleByAreaType(Context ctx) {
        String areaType = ctx.pathParam("area_type");
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(PROPERTY_SERVICE_URL + "/sales/area_type/" + areaType))
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            ctx.status(response.statusCode()).result(response.body());
        } catch (Exception e) {
            ctx.status(500).result("Error: " + e.getMessage());
        }
    }

    private void findSalesByPriceRange(Context ctx) {
        String minPrice = ctx.pathParam("minPrice");
        String maxPrice = ctx.pathParam("maxPrice");
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(PROPERTY_SERVICE_URL + "/sales/" + minPrice + "/" + maxPrice))
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            ctx.status(response.statusCode()).result(response.body());
        } catch (Exception e) {
            ctx.status(500).result("Error: " + e.getMessage());
        }
    }

    private void getSaleViews(Context ctx) {
        String id = ctx.pathParam("id");
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ANALYTICS_SERVICE_URL + "/sales/stats/sales/" + id))
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            ctx.status(response.statusCode()).result(response.body());
        } catch (Exception e) {
            ctx.status(500).result("Error: " + e.getMessage());
        }
    }

    private void getPostcodeViews(Context ctx) {
        String postcode = ctx.pathParam("postcode");
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ANALYTICS_SERVICE_URL + "/sales/stats/postcode/" + postcode))
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