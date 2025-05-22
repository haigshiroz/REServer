package app;

import io.javalin.Javalin;
import io.javalin.http.Context;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import sales.SalesDAO;
import sales.ViewStats;
import app.kafka.ViewConsumer;

public class AnalyticsService {
    private final SalesDAO salesDAO;
    private final ObjectMapper objectMapper;
    private final ViewConsumer viewConsumer;

    public AnalyticsService() {
        this.salesDAO = new SalesDAO();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.viewConsumer = new ViewConsumer(salesDAO);
        this.viewConsumer.startConsuming();
    }

    public void start() {
        Javalin app = Javalin.create(config -> {
            // No plugins needed for internal service
        }).start(7002);

        // Analytics endpoints
        app.get("/sales/stats/sales/{id}", this::getSaleViews);
        app.get("/sales/stats/postcode/{postcode}", this::getPostcodeViews);

        // Add POST endpoints for incrementing views
        app.post("/sales/stats/sales/{id}", this::incrementSaleViews);
        app.post("/sales/stats/postcode/{postcode}", this::incrementPostcodeViews);
    }

    private void getSaleViews(Context ctx) {
        String id = ctx.pathParam("id");
        try {
            ViewStats stats = salesDAO.getViewStats(id);
            String formattedJson = objectMapper.writeValueAsString(stats);
            ctx.result(formattedJson);
            ctx.status(200);
        } catch (Exception e) {
            ctx.status(500).result("Error: " + e.getMessage());
        }
    }

    private void getPostcodeViews(Context ctx) {
        String postcode = ctx.pathParam("postcode");
        try {
            ViewStats stats = salesDAO.getViewStats(postcode);
            String formattedJson = objectMapper.writeValueAsString(stats);
            ctx.result(formattedJson);
            ctx.status(200);
        } catch (Exception e) {
            ctx.status(500).result("Error: " + e.getMessage());
        }
    }

    private void incrementSaleViews(Context ctx) {
        String id = ctx.pathParam("id");
        try {
            salesDAO.incrementViews(id);
            ViewStats stats = salesDAO.getViewStats(id);
            String formattedJson = objectMapper.writeValueAsString(stats);
            ctx.result(formattedJson);
            ctx.status(200);
        } catch (Exception e) {
            ctx.status(500).result("Error: " + e.getMessage());
        }
    }

    private void incrementPostcodeViews(Context ctx) {
        String postcode = ctx.pathParam("postcode");
        try {
            salesDAO.incrementViews(postcode);
            ViewStats stats = salesDAO.getViewStats(postcode);
            String formattedJson = objectMapper.writeValueAsString(stats);
            ctx.result(formattedJson);
            ctx.status(200);
        } catch (Exception e) {
            ctx.status(500).result("Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        new AnalyticsService().start();
    }
}
