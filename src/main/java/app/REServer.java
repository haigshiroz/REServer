package app;

import io.javalin.Javalin;
import io.javalin.apibuilder.ApiBuilder;

import io.javalin.openapi.plugin.OpenApiPlugin;
import io.javalin.openapi.plugin.swagger.SwaggerPlugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sales.SalesDAO;
import sales.SalesController;

public class REServer {
    private static final Logger LOG = LoggerFactory.getLogger(REServer.class);

    public static void main(String[] args) {

        var sales = new SalesDAO();
        SalesController salesController = new SalesController(sales);

        // Create Javalin instance
        Javalin app = Javalin.create(config -> {
            // Configure OpenAPI plugin
            config.registerPlugin(new OpenApiPlugin(pluginConfig -> {
                pluginConfig.withDefinitionConfiguration((version, definition) -> {
                    definition.withOpenApiInfo(info -> {
                        info.setTitle("Real Estate API");
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
                    ApiBuilder.path("area_type/{area_type}", () -> {
                        ApiBuilder.get(ctx -> salesController.findSaleByarea_type(ctx, ctx.pathParam("area_type")));
                    });
                    ApiBuilder.path("{minPrice}/{maxPrice}", () -> {
                        ApiBuilder.get(ctx -> salesController.findSalesBypurchasePrice(
                            ctx,
                            ctx.pathParam("minPrice"),
                            ctx.pathParam("maxPrice")
                        ));
                    });
                });
            });
        }).start(7070);

        LOG.info("Server started at http://localhost:7070/");
        LOG.info("Swagger UI at http://localhost:7070/swagger");
    }
}