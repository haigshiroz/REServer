package sales;

import io.javalin.http.Context;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.util.List;
import java.util.Optional;

public class SalesController {
    private SalesDAO homeSales;
    private final ObjectMapper objectMapper;

    public SalesController(SalesDAO homeSales) {
        this.homeSales = homeSales;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public void createSale(Context ctx) {
        HomeSale sale = ctx.bodyValidator(HomeSale.class).get();

        if (homeSales.newSale(sale)) {
            ctx.result("Sale Created");
            ctx.status(201);
        } else {
            ctx.result("Failed to add sale");
            ctx.status(400);
        }
    }

    public void getAllSales(Context ctx) {
        List<HomeSale> allSales = homeSales.getAllSales();
        if (allSales.isEmpty()) {
            ctx.result("No Sales Found");
            ctx.status(404);
        } else {
            try {
                String jsonResponse = objectMapper.writeValueAsString(allSales);
                ctx.result(jsonResponse);
                ctx.status(200);
            } catch (Exception e) {
                ctx.result("Error formatting response: " + e.getMessage());
                ctx.status(500);
            }
        }
    }

    public void getSaleByID(Context ctx, String id) {
        homeSales.incrementViews(id);
        Optional<HomeSale> sale = homeSales.getSaleById(id);
        if (sale.isPresent()) {
            try {
                String jsonResponse = objectMapper.writeValueAsString(sale.get());
                ctx.result(jsonResponse);
                ctx.status(200);
            } catch (Exception e) {
                ctx.result("Error formatting response: " + e.getMessage());
                ctx.status(500);
            }
        } else {
            error(ctx, "Sale not found", 404);
        }
    }

    public void findSaleByPostCode(Context ctx, String postCode) {
        homeSales.incrementViews(postCode);
        List<HomeSale> sales = homeSales.getSalesByPostCode(postCode);
        if (sales.isEmpty()) {
            ctx.result("No sales for postcode found");
            ctx.status(404);
        } else {
            try {
                String jsonResponse = objectMapper.writeValueAsString(sales);
                ctx.result(jsonResponse);
                ctx.status(200);
            } catch (Exception e) {
                ctx.result("Error formatting response: " + e.getMessage());
                ctx.status(500);
            }
        }
    }

    public void findSaleByarea_type(Context ctx, String area_type) {
        List<HomeSale> sales = homeSales.getSalesByarea_type(area_type);
        if (sales.isEmpty()) {
            ctx.result("No sales found for this area type");
            ctx.status(404);
        } else {
            try {
                String jsonResponse = objectMapper.writeValueAsString(sales);
                ctx.result(jsonResponse);
                ctx.status(200);
            } catch (Exception e) {
                ctx.result("Error formatting response: " + e.getMessage());
                ctx.status(500);
            }
        }
    }

    public void findSalesBypurchasePrice(Context ctx, String purchase_price, String purchase_price2) {
        List<HomeSale> sales = homeSales.getSalesBypurchasePrice(purchase_price, purchase_price2);
        if (sales.isEmpty()) {
            ctx.result("No sales between the given price range");
            ctx.status(404);
        } else {
            try {
                String jsonResponse = objectMapper.writeValueAsString(sales);
                ctx.result(jsonResponse);
                ctx.status(200);
            } catch (Exception e) {
                ctx.result("Error formatting response: " + e.getMessage());
                ctx.status(500);
            }
        }
    }

    public void getSaleViews(Context ctx, String id) {
        ViewStats stats = homeSales.getViewStats(id);
        try {
            String jsonResponse = objectMapper.writeValueAsString(stats);
            ctx.result(jsonResponse);
            ctx.status(200);
        } catch (Exception e) {
            ctx.result("Error formatting response: " + e.getMessage());
            ctx.status(500);
        }
    }

    public void getPostcodeViews(Context ctx, String postcode) {
        ViewStats stats = homeSales.getViewStats(postcode);
        try {
            String jsonResponse = objectMapper.writeValueAsString(stats);
            ctx.result(jsonResponse);
            ctx.status(200);
        } catch (Exception e) {
            ctx.result("Error formatting response: " + e.getMessage());
            ctx.status(500);
        }
    }

    private Context error(Context ctx, String msg, int code) {
        ctx.result(msg);
        ctx.status(code);
        return ctx;
    }
}
