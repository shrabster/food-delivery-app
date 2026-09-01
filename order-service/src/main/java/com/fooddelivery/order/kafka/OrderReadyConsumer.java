package com.fooddelivery.order.kafka;

import com.fooddelivery.order.domain.OrderStatus;
import com.fooddelivery.order.kafka.events.OrderReadyEvent;
import com.fooddelivery.order.repository.OrderRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderReadyConsumer {

    private final OrderRepository orderRepository;

    public OrderReadyConsumer(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @KafkaListener(topics = KafkaTopics.ORDER_READY, containerFactory = "orderReadyListenerFactory")
    public void onOrderReady(OrderReadyEvent event) {
        orderRepository.findById(event.getOrderId()).ifPresent(order -> {
            order.setStatus(OrderStatus.READY);
            orderRepository.save(order);
        });
    }
}
