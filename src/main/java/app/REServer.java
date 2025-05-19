package app;

import io.javalin.Javalin;
import io.javalin.apibuilder.ApiBuilder;
import io.javalin.openapi.plugin.OpenApiPlugin;
import io.javalin.openapi.plugin.swagger.SwaggerPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sales.*;

public class REServer {
    private static final Logger LOG = LoggerFactory.getLogger(REServer.class);

    public static void main(String[] args) {
        // Use only CassandraDAO as DatabaseInterface
        DatabaseInterface salesDAO = new CassandraDAO();
        SalesController salesController = new SalesController(salesDAO);

        // Create Javalin instance
        Javalin app = Javalin.create(config -> {
            // Configure OpenAPI plugin
            config.registerPlugin(new OpenApiPlugin(pluginConfig -> {
                pluginConfig.withDefinitionConfiguration((version, definition) -> {
                    definition.withOpenApiInfo(info -> {
                        info.setTitle("Real Estate API (Cassandra)");
                        info.setVersion("1.0");
                        info.setDescription("API for accessing real estate sales data in Australia");
                    });
                });
            }));

            // Register Swagger plugins
            config.registerPlugin(new SwaggerPlugin());

            // Define endpoints
            config.router.apiBuilder(() -> {
                ApiBuilder.path("sales", () -> {
                    ApiBuilder.get(salesController::getAllSales);
                    ApiBuilder.post(salesController::createSale);
                    ApiBuilder.path("{saleID}", () -> {
                        ApiBuilder.get(ctx -> salesController.getSaleByID(ctx, ctx.pathParam("saleID")));
                    });
                    ApiBuilder.path("postcode/{postcodeID}", () -> {
                        ApiBuilder.get(ctx -> salesController.findSaleByPostCode(ctx, ctx.pathParam("postcodeID")));
                    });
                    ApiBuilder.path("area_type/{areaType}", () -> {
                        ApiBuilder.get(ctx -> salesController.findSaleByAreaType(ctx, ctx.pathParam("areaType")));
                    });
                    ApiBuilder.path("price/{minPrice}/{maxPrice}", () -> {
                        ApiBuilder.get(ctx -> salesController.findSalesByPriceRange(ctx, ctx.pathParam("minPrice"), ctx.pathParam("maxPrice")));
                    });

                    // Statistics endpoints
                    ApiBuilder.path("stats", () -> {
                        ApiBuilder.path("sales/{id}", () -> {
                            ApiBuilder.get(ctx -> salesController.getSaleViews(ctx, ctx.pathParam("id")));
                        });
                        ApiBuilder.path("postcode/{postcode}", () -> {
                            ApiBuilder.get(ctx -> salesController.getPostcodeViews(ctx, ctx.pathParam("postcode")));
                        });
                    });
                });
            });
        }).start(7070);

        LOG.info("Server started at http://localhost:7070/");
        LOG.info("Swagger UI at http://localhost:7070/swagger");
        LOG.info("Using database: Cassandra");

        // Add shutdown hook to close database connection
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            salesDAO.close();
            LOG.info("Server stopped");
        }));
    }
}