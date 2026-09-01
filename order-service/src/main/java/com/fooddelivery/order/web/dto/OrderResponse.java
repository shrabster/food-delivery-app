package com.fooddelivery.order.web.dto;

import com.fooddelivery.order.domain.Order;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class OrderResponse {

    private Long id;
    private Long restaurantId;
    private String customerName;
    private String status;
    private Instant createdAt;
    private List<ItemResponse> items;
    private BigDecimal total;

    public static OrderResponse from(Order order) {
        OrderResponse response = new OrderResponse();
        response.id = order.getId();
        response.restaurantId = order.getRestaurantId();
        response.customerName = order.getCustomerName();
        response.status = order.getStatus().name();
        response.createdAt = order.getCreatedAt();
        response.items = order.getItems().stream()
                .map(item -> new ItemResponse(item.getName(), item.getPrice(), item.getQuantity()))
                .toList();
        response.total = order.getItems().stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return response;
    }

    public Long getId() {
        return id;
    }

    public Long getRestaurantId() {
        return restaurantId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public List<ItemResponse> getItems() {
        return items;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public record ItemResponse(String name, BigDecimal price, Integer quantity) {
    }
}
