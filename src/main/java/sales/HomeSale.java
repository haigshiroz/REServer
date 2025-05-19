package sales;

// Simple class to provide test data in SalesDAO

public class HomeSale {
    private String id;
    private String saleID;
    private String postcode;
    private int salePrice;
    private String area_type;

    public HomeSale(String saleID, String postcode, int salePrice, String area_type) {
        this.id = saleID;  // Use saleID as the id
        this.saleID = saleID;
        this.postcode = postcode;
        this.salePrice = salePrice;
        this.area_type = area_type;
    }

    // needed for JSON conversion
    public HomeSale() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSaleID() {
        return saleID;
    }

    public void setSaleID(String saleID) {
        this.saleID = saleID;
    }

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public int getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(int salePrice) {
        this.salePrice = salePrice;
    }

    public String getarea_type() {
        return area_type;
    }

    public void setarea_type(String area_type) {
        this.area_type = area_type;
    }
}
