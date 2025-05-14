package sales;

import org.bson.types.ObjectId;

// Simple class to provide test data in SalesDAO

public class HomeSale {
    public ObjectId id;
    public String saleID;
    public String postcode;
    public String salePrice;
    public String area_type;

    public HomeSale(String saleID, String postcode, String salePrice, String area_type) {
        this.id = new ObjectId();
        this.saleID = saleID;
        this.postcode = postcode;
        this.salePrice = salePrice;
        this.area_type = area_type;
    }

    // needed for JSON conversion
    public HomeSale() {
        this.id = new ObjectId();
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
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

    public String getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(String salePrice) {
        this.salePrice = salePrice;
    }

    public String getarea_type() {
        return area_type;
    }

    public void setarea_type(String area_type) {
        this.area_type = area_type;
    }
}
