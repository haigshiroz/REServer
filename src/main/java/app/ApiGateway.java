package app;

import io.javalin.Javalin;
import io.javalin.http.Context;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import sales.HomeSale;
import sales.ViewStats;
import app.kafka.ViewProducer;

public class ApiGateway {
    private static final String PROPERTY_SERVICE_URL = "http://localhost:7001";
    private static final String ANALYTICS_SERVICE_URL = "http://localhost:7002";
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final ViewProducer viewProducer;

    public ApiGateway() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.viewProducer = new ViewProducer();
    }

    public void start() {
        Javalin app = Javalin.create().start(7003);

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

    public void getAllSales(Context ctx) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(PROPERTY_SERVICE_URL + "/sales"))
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                // Parse and reformat the JSON
                Object json = objectMapper.readValue(response.body(), Object.class);
                String formattedJson = objectMapper.writeValueAsString(json);
                ctx.result(formattedJson);
            }
            ctx.status(response.statusCode());
        } catch (Exception e) {
            ctx.status(500).result("Error: " + e.getMessage());
        }
    }

    public void createSale(Context ctx) {
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

    public void getSaleByID(Context ctx) {
        String id = ctx.pathParam("saleID");
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(PROPERTY_SERVICE_URL + "/sales/" + id))
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // Send view event to Kafka
            viewProducer.sendViewEvent(id, false);

            if (response.statusCode() == 200) {
                // Parse and reformat the JSON
                Object json = objectMapper.readValue(response.body(), Object.class);
                String formattedJson = objectMapper.writeValueAsString(json);
                ctx.result(formattedJson);
            }
            ctx.status(response.statusCode());
        } catch (Exception e) {
            ctx.status(500).result("Error: " + e.getMessage());
        }
    }

    public void findSaleByPostCode(Context ctx) {
        String postcode = ctx.pathParam("postcodeID");
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(PROPERTY_SERVICE_URL + "/sales/postcode/" + postcode))
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // Send view event to Kafka
            viewProducer.sendViewEvent(postcode, true);

            if (response.statusCode() == 200) {
                // Parse and reformat the JSON
                Object json = objectMapper.readValue(response.body(), Object.class);
                String formattedJson = objectMapper.writeValueAsString(json);
                ctx.result(formattedJson);
            }
            ctx.status(response.statusCode());
        } catch (Exception e) {
            ctx.status(500).result("Error: " + e.getMessage());
        }
    }

    public void findSaleByAreaType(Context ctx) {
        String areaType = ctx.pathParam("area_type");
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(PROPERTY_SERVICE_URL + "/sales/area_type/" + areaType))
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                // Parse and reformat the JSON
                Object json = objectMapper.readValue(response.body(), Object.class);
                String formattedJson = objectMapper.writeValueAsString(json);
                ctx.result(formattedJson);
            }
            ctx.status(response.statusCode());
        } catch (Exception e) {
            ctx.status(500).result("Error: " + e.getMessage());
        }
    }

    public void findSalesByPriceRange(Context ctx) {
        String minPrice = ctx.pathParam("minPrice");
        String maxPrice = ctx.pathParam("maxPrice");
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(PROPERTY_SERVICE_URL + "/sales/" + minPrice + "/" + maxPrice))
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                // Parse and reformat the JSON
                Object json = objectMapper.readValue(response.body(), Object.class);
                String formattedJson = objectMapper.writeValueAsString(json);
                ctx.result(formattedJson);
            }
            ctx.status(response.statusCode());
        } catch (Exception e) {
            ctx.status(500).result("Error: " + e.getMessage());
        }
    }

    public void getSaleViews(Context ctx) {
        String id = ctx.pathParam("id");
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ANALYTICS_SERVICE_URL + "/sales/stats/sales/" + id))
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                // Parse and reformat the JSON
                Object json = objectMapper.readValue(response.body(), Object.class);
                String formattedJson = objectMapper.writeValueAsString(json);
                ctx.result(formattedJson);
            }
            ctx.status(response.statusCode());
        } catch (Exception e) {
            ctx.status(500).result("Error: " + e.getMessage());
        }
    }

    public void getPostcodeViews(Context ctx) {
        String postcode = ctx.pathParam("postcode");
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ANALYTICS_SERVICE_URL + "/sales/stats/postcode/" + postcode))
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                // Parse and reformat the JSON
                Object json = objectMapper.readValue(response.body(), Object.class);
                String formattedJson = objectMapper.writeValueAsString(json);
                ctx.result(formattedJson);
            }
            ctx.status(response.statusCode());
        } catch (Exception e) {
            ctx.status(500).result("Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        new ApiGateway().start();
    }
}