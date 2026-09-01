package com.fooddelivery.delivery.web.dto;

import com.fooddelivery.delivery.domain.Delivery;
import java.time.Instant;

public record DeliveryResponse(Long id, Long orderId, Long restaurantId, Long courierId, String status, Instant createdAt) {

    public static DeliveryResponse from(Delivery delivery) {
        return new DeliveryResponse(
                delivery.getId(),
                delivery.getOrderId(),
                delivery.getRestaurantId(),
                delivery.getCourierId(),
                delivery.getStatus().name(),
                delivery.getCreatedAt());
    }
}
