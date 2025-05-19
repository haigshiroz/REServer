package sales;

import io.javalin.http.Context;
import io.javalin.openapi.*;

import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SalesController {

    private DatabaseInterface homeSales;
    private static final Logger LOG = LoggerFactory.getLogger(SalesController.class);

    public SalesController(DatabaseInterface homeSales) {
        this.homeSales = homeSales;
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
    public void createSale(Context ctx) {

        // Extract Home Sale from request body
        // TO DO override Validator exception method to report better error message
        HomeSale sale = ctx.bodyValidator(HomeSale.class).get();

        // store new sale in data set
        if (homeSales.newSale(sale)) {
            ctx.result("Sale Created");
            ctx.status(201);
        } else {
            ctx.result("Failed to add sale");
            ctx.status(400);
        }
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
    public void getAllSales(Context ctx) {
        List<HomeSale> allSales = homeSales.getAllSales();
        if (allSales.isEmpty()) {
            ctx.result("No Sales Found");
            ctx.status(404);
        } else {
            ctx.json(allSales);
            ctx.status(200);
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
    public void getSaleByID(Context ctx, String id) {
        // Track the view before getting the sale
        homeSales.incrementViews(id);
        LOG.info("Incrementing view count for sale ID: {}", id);

        Optional<HomeSale> sale = homeSales.getSaleById(id);
        if (sale.isPresent()) {
            ctx.json(sale.get());
            ctx.status(200);
        } else {
            error(ctx, "Sale not found", 404);
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
    public void findSaleByPostCode(Context ctx, String postCode) {
        // Track the postcode search before getting the sales
        homeSales.incrementViews(postCode);
        LOG.info("Incrementing view count for postcode: {}", postCode);

        List<HomeSale> sales = homeSales.getSalesByPostCode(postCode);
        if (sales.isEmpty()) {
            ctx.result("No sales for postcode found");
            ctx.status(404);
        } else {
            ctx.json(sales);
            ctx.status(200);
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
    public void findSaleByAreaType(Context ctx, String areaType) {
        List<HomeSale> sales = homeSales.getSalesByAreaType(areaType);
        if (sales.isEmpty()) {
            ctx.result("No sales found for this area type");
            ctx.status(404);
        } else {
            ctx.json(sales);
            ctx.status(200);
        }
    }

    @OpenApi(
        path = "/sales/price/{minPrice}/{maxPrice}",
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
    public void findSalesByPriceRange(Context ctx, String minPrice, String maxPrice) {
        try {
            List<HomeSale> sales = homeSales.getSalesByPriceRange(minPrice, maxPrice);
            if (sales.isEmpty()) {
                ctx.result("No sales between the given price range");
                ctx.status(404);
            } else {
                ctx.json(sales);
                ctx.status(200);
            }
        } catch (NumberFormatException e) {
            ctx.result("Invalid price format. Please provide valid numbers.");
            ctx.status(400);
        } catch (Exception e) {
            ctx.result("Error processing request: " + e.getMessage());
            ctx.status(500);
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
    public void getSaleViews(Context ctx, String id) {
        ViewStats stats = homeSales.getViewStats(id);
        LOG.info("Retrieved view stats for sale ID {}: {} views", id, stats.getViewCount());
        ctx.json(stats);
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
    public void getPostcodeViews(Context ctx, String postcode) {
        ViewStats stats = homeSales.getViewStats(postcode);
        LOG.info("Retrieved view stats for postcode {}: {} views", postcode, stats.getViewCount());
        ctx.json(stats);
    }

    private Context error(Context ctx, String msg, int code) {
        ctx.result(msg);
        ctx.status(code);
        return ctx;
    }
}
