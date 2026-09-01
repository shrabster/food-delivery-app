package com.fooddelivery.delivery.kafka;

import com.fooddelivery.delivery.domain.Courier;
import com.fooddelivery.delivery.domain.Delivery;
import com.fooddelivery.delivery.kafka.events.OrderReadyEvent;
import com.fooddelivery.delivery.repository.CourierRepository;
import com.fooddelivery.delivery.repository.DeliveryRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class OrderReadyConsumer {

    private final DeliveryRepository deliveryRepository;
    private final CourierRepository courierRepository;

    public OrderReadyConsumer(DeliveryRepository deliveryRepository, CourierRepository courierRepository) {
        this.deliveryRepository = deliveryRepository;
        this.courierRepository = courierRepository;
    }

    @Transactional
    @KafkaListener(topics = KafkaTopics.ORDER_READY, groupId = "delivery-service")
    public void onOrderReady(OrderReadyEvent event) {
        if (deliveryRepository.findByOrderId(event.getOrderId()).isPresent()) {
            return;
        }

        Courier courier = courierRepository.findNextAvailableForUpdate().orElse(null);
        if (courier == null) {
            return;
        }
        courier.setAvailable(false);
        courierRepository.save(courier);

        Delivery delivery = new Delivery();
        delivery.setOrderId(event.getOrderId());
        delivery.setRestaurantId(event.getRestaurantId());
        delivery.setCourierId(courier.getId());
        deliveryRepository.save(delivery);
    }
}
