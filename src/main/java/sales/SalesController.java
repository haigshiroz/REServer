package sales;

import io.javalin.http.Context;
import io.javalin.openapi.*;

import java.util.List;
import java.util.Optional;

public class SalesController {

    private SalesDAO homeSales;

    public SalesController(SalesDAO homeSales) {
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

        Optional<HomeSale> sale = homeSales.getSaleById(id);
        sale.map(ctx::json).orElseGet(() -> error(ctx, "Sale not found", 404));

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
    public void findSaleByarea_type(Context ctx, String area_type) {
        List<HomeSale> sales = homeSales.getSalesByarea_type(area_type);
        if (sales.isEmpty()) {
            ctx.result("No sales found for this area type");

            ctx.status(404);
        } else {
            ctx.json(sales);
            ctx.status(200);
        }
    }

    @OpenApi(
        path = "/sales/{minPrice}/{maxPrice}",
        methods = {HttpMethod.GET},
        summary = "Get sales by price range",
        operationId = "findSalesByPurchasePrice",
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
    public void findSalesBypurchasePrice(Context ctx, String purchase_price, String purchase_price2) {
        List<HomeSale> sales = homeSales.getSalesBypurchasePrice(purchase_price, purchase_price2);
        if (sales.isEmpty()) {
            ctx.result("No sales between the given price range");
            ctx.status(404);
        } else {
            ctx.json(sales);
            ctx.status(200);
        }
    }

    private Context error(Context ctx, String msg, int code) {
        ctx.result(msg);
        ctx.status(code);
        return ctx;
    }
}
