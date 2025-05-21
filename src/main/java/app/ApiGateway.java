package app;

import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.openapi.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import sales.HomeSale;
import sales.ViewStats;

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
            // Add OpenAPI plugin
            config.registerPlugin(new OpenApiPlugin(pluginConfig -> {
                pluginConfig.withDefinitionConfiguration((version, definition) -> {
                    definition.withOpenApiInfo(info -> {
                        info.setTitle("Real Estate API Gateway");
                        info.setVersion("1.0");
                        info.setDescription("API Gateway for Real Estate Microservices");
                    });
                });
            }));
            // Add Swagger plugin
            config.registerPlugin(new SwaggerPlugin());
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

    @OpenApi(
        path = "/sales",
        methods = {HttpMethod.GET},
        summary = "Get all sales",
        operationId = "getAllSales",
        tags = {"Sales"},
        responses = {
            @OpenApiResponse(
                status = "200",
                content = @OpenApiContent(from = HomeSale[].class),
                description = "List of all sales"
            ),
            @OpenApiResponse(status = "404", description = "No sales found")
        }
    )
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

    @OpenApi(
        path = "/sales",
        methods = {HttpMethod.POST},
        summary = "Create a new sale",
        operationId = "createSale",
        tags = {"Sales"},
        requestBody = @OpenApiRequestBody(
            content = @OpenApiContent(from = HomeSale.class),
            required = true,
            description = "Sale details"
        ),
        responses = {
            @OpenApiResponse(status = "201", description = "Sale created successfully"),
            @OpenApiResponse(status = "400", description = "Invalid sale data")
        }
    )
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

    @OpenApi(
        path = "/sales/{saleID}",
        methods = {HttpMethod.GET},
        summary = "Get a sale by ID",
        operationId = "getSaleByID",
        tags = {"Sales"},
        pathParams = {
            @OpenApiParam(name = "saleID", description = "The ID of the sale to retrieve")
        },
        responses = {
            @OpenApiResponse(
                status = "200",
                content = @OpenApiContent(from = HomeSale.class),
                description = "The requested sale"
            ),
            @OpenApiResponse(status = "404", description = "Sale not found")
        }
    )
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

    @OpenApi(
        path = "/sales/postcode/{postcodeID}",
        methods = {HttpMethod.GET},
        summary = "Get sales by postcode",
        operationId = "findSaleByPostCode",
        tags = {"Sales"},
        pathParams = {
            @OpenApiParam(name = "postcodeID", description = "The postcode to search for")
        },
        responses = {
            @OpenApiResponse(
                status = "200",
                content = @OpenApiContent(from = HomeSale[].class),
                description = "List of sales in the postcode"
            ),
            @OpenApiResponse(status = "404", description = "No sales found for postcode")
        }
    )
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

    @OpenApi(
        path = "/sales/area_type/{area_type}",
        methods = {HttpMethod.GET},
        summary = "Get sales by area type",
        operationId = "findSaleByAreaType",
        tags = {"Sales"},
        pathParams = {
            @OpenApiParam(name = "area_type", description = "The area type to search for")
        },
        responses = {
            @OpenApiResponse(
                status = "200",
                content = @OpenApiContent(from = HomeSale[].class),
                description = "List of sales for the area type"
            ),
            @OpenApiResponse(status = "404", description = "No sales found for area type")
        }
    )
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

    @OpenApi(
        path = "/sales/{minPrice}/{maxPrice}",
        methods = {HttpMethod.GET},
        summary = "Get sales by price range",
        operationId = "findSalesByPriceRange",
        tags = {"Sales"},
        pathParams = {
            @OpenApiParam(name = "minPrice", description = "Minimum price"),
            @OpenApiParam(name = "maxPrice", description = "Maximum price")
        },
        responses = {
            @OpenApiResponse(
                status = "200",
                content = @OpenApiContent(from = HomeSale[].class),
                description = "List of sales in the price range"
            ),
            @OpenApiResponse(status = "404", description = "No sales found in price range")
        }
    )
    private void findSalesByPriceRange(Context ctx) {
        String purchase_price = ctx.pathParam("minPrice");
        String purchase_price2 = ctx.pathParam("maxPrice");
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(PROPERTY_SERVICE_URL + "/sales/" + purchase_price + "/" + purchase_price2))
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            ctx.status(response.statusCode()).result(response.body());
        } catch (Exception e) {
            ctx.status(500).result("Error: " + e.getMessage());
        }
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