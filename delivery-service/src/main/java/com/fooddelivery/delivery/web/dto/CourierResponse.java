package com.fooddelivery.delivery.web.dto;

import com.fooddelivery.delivery.domain.Courier;

public record CourierResponse(Long id, String name, boolean available) {

    public static CourierResponse from(Courier courier) {
        return new CourierResponse(courier.getId(), courier.getName(), courier.isAvailable());
    }
}
