package com.fooddelivery.order.kafka;

public final class KafkaTopics {

    public static final String ORDER_PLACED = "order.placed";
    public static final String ORDER_READY = "order.ready";
    public static final String DELIVERY_STATUS_UPDATED = "delivery.status.updated";

    private KafkaTopics() {
    }
}
