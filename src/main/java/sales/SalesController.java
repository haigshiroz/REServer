package sales;

import io.javalin.http.Context;

import java.util.List;
import java.util.Optional;

public class SalesController {

    private SalesDAO homeSales;

    public SalesController(SalesDAO homeSales) {
        this.homeSales = homeSales;
    }

    // implements POST /sales
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

    // implements Get /sales
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

    // implements GET /sales/{saleID}
    public void getSaleByID(Context ctx, String id) {

        Optional<HomeSale> sale = homeSales.getSaleById(id);
        sale.map(ctx::json).orElseGet(() -> error(ctx, "Sale not found", 404));

    }

    // Implements GET /sales/postcode/{postcodeID}
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

    // Implements Get /sales/area_type/{area_type}
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


    // Implements GET /sales/{minPrice}/{maxPrice}
    public void findSalesBypurchase_price(Context ctx, String purchase_price, String purchase_price2) {
        List<HomeSale> sales = homeSales.getSalesBypurchase_price(purchase_price, purchase_price2);
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
