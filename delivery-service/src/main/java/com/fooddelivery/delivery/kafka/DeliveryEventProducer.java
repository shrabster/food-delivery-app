package com.fooddelivery.delivery.kafka;

import com.fooddelivery.delivery.kafka.events.DeliveryStatusUpdatedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class DeliveryEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public DeliveryEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishStatusUpdated(Long orderId, String status) {
        DeliveryStatusUpdatedEvent event = new DeliveryStatusUpdatedEvent(orderId, status);
        kafkaTemplate.send(KafkaTopics.DELIVERY_STATUS_UPDATED, orderId.toString(), event);
    }
}
