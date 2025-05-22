package app;

import io.javalin.Javalin;
import io.javalin.http.Context;
import com.fasterxml.jackson.databind.ObjectMapper;
import sales.HomeSale;
import sales.SalesDAO;
import app.kafka.PropertyProducer;
import java.util.List;

public class PropertyService {
    private final SalesDAO salesDAO;
    private final ObjectMapper objectMapper;
    private final PropertyProducer propertyProducer;

    public PropertyService() {
        this.salesDAO = new SalesDAO();
        this.objectMapper = new ObjectMapper();
        this.propertyProducer = new PropertyProducer();
    }

    public void start() {
        Javalin app = Javalin.create(config -> {
            // No plugins needed for internal service
        }).start(7001);

        // Sales endpoints
        app.get("/sales", this::getAllSales);
        app.post("/sales", this::createSale);
        app.get("/sales/{saleID}", this::getSaleByID);
        app.get("/sales/postcode/{postcodeID}", this::findSaleByPostCode);
        app.get("/sales/area_type/{area_type}", this::findSaleByAreaType);
        app.get("/sales/{minPrice}/{maxPrice}", this::findSalesByPriceRange);
    }

    private void getAllSales(Context ctx) {
        try {
            List<HomeSale> sales = salesDAO.getAllSales();
            ctx.json(sales);
        } catch (Exception e) {
            ctx.status(500).result("Error: " + e.getMessage());
        }
    }

    private void createSale(Context ctx) {
        try {
            HomeSale sale = objectMapper.readValue(ctx.body(), HomeSale.class);
            if (salesDAO.newSale(sale)) {
                // Send event to Kafka
                propertyProducer.sendPropertyEvent(sale);
                ctx.status(201).json(sale);
            } else {
                ctx.status(400).result("Failed to create sale");
            }
        } catch (Exception e) {
            ctx.status(400).result("Invalid sale data: " + e.getMessage());
        }
    }

    private void getSaleByID(Context ctx) {
        String id = ctx.pathParam("saleID");
        try {
            var sale = salesDAO.getSaleById(id);
            if (sale.isPresent()) {
                ctx.json(sale.get());
            } else {
                ctx.status(404).result("Sale not found");
            }
        } catch (Exception e) {
            ctx.status(500).result("Error: " + e.getMessage());
        }
    }

    private void findSaleByPostCode(Context ctx) {
        String postcode = ctx.pathParam("postcodeID");
        try {
            List<HomeSale> sales = salesDAO.getSalesByPostCode(postcode);
            ctx.json(sales);
        } catch (Exception e) {
            ctx.status(500).result("Error: " + e.getMessage());
        }
    }

    private void findSaleByAreaType(Context ctx) {
        String areaType = ctx.pathParam("area_type");
        try {
            List<HomeSale> sales = salesDAO.getSalesByarea_type(areaType);
            ctx.json(sales);
        } catch (Exception e) {
            ctx.status(500).result("Error: " + e.getMessage());
        }
    }

    private void findSalesByPriceRange(Context ctx) {
        String minPrice = ctx.pathParam("minPrice");
        String maxPrice = ctx.pathParam("maxPrice");
        try {
            List<HomeSale> sales = salesDAO.getSalesBypurchasePrice(minPrice, maxPrice);
            ctx.json(sales);
        } catch (Exception e) {
            ctx.status(500).result("Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        new PropertyService().start();
    }
}
