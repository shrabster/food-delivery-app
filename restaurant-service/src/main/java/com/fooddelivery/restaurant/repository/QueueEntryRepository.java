package com.fooddelivery.restaurant.repository;

import com.fooddelivery.restaurant.domain.QueueEntry;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QueueEntryRepository extends JpaRepository<QueueEntry, Long> {

    Optional<QueueEntry> findByOrderId(Long orderId);

    List<QueueEntry> findAllByRestaurantIdOrderByCreatedAtAsc(Long restaurantId);
}
