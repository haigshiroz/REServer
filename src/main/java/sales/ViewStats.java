package sales;

public class ViewStats {
    private String id;          // property ID or postcode
    private long viewCount;     // Changed to long to match Cassandra counter type

    public ViewStats(String id, long viewCount) {
        this.id = id;
        this.viewCount = viewCount;
    }

    public String getId() {
        return id;
    }

    public long getViewCount() {
        return viewCount;
    }
}