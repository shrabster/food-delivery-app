package com.fooddelivery.order.kafka;

import com.fooddelivery.order.domain.OrderStatus;
import com.fooddelivery.order.kafka.events.DeliveryStatusUpdatedEvent;
import com.fooddelivery.order.repository.OrderRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class DeliveryStatusConsumer {

    private final OrderRepository orderRepository;

    public DeliveryStatusConsumer(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @KafkaListener(topics = KafkaTopics.DELIVERY_STATUS_UPDATED, containerFactory = "deliveryStatusListenerFactory")
    public void onDeliveryStatusUpdated(DeliveryStatusUpdatedEvent event) {
        orderRepository.findById(event.getOrderId()).ifPresent(order -> {
            order.setStatus(OrderStatus.valueOf(event.getStatus()));
            orderRepository.save(order);
        });
    }
}
