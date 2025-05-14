package sales;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class SalesDAO {

    private final String DB_URL = "mongodb://localhost:27017/";
    // private final String DB_URL = "mongodb+srv://shirozianh:lReVvB53gWWFTOyx@realestatedata.ncrbvt4.mongodb.net/";


    public boolean newSale(HomeSale homeSale){
        // Establish connection
        try (MongoClient mongoClient = MongoClients.create(DB_URL)) {
            // Get the database
            MongoDatabase sampleTrainingDB = mongoClient.getDatabase("RealEstateDB");

            // Get the specific collection of residencies
            MongoCollection<Document> residenciesCollection = sampleTrainingDB.getCollection("residencies");

            // Add new residence
            Document homeSaleDocument = new Document("_id", new ObjectId(homeSale.saleID))
                                            .append("post_code", homeSale.postcode)
                                            .append("purchase_price", homeSale.salePrice);

            residenciesCollection.insertOne(homeSaleDocument);
            return true;
        } catch (Exception e) {
            System.err.println("Error in createResidence Dao: " + e.getMessage());
            return false;
        }
    }

    // returns Optional wrapping a HomeSale if id is found, empty Optional otherwise
    public Optional<HomeSale> getSaleById(String saleID) {
        try (MongoClient mongoClient = MongoClients.create(DB_URL)) {
            // Get the database
            MongoDatabase sampleTrainingDB = mongoClient.getDatabase("RealEstateDB");

            // Get the specific collection of residencies
            MongoCollection<Document> residenciesCollection = sampleTrainingDB.getCollection("residencies");

            // Find the document with the given saleID
            Document query = new Document("_id", new ObjectId(saleID));
            Document result = residenciesCollection.find(query).first();

            if (result != null) {
                // Map the result to a HomeSale object
                HomeSale homeSale = new HomeSale(
                    result.getObjectId("_id").toString(),
                    result.getString("post_code"),
                    result.getString("purchase_price"),
                    result.getString("area_type")
                );
                return Optional.of(homeSale);
            } else {
                return Optional.empty();
            }
        } catch (Exception e) {
            System.err.println("Error in getSaleById Dao: " + e.getMessage());
            return Optional.empty();
        }
    }

    // returns a List of homesales  in a given postCode
    public List<HomeSale> getSalesByPostCode(String postCode) {
        try (MongoClient mongoClient = MongoClients.create(DB_URL)) {
            // Get the database
            MongoDatabase sampleTrainingDB = mongoClient.getDatabase("RealEstateDB");

            // Get the specific collection of residencies
            MongoCollection<Document> residenciesCollection = sampleTrainingDB.getCollection("residencies");

            // Query to find documents with the given postCode
            Document query = new Document("post_code", postCode);

            // Retrieve matching documents
            List<HomeSale> homeSales = new ArrayList<>();
            for (Document doc : residenciesCollection.find(query)) {
                // Map each document to a HomeSale object
                HomeSale homeSale = new HomeSale(
                    doc.getObjectId("_id").toString(),
                    doc.getString("post_code"),
                    doc.getString("purchase_price"),
                    doc.getString("area_type")
                );
                homeSales.add(homeSale);
            }
            return homeSales;
        } catch (Exception e) {
            System.err.println("Error in getSalesByPostCode Dao: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    // // returns the individual prices for all sales. Potentially large
    // public List<String> getAllSalePrices() {
    //     return   sales.stream()
    //             .map(e -> e.salePrice)
    //             .collect(Collectors.toList());
    // }

    // returns all home sales. Potentially large
    public List<HomeSale> getAllSales() {
        try (MongoClient mongoClient = MongoClients.create(DB_URL)) {
            // Get the database
            MongoDatabase sampleTrainingDB = mongoClient.getDatabase("RealEstateDB");

            // Get the specific collection of residencies
            MongoCollection<Document> residenciesCollection = sampleTrainingDB.getCollection("residencies");

            // Retrieve all documents from the collection
            List<HomeSale> homeSales = new ArrayList<>();
            int count = 0;
            for (Document doc : residenciesCollection.find()) {
                // Map each document to a HomeSale object
                HomeSale homeSale = new HomeSale(
                        doc.getObjectId("_id").toString(),
                        doc.getString("post_code"),
                        doc.getString("purchase_price"),
                        doc.getString("area_type"));
                homeSales.add(homeSale);
                count++;

                // Have a limit just for testing
                if (count > 10) {
                    break;
                }
            }

            return homeSales;
        } catch (Exception e) {
            System.err.println("Error in getAllSales Dao: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    // returns a List of homesales in a given postCode
    public List<HomeSale> getSalesByarea_type(String area_type) {
        try (MongoClient mongoClient = MongoClients.create(DB_URL)) {
            // Get the database
            MongoDatabase sampleTrainingDB = mongoClient.getDatabase("RealEstateDB");

            // Get the specific collection of residencies
            MongoCollection<Document> residenciesCollection = sampleTrainingDB.getCollection("residencies");

            // Query to find documents with the given postCode
            Document query = new Document("area_type", area_type);

            // Retrieve matching documents
            List<HomeSale> homeSales = new ArrayList<>();
            int count = 0;
            for (Document doc : residenciesCollection.find(query)) {
                // Map each document to a HomeSale object
                HomeSale homeSale = new HomeSale(
                        doc.getObjectId("_id").toString(),
                        doc.getString("post_code"),
                        doc.getString("purchase_price"),
                        doc.getString("area_type"));
                homeSales.add(homeSale);

                count++;
                if (count > 10) {
                    break;
                }
            }
            return homeSales;
        } catch (Exception e) {
            System.err.println("Error in getSalesByPostCode Dao: " + e.getMessage());
            return Collections.emptyList();
        }
    }
}
