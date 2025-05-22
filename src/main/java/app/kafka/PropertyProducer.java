package app.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import sales.HomeSale;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PropertyProducer {
    private final KafkaProducer<String, String> producer;
    private final ObjectMapper objectMapper;
    private static final String PROPERTY_TOPIC = "property-events";

    public PropertyProducer() {
        this.producer = new KafkaProducer<>(KafkaConfig.getProducerProps());
        this.objectMapper = new ObjectMapper();
    }

    public void sendPropertyEvent(HomeSale sale) {
        try {
            String saleJson = objectMapper.writeValueAsString(sale);
            ProducerRecord<String, String> record = new ProducerRecord<>(
                PROPERTY_TOPIC,
                sale.getId().toString(),
                saleJson
            );
            producer.send(record);
        } catch (Exception e) {
            System.err.println("Error sending property event: " + e.getMessage());
        }
    }

    public void close() {
        producer.close();
    }
}
