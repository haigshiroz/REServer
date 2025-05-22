package app.kafka;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import sales.SalesDAO;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.Collections;

public class ViewConsumer {
    private final KafkaConsumer<String, String> consumer;
    private final SalesDAO salesDAO;
    private final ObjectMapper objectMapper;
    private static final String VIEW_TOPIC = "property-views";
    private static final String CONSUMER_GROUP = "analytics-group";
    private volatile boolean running = true;

    public ViewConsumer(SalesDAO salesDAO) {
        this.consumer = new KafkaConsumer<>(KafkaConfig.getConsumerProps(CONSUMER_GROUP));
        this.salesDAO = salesDAO;
        this.objectMapper = new ObjectMapper();
        this.consumer.subscribe(Collections.singletonList(VIEW_TOPIC));
    }

    public void startConsuming() {
        new Thread(() -> {
            while (running) {
                try {
                    ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                    for (ConsumerRecord<String, String> record : records) {
                        String eventJson = record.value();
                        ViewEvent event = objectMapper.readValue(eventJson, ViewEvent.class);
                        salesDAO.incrementViews(event.getId());
                    }
                } catch (Exception e) {
                    System.err.println("Error consuming view event: " + e.getMessage());
                }
            }
        }).start();
    }

    public void stop() {
        running = false;
        consumer.close();
    }

    private static class ViewEvent {
        private String id;
        private boolean isPostcode;

        // Required for Jackson deserialization
        public ViewEvent() {}

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
