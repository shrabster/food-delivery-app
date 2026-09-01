package com.fooddelivery.order.kafka.events;

public class OrderReadyEvent {

    private Long orderId;
    private Long restaurantId;

    public OrderReadyEvent() {
    }

    public OrderReadyEvent(Long orderId, Long restaurantId) {
        this.orderId = orderId;
        this.restaurantId = restaurantId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(Long restaurantId) {
        this.restaurantId = restaurantId;
    }
}
