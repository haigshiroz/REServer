package sales;

public class ViewStats {
    private String id;          // property ID or postcode
    private int viewCount;

    public ViewStats(String id, int viewCount) {
        this.id = id;
        this.viewCount = viewCount;
    }

    public String getId() {
        return id;
    }

    public int getViewCount() {
        return viewCount;
    }

    public void incrementViewCount() {
        this.viewCount++;
    }
}