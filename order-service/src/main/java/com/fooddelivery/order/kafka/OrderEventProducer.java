package com.fooddelivery.order.kafka;

import com.fooddelivery.order.domain.Order;
import com.fooddelivery.order.kafka.events.OrderPlacedEvent;
import java.util.List;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public OrderEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishOrderPlaced(Order order) {
        List<OrderPlacedEvent.Item> items = order.getItems().stream()
                .map(item -> new OrderPlacedEvent.Item(item.getName(), item.getPrice(), item.getQuantity()))
                .toList();

        OrderPlacedEvent event = new OrderPlacedEvent(
                order.getId(),
                order.getRestaurantId(),
                order.getCustomerName(),
                items,
                order.getCreatedAt());

        kafkaTemplate.send(KafkaTopics.ORDER_PLACED, order.getId().toString(), event);
    }
}
