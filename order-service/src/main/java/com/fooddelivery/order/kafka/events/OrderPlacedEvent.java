package com.fooddelivery.order.kafka.events;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class OrderPlacedEvent {

    private Long orderId;
    private Long restaurantId;
    private String customerName;
    private List<Item> items;
    private Instant createdAt;

    public OrderPlacedEvent() {
    }

    public OrderPlacedEvent(Long orderId, Long restaurantId, String customerName, List<Item> items, Instant createdAt) {
        this.orderId = orderId;
        this.restaurantId = restaurantId;
        this.customerName = customerName;
        this.items = items;
        this.createdAt = createdAt;
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

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public static class Item {
        private String name;
        private BigDecimal price;
        private Integer quantity;

        public Item() {
        }

        public Item(String name, BigDecimal price, Integer quantity) {
            this.name = name;
            this.price = price;
            this.quantity = quantity;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }
    }
}
