package com.fooddelivery.restaurant.web.dto;

import com.fooddelivery.restaurant.domain.QueueEntry;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class QueueEntryResponse {

    private Long orderId;
    private String customerName;
    private String status;
    private Instant createdAt;
    private List<ItemResponse> items;

    public static QueueEntryResponse from(QueueEntry entry) {
        QueueEntryResponse response = new QueueEntryResponse();
        response.orderId = entry.getOrderId();
        response.customerName = entry.getCustomerName();
        response.status = entry.getStatus().name();
        response.createdAt = entry.getCreatedAt();
        response.items = entry.getItems().stream()
                .map(item -> new ItemResponse(item.getName(), item.getPrice(), item.getQuantity()))
                .toList();
        return response;
    }

    public Long getOrderId() {
        return orderId;
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

    public record ItemResponse(String name, BigDecimal price, Integer quantity) {
    }
}
