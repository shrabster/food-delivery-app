package com.fooddelivery.restaurant.kafka;

import com.fooddelivery.restaurant.kafka.events.OrderReadyEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public OrderEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishOrderReady(Long orderId, Long restaurantId) {
        OrderReadyEvent event = new OrderReadyEvent(orderId, restaurantId);
        kafkaTemplate.send(KafkaTopics.ORDER_READY, orderId.toString(), event);
    }
}
