package com.fooddelivery.order.kafka.events;

public class DeliveryStatusUpdatedEvent {

    private Long orderId;
    private String status;

    public DeliveryStatusUpdatedEvent() {
    }

    public DeliveryStatusUpdatedEvent(Long orderId, String status) {
        this.orderId = orderId;
        this.status = status;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
