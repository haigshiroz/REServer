package sales;

import java.util.List;
import java.util.Optional;

public interface DatabaseInterface {
    boolean newSale(HomeSale homeSale);
    Optional<HomeSale> getSaleById(String saleId);
    List<HomeSale> getSalesByPostCode(String postcode);
    List<HomeSale> getSalesByAreaType(String areaType);
    List<HomeSale> getSalesByPriceRange(String min, String max);
    List<HomeSale> getAllSales();
    void close();

    // View statistics methods
    void incrementViews(String id);
    ViewStats getViewStats(String id);
}