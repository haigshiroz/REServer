package app;

import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.openapi.*;
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
            config.plugins.enableCors(cors -> {
                cors.add(it -> it.anyHost());
            });
            // Add OpenAPI plugin
            config.registerPlugin(new OpenApiPlugin(pluginConfig -> {
                pluginConfig.withDefinitionConfiguration((version, definition) -> {
                    definition.withOpenApiInfo(info -> {
                        info.setTitle("Real Estate Analytics Service");
                        info.setVersion("1.0");
                        info.setDescription("Analytics Service for Real Estate Microservices");
                    });
                });
            }));
            // Add Swagger plugin
            config.registerPlugin(new SwaggerPlugin());
        }).start(7002);

        // Analytics endpoints
        app.get("/sales/stats/sales/{id}", this::getSaleViews);
        app.get("/sales/stats/postcode/{postcode}", this::getPostcodeViews);
    }

    @OpenApi(
        path = "/sales/stats/sales/{id}",
        methods = {HttpMethod.GET},
        summary = "Get view count for a specific sale",
        operationId = "getSaleViews",
        tags = {"Statistics"},
        pathParams = {
            @OpenApiParam(name = "id", description = "The sale ID to get view count for")
        },
        responses = {
            @OpenApiResponse(
                status = "200",
                content = @OpenApiContent(from = ViewStats.class),
                description = "View count for the sale"
            )
        }
    )
    private void getSaleViews(Context ctx) {
        String id = ctx.pathParam("id");
        try {
            ViewStats stats = salesDAO.getViewStats(id);
            ctx.json(stats);
        } catch (Exception e) {
            ctx.status(500).result("Error: " + e.getMessage());
        }
    }

    @OpenApi(
        path = "/sales/stats/postcode/{postcode}",
        methods = {HttpMethod.GET},
        summary = "Get view count for a specific postcode",
        operationId = "getPostcodeViews",
        tags = {"Statistics"},
        pathParams = {
            @OpenApiParam(name = "postcode", description = "The postcode to get view count for")
        },
        responses = {
            @OpenApiResponse(
                status = "200",
                content = @OpenApiContent(from = ViewStats.class),
                description = "View count for the postcode"
            )
        }
    )
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
