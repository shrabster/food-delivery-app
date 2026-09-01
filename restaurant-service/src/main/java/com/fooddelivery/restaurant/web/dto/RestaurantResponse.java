package com.fooddelivery.restaurant.web.dto;

import com.fooddelivery.restaurant.domain.Restaurant;

public record RestaurantResponse(Long id, String name, String address) {

    public static RestaurantResponse from(Restaurant restaurant) {
        return new RestaurantResponse(restaurant.getId(), restaurant.getName(), restaurant.getAddress());
    }
}
