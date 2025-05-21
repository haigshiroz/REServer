package app;

import io.javalin.Javalin;
import io.javalin.http.Context;

import com.apple.laf.ClientPropertyApplicator.Property;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.ArrayList;

public class PropertyService {
    private final Map<String, Property> properties;
    private final ObjectMapper objectMapper;

    public PropertyService() {
        this.properties = new ConcurrentHashMap<>();
        this.objectMapper = new ObjectMapper();
  
    }

    public void start() {
        Javalin app = Javalin.create(config -> {
            config.plugins.enableCors(cors -> {
                cors.add(it -> it.anyHost());
            });
        }).start(7001);

        app.get("/properties/{id}", this::getProperty);
        app.get("/properties", this::getAllProperties);
        app.post("/properties", this::createProperty);
        app.put("/properties/{id}", this::updateProperty);
        app.delete("/properties/{id}", this::deleteProperty);
    }

    private void getProperty(Context ctx) {
        String id = ctx.pathParam("id");
        Property property = properties.get(id);
        
        if (property != null) {
            ctx.json(property);
        } else {
            ctx.status(404).result("Property not found");
        }
    }

    private void getAllProperties(Context ctx) {
        List<Property> propertyList = new ArrayList<>(properties.values());
        ctx.json(propertyList);
    }

    private void createProperty(Context ctx) {
        try {
            Property property = objectMapper.readValue(ctx.body(), Property.class);
            
            // Generate an ID if not provided
            if (property.getId() == null || property.getId().isEmpty()) {
                property.setId(UUID.randomUUID().toString());
            }
            
            properties.put(property.getId(), property);
            ctx.status(201).json(property);
        } catch (Exception e) {
            ctx.status(400).result("Invalid property data: " + e.getMessage());
        }
    }

    private void updateProperty(Context ctx) {
        String id = ctx.pathParam("id");
        
        try {
            Property updatedProperty = objectMapper.readValue(ctx.body(), Property.class);
            
            if (properties.containsKey(id)) {
                updatedProperty.setId(id); // Ensure ID is preserved
                properties.put(id, updatedProperty);
                ctx.json(updatedProperty);
            } else {
                ctx.status(404).result("Property not found");
            }
        } catch (Exception e) {
            ctx.status(400).result("Invalid property data: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        new PropertyService().start();
    }
}
