package com.fooddelivery.restaurant.repository;

import com.fooddelivery.restaurant.domain.MenuItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    List<MenuItem> findAllByRestaurantId(Long restaurantId);
}
