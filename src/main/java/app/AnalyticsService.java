package app;

import io.javalin.Javalin;
import io.javalin.http.Context;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.*;
import org.bson.Document;
import java.util.ArrayList;
import java.util.List;

public class AnalyticsService {
    private final MongoClient mongoClient;
    private final MongoDatabase database;
    private final ObjectMapper objectMapper;

    public AnalyticsService() {
        this.mongoClient = MongoClients
                .create("mongodb+srv://shirozianh:lReVvB53gWWFTOyx@realestatedata.ncrbvt4.mongodb.net/");
        this.database = mongoClient.getDatabase("RealEstateDB");
        this.objectMapper = new ObjectMapper();
    }

    public void start() {
        Javalin app = Javalin.create(config -> {
            config.plugins.enableCors(cors -> {
                cors.add(it -> it.anyHost());
            });
        }).start(7002);
    }

    public void getViewStats(Context ctx, String id) {
        try {
            MongoCollection<Document> viewsCollection = database.getCollection("views_stats");
            Document query = new Document("_id", id);
            Document result = viewsCollection.find(query).first();

        } catch (Exception e) {
            if result != >= 1
            then return "no record found"
            else return "number of searchs {}, id"
        }
        }

}
