package com.fooddelivery.restaurant.web.dto;

import com.fooddelivery.restaurant.domain.MenuItem;
import java.math.BigDecimal;

public record MenuItemResponse(Long id, String name, BigDecimal price) {

    public static MenuItemResponse from(MenuItem menuItem) {
        return new MenuItemResponse(menuItem.getId(), menuItem.getName(), menuItem.getPrice());
    }
}
