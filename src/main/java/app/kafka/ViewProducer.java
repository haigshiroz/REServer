package app.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ViewProducer {
    private final KafkaProducer<String, String> producer;
    private final ObjectMapper objectMapper;
    private static final String VIEW_TOPIC = "property-views";

    public ViewProducer() {
        this.producer = new KafkaProducer<>(KafkaConfig.getProducerProps());
        this.objectMapper = new ObjectMapper();
    }

    public void sendViewEvent(String id, boolean isPostcode) {
        try {
            String eventJson = objectMapper.writeValueAsString(new ViewEvent(id, isPostcode));
            ProducerRecord<String, String> record = new ProducerRecord<>(
                VIEW_TOPIC,
                id,
                eventJson
            );
            producer.send(record);
        } catch (Exception e) {
            System.err.println("Error sending view event: " + e.getMessage());
        }
    }

    public void close() {
        producer.close();
    }

    private static class ViewEvent {
        private final String id;
        private final boolean isPostcode;

        public ViewEvent(String id, boolean isPostcode) {
            this.id = id;
            this.isPostcode = isPostcode;
        }

        public String getId() {
            return id;
        }

        public boolean isPostcode() {
            return isPostcode;
        }
    }
}