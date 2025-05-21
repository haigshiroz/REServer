package app;

import io.javalin.Javalin;
import io.javalin.http.Context;
import com.fasterxml.jackson.databind.ObjectMapper;
import sales.SalesDAO;
import sales.ViewStats;

public class AnalyticsService {
    private final SalesDAO salesDAO;
    private final ObjectMapper objectMapper;

    public AnalyticsService() {
        this.salesDAO = new SalesDAO();
        this.objectMapper = new ObjectMapper();
    }

    public void start() {
        Javalin app = Javalin.create(config -> {
            // No plugins needed for internal service
        }).start(7002);

        // Analytics endpoints
        app.get("/sales/stats/sales/{id}", this::getSaleViews);
        app.get("/sales/stats/postcode/{postcode}", this::getPostcodeViews);
    }

    private void getSaleViews(Context ctx) {
        String id = ctx.pathParam("id");
        try {
            ViewStats stats = salesDAO.getViewStats(id);
            ctx.json(stats);
        } catch (Exception e) {
            ctx.status(500).result("Error: " + e.getMessage());
        }
    }

    private void getPostcodeViews(Context ctx) {
        String postcode = ctx.pathParam("postcode");
        try {
            ViewStats stats = salesDAO.getViewStats(postcode);
            ctx.json(stats);
        } catch (Exception e) {
            ctx.status(500).result("Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        new AnalyticsService().start();
    }
}
